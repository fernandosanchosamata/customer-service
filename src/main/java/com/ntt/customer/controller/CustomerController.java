package com.ntt.customer.controller;

import com.ntt.customer.model.dto.CustomerRequest;
import com.ntt.customer.model.dto.CustomerResponse;
import com.ntt.customer.model.dto.CustomerSummaryResponse;
import com.ntt.customer.service.CustomerService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class CustomerController {

  private final CustomerService customerService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<ResponseEntity<CustomerResponse>> createCustomer(
      @Valid @RequestBody CustomerRequest request) {
    return customerService
        .createCustomer(request)
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
  }

  @GetMapping("/{id}")
  public Single<ResponseEntity<CustomerResponse>> getCustomerById(@PathVariable String id) {
    return customerService.getCustomerById(id).map(ResponseEntity::ok);
  }

  @GetMapping("/{id}/summary")
  public Single<ResponseEntity<CustomerSummaryResponse>> getCustomerSummaryById(
      @PathVariable String id) {
    return customerService.getCustomerSummaryById(id).map(ResponseEntity::ok);
  }

  @GetMapping("/document/{documentNumber}")
  public Single<ResponseEntity<CustomerResponse>> getCustomerByDocument(
      @PathVariable String documentNumber) {
    return customerService.getCustomerByDocument(documentNumber).map(ResponseEntity::ok);
  }

  @PutMapping("/{id}")
  public Single<ResponseEntity<CustomerResponse>> updateCustomer(
      @PathVariable String id, @Valid @RequestBody CustomerRequest request) {
    return customerService.updateCustomer(id, request).map(ResponseEntity::ok);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable deleteCustomer(@PathVariable String id) {
    return customerService.deleteCustomer(id);
  }
}
