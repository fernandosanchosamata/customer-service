package com.ntt.customer.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ntt.customer.model.dto.CustomerCacheDto;
import com.ntt.customer.model.dto.CustomerRequest;
import com.ntt.customer.model.entity.Customer;
import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import com.ntt.customer.model.enums.DocumentType;
import com.ntt.customer.repository.CustomerRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

  @Mock private CustomerRepository customerRepository;
  @Mock private ReactiveRedisOperations<String, CustomerCacheDto> redisOperations;
  @Mock private ReactiveValueOperations<String, CustomerCacheDto> valueOperations;
  @Mock private KafkaTemplate<String, Object> kafkaTemplate;

  private CustomerServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new CustomerServiceImpl(customerRepository, redisOperations, kafkaTemplate);
  }

  @Test
  void createCustomerPersistsCacheAndPublishesEvent() {
    CustomerRequest request = personalRequest();
    when(customerRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId("customer-1");
              return Single.just(customer);
            });
    when(redisOperations.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.set(eq("CUSTOMER_customer-1"), any(CustomerCacheDto.class)))
        .thenReturn(Mono.just(true));

    var response = service.createCustomer(request).blockingGet();

    assertThat(response.getId()).isEqualTo("customer-1");
    assertThat(response.getProfile()).isEqualTo(CustomerProfile.REGULAR);
    assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    verify(kafkaTemplate).send(eq("customers-topic"), eq("customer-1"), any());
  }

  @Test
  void createCustomerRejectsDuplicatedDocument() {
    CustomerRequest request = personalRequest();
    when(customerRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(true));

    var observer = service.createCustomer(request).test();

    observer.assertError(error -> error.getMessage().contains("documento ya se encuentra"));
    verify(customerRepository, never()).save(any());
  }

  @Test
  void getCustomerSummaryReturnsCachedData() {
    CustomerCacheDto cacheDto =
        CustomerCacheDto.builder()
            .type(CustomerType.PERSONAL)
            .profile(CustomerProfile.VIP)
            .status(CustomerStatus.ACTIVE)
            .documentNumber("12345678")
            .build();
    when(redisOperations.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("CUSTOMER_customer-1")).thenReturn(Mono.just(cacheDto));
    when(customerRepository.findById("customer-1")).thenReturn(Maybe.empty());

    var summary = service.getCustomerSummaryById("customer-1").blockingGet();

    assertThat(summary.getId()).isEqualTo("customer-1");
    assertThat(summary.getProfile()).isEqualTo(CustomerProfile.VIP);
    verify(customerRepository).findById("customer-1");
  }

  @Test
  void getCustomerSummaryFallsBackToMongoAndCachesResult() {
    Customer customer = activeCustomer();
    when(redisOperations.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("CUSTOMER_customer-1")).thenReturn(Mono.empty());
    when(customerRepository.findById("customer-1")).thenReturn(Maybe.just(customer));
    when(valueOperations.set(eq("CUSTOMER_customer-1"), any(CustomerCacheDto.class)))
        .thenReturn(Mono.just(true));

    var summary = service.getCustomerSummaryById("customer-1").blockingGet();

    assertThat(summary.getId()).isEqualTo("customer-1");
    assertThat(summary.getType()).isEqualTo(CustomerType.PERSONAL);
    verify(valueOperations).set(eq("CUSTOMER_customer-1"), any(CustomerCacheDto.class));
  }

  private CustomerRequest personalRequest() {
    CustomerRequest request = new CustomerRequest();
    request.setType(CustomerType.PERSONAL);
    request.setDocumentType(DocumentType.DNI);
    request.setDocumentNumber("12345678");
    request.setFirstName("Ada");
    request.setLastName("Lovelace");
    request.setEmail("ada@test.com");
    request.setPhone("999888777");
    return request;
  }

  private Customer activeCustomer() {
    return Customer.builder()
        .id("customer-1")
        .type(CustomerType.PERSONAL)
        .profile(CustomerProfile.REGULAR)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .firstName("Ada")
        .lastName("Lovelace")
        .email("ada@test.com")
        .phone("999888777")
        .status(CustomerStatus.ACTIVE)
        .build();
  }
}
