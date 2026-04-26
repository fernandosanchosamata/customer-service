package com.ntt.customer.repository;

import com.ntt.customer.model.entity.Customer;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends RxJava3CrudRepository<Customer, String> {

  Maybe<Customer> findByDocumentNumber(String documentNumber);

  Single<Boolean> existsByDocumentNumber(String documentNumber);
}
