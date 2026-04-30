package com.ntt.customer.service.impl;

import com.ntt.customer.model.dto.CustomerCacheDto;
import com.ntt.customer.model.dto.CustomerRequest;
import com.ntt.customer.model.dto.CustomerResponse;
import com.ntt.customer.model.dto.CustomerSummaryResponse;
import com.ntt.customer.model.entity.Customer;
import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import com.ntt.customer.model.kafka.CustomerCreatedEvent;
import com.ntt.customer.model.kafka.CustomerUpdatedEvent;
import com.ntt.customer.repository.CustomerRepository;
import com.ntt.customer.service.CustomerService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final ReactiveRedisOperations<String, CustomerCacheDto> redisOperations;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String REDIS_PREFIX = "CUSTOMER_";
  private static final String KAFKA_TOPIC = "customers-topic";

  @Override
  public Single<CustomerResponse> createCustomer(CustomerRequest request) {
    log.info(
        "Iniciando creacion de cliente. type={}, profile={}",
        request.getType(),
        request.getProfile());
    return customerRepository
        .existsByDocumentNumber(request.getDocumentNumber())
        .flatMap(
            exists -> {
              if (Boolean.TRUE.equals(exists)) {
                log.warn("Creacion de cliente rechazada por documento duplicado.");
                return Single.error(
                    new IllegalArgumentException("El documento ya se encuentra registrado."));
              }
              validateBusinessRules(request);

              Customer customer = buildEntity(request);

              return customerRepository
                  .save(customer)
                  .doOnSuccess(saved -> log.info("Cliente guardado. customerId={}", saved.getId()))
                  .flatMap(saved -> updateCacheAndPublishEvent(saved, true));
            });
  }

  @Override
  public Single<CustomerResponse> getCustomerById(String id) {
    log.info("Consultando cliente. customerId={}", id);
    return customerRepository
        .findById(id)
        .switchIfEmpty(Single.error(new IllegalArgumentException("Cliente no encontrado.")))
        .map(this::buildResponse);
  }

  @Override
  public Single<CustomerSummaryResponse> getCustomerSummaryById(String id) {
    log.debug("Consultando resumen de cliente. customerId={}", id);
    return RxJava3Adapter.monoToMaybe(redisOperations.opsForValue().get(REDIS_PREFIX + id))
        .filter(this::hasCompleteCacheData)
        .doOnSuccess(
            cache -> log.debug("Resumen de cliente encontrado en Redis. customerId={}", id))
        .map(cacheDto -> buildSummaryFromCache(id, cacheDto))
        .switchIfEmpty(
            customerRepository
                .findById(id)
                .switchIfEmpty(Single.error(new IllegalArgumentException("Cliente no encontrado.")))
                .flatMap(this::cacheAndBuildSummary));
  }

  @Override
  public Single<CustomerResponse> getCustomerByDocument(String documentNumber) {
    log.info("Consultando cliente por documento.");
    return customerRepository
        .findByDocumentNumber(documentNumber)
        .switchIfEmpty(Single.error(new IllegalArgumentException("Cliente no encontrado.")))
        .map(this::buildResponse);
  }

  @Override
  public Single<CustomerResponse> updateCustomer(String id, CustomerRequest request) {
    log.info("Iniciando actualizacion de cliente. customerId={}", id);
    return customerRepository
        .findById(id)
        .switchIfEmpty(Single.error(new IllegalArgumentException("Cliente no encontrado.")))
        .flatMap(
            existing -> {
              existing.setEmail(request.getEmail());
              existing.setPhone(request.getPhone());

              return customerRepository
                  .save(existing)
                  .doOnSuccess(
                      saved ->
                          log.info(
                              "Cliente guardado tras actualizacion. customerId={}", saved.getId()))
                  .flatMap(saved -> updateCacheAndPublishEvent(saved, false));
            });
  }

  @Override
  public Completable deleteCustomer(String id) {
    log.info("Iniciando desactivacion de cliente. customerId={}", id);
    return customerRepository
        .findById(id)
        .switchIfEmpty(Single.error(new IllegalArgumentException("Cliente no encontrado.")))
        .flatMap(
            existing -> {
              existing.setStatus(CustomerStatus.INACTIVE);
              return customerRepository.save(existing);
            })
        .flatMapCompletable(
            saved -> {
              log.info("Cliente marcado como inactivo. customerId={}", saved.getId());
              // Adaptar la operacion Mono de Redis a Completable
              return RxJava3Adapter.monoToCompletable(
                  redisOperations.opsForValue().delete(REDIS_PREFIX + saved.getId()));
            });
  }

  private Single<CustomerResponse> updateCacheAndPublishEvent(Customer customer, boolean isNew) {
    CustomerCacheDto cacheDto =
        CustomerCacheDto.builder()
            .type(customer.getType())
            .profile(customer.getProfile())
            .status(customer.getStatus())
            .documentNumber(customer.getDocumentNumber())
            .build();

    // Convertimos el Mono de Redis a Single Native
    return RxJava3Adapter.monoToSingle(
            redisOperations.opsForValue().set(REDIS_PREFIX + customer.getId(), cacheDto))
        .map(
            success -> {
              if (isNew) {
                log.info("Publicando evento de cliente creado. customerId={}", customer.getId());
                kafkaTemplate.send(
                    KAFKA_TOPIC,
                    customer.getId(),
                    CustomerCreatedEvent.builder()
                        .customerId(customer.getId())
                        .type(customer.getType())
                        .profile(customer.getProfile())
                        .documentNumber(customer.getDocumentNumber())
                        .email(customer.getEmail())
                        .build());
              } else {
                log.info(
                    "Publicando evento de cliente actualizado. customerId={}", customer.getId());
                kafkaTemplate.send(
                    KAFKA_TOPIC,
                    customer.getId(),
                    CustomerUpdatedEvent.builder()
                        .customerId(customer.getId())
                        .type(customer.getType())
                        .profile(customer.getProfile())
                        .documentNumber(customer.getDocumentNumber())
                        .email(customer.getEmail())
                        .phone(customer.getPhone())
                        .build());
              }
              return buildResponse(customer);
            });
  }

  private void validateBusinessRules(CustomerRequest request) {
    if (request.getProfile() == null) {
      request.setProfile(CustomerProfile.REGULAR);
    }

    if (request.getType() == CustomerType.PERSONAL) {
      if (request.getFirstName() == null || request.getLastName() == null) {
        throw new IllegalArgumentException("Clientes personales deben tener Nombres y Apellidos.");
      }
      if (request.getProfile() == CustomerProfile.PYME) {
        throw new IllegalArgumentException("Un cliente PERSONAL no puede tener el perfil PYME.");
      }
    } else {
      if (request.getBusinessName() == null) {
        throw new IllegalArgumentException("Clientes empresariales deben tener una Razón Social.");
      }
      if (request.getProfile() == CustomerProfile.VIP) {
        throw new IllegalArgumentException("Un cliente EMPRESARIAL no puede tener el perfil VIP.");
      }
    }
  }

  private Customer buildEntity(CustomerRequest request) {
    return Customer.builder()
        .type(request.getType())
        .profile(request.getProfile())
        .documentType(request.getDocumentType())
        .documentNumber(request.getDocumentNumber())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .businessName(request.getBusinessName())
        .email(request.getEmail())
        .phone(request.getPhone())
        .status(CustomerStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private CustomerResponse buildResponse(Customer customer) {
    return CustomerResponse.builder()
        .id(customer.getId())
        .type(customer.getType())
        .profile(customer.getProfile())
        .documentType(customer.getDocumentType())
        .documentNumber(customer.getDocumentNumber())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .businessName(customer.getBusinessName())
        .email(customer.getEmail())
        .phone(customer.getPhone())
        .status(customer.getStatus())
        .createdAt(customer.getCreatedAt())
        .build();
  }

  private Single<CustomerSummaryResponse> cacheAndBuildSummary(Customer customer) {
    CustomerCacheDto cacheDto =
        CustomerCacheDto.builder()
            .type(customer.getType())
            .profile(customer.getProfile())
            .status(customer.getStatus())
            .documentNumber(customer.getDocumentNumber())
            .build();

    return RxJava3Adapter.monoToSingle(
            redisOperations.opsForValue().set(REDIS_PREFIX + customer.getId(), cacheDto))
        .doOnSuccess(
            success -> log.debug("Resumen de cliente cacheado. customerId={}", customer.getId()))
        .map(success -> buildSummary(customer));
  }

  private CustomerSummaryResponse buildSummaryFromCache(String id, CustomerCacheDto cacheDto) {
    return CustomerSummaryResponse.builder()
        .id(id)
        .type(cacheDto.getType())
        .profile(cacheDto.getProfile())
        .status(cacheDto.getStatus())
        .documentNumber(cacheDto.getDocumentNumber())
        .build();
  }

  private CustomerSummaryResponse buildSummary(Customer customer) {
    return CustomerSummaryResponse.builder()
        .id(customer.getId())
        .type(customer.getType())
        .profile(customer.getProfile())
        .status(customer.getStatus())
        .documentNumber(customer.getDocumentNumber())
        .build();
  }

  private boolean hasCompleteCacheData(CustomerCacheDto cacheDto) {
    return cacheDto.getType() != null
        && cacheDto.getProfile() != null
        && cacheDto.getStatus() != null;
  }
}
