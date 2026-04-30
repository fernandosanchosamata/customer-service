package com.ntt.customer.controller;

import com.ntt.customer.model.dto.CustomerRequest;
import com.ntt.customer.model.dto.CustomerResponse;
import com.ntt.customer.model.dto.CustomerSummaryResponse;
import com.ntt.customer.service.CustomerService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

  private final CustomerService customerService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<ResponseEntity<CustomerResponse>> createCustomer(
      @Valid @RequestBody CustomerRequest request) {
    log.info(
        "Solicitud recibida para crear cliente. type={}, profile={}",
        request.getType(),
        request.getProfile());
    return customerService
        .createCustomer(request)
        .doOnSuccess(
            response -> log.info("Cliente creado exitosamente. customerId={}", response.getId()))
        .doOnError(error -> log.warn("No se pudo crear cliente: {}", error.getMessage()))
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
  }

  @GetMapping("/{id}")
  public Single<ResponseEntity<CustomerResponse>> getCustomerById(@PathVariable String id) {
    log.info("Solicitud recibida para consultar cliente. customerId={}", id);
    return customerService
        .getCustomerById(id)
        .doOnSuccess(response -> log.info("Cliente consultado. customerId={}", response.getId()))
        .doOnError(error -> log.warn("No se pudo consultar cliente: {}", error.getMessage()))
        .map(ResponseEntity::ok);
  }

  @GetMapping("/{id}/summary")
  public Single<ResponseEntity<CustomerSummaryResponse>> getCustomerSummaryById(
      @PathVariable String id) {
    log.debug("Solicitud recibida para consultar resumen de cliente. customerId={}", id);
    return customerService.getCustomerSummaryById(id).map(ResponseEntity::ok);
  }

  @GetMapping("/document/{documentNumber}")
  public Single<ResponseEntity<CustomerResponse>> getCustomerByDocument(
      @PathVariable String documentNumber) {
    log.info("Solicitud recibida para consultar cliente por documento.");
    return customerService
        .getCustomerByDocument(documentNumber)
        .doOnSuccess(
            response ->
                log.info("Cliente consultado por documento. customerId={}", response.getId()))
        .doOnError(
            error -> log.warn("No se pudo consultar cliente por documento: {}", error.getMessage()))
        .map(ResponseEntity::ok);
  }

  @PutMapping("/{id}")
  public Single<ResponseEntity<CustomerResponse>> updateCustomer(
      @PathVariable String id, @Valid @RequestBody CustomerRequest request) {
    log.info("Solicitud recibida para actualizar cliente. customerId={}", id);
    return customerService
        .updateCustomer(id, request)
        .doOnSuccess(response -> log.info("Cliente actualizado. customerId={}", response.getId()))
        .doOnError(error -> log.warn("No se pudo actualizar cliente: {}", error.getMessage()))
        .map(ResponseEntity::ok);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable deleteCustomer(@PathVariable String id) {
    log.info("Solicitud recibida para desactivar cliente. customerId={}", id);
    return customerService
        .deleteCustomer(id)
        .doOnComplete(() -> log.info("Cliente desactivado. customerId={}", id))
        .doOnError(error -> log.warn("No se pudo desactivar cliente: {}", error.getMessage()));
  }
}
