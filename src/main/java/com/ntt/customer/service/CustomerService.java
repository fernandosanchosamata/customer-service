package com.ntt.customer.service;

import com.ntt.customer.model.dto.CustomerRequest;
import com.ntt.customer.model.dto.CustomerResponse;
import com.ntt.customer.model.dto.CustomerSummaryResponse;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface CustomerService {

  Single<CustomerResponse> createCustomer(CustomerRequest request);

  Single<CustomerResponse> getCustomerById(String id);

  Single<CustomerSummaryResponse> getCustomerSummaryById(String id);

  Single<CustomerResponse> getCustomerByDocument(String documentNumber);

  Single<CustomerResponse> updateCustomer(String id, CustomerRequest request);

  Completable deleteCustomer(String id);
}
