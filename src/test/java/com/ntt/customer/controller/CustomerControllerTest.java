package com.ntt.customer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ntt.customer.model.dto.CustomerRequest;
import com.ntt.customer.model.dto.CustomerResponse;
import com.ntt.customer.model.dto.CustomerSummaryResponse;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import com.ntt.customer.model.enums.DocumentType;
import com.ntt.customer.service.CustomerService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

  @Mock private CustomerService customerService;

  @InjectMocks private CustomerController controller;

  @Test
  void createCustomerReturnsCreatedResponse() {
    CustomerRequest request = customerRequest();
    CustomerResponse response = customerResponse();
    when(customerService.createCustomer(request)).thenReturn(Single.just(response));

    var result = controller.createCustomer(request).blockingGet();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isEqualTo(response);
    verify(customerService).createCustomer(request);
  }

  @Test
  void getCustomerSummaryReturnsOkResponse() {
    CustomerSummaryResponse response =
        CustomerSummaryResponse.builder()
            .id("customer-1")
            .type(CustomerType.PERSONAL)
            .status(CustomerStatus.ACTIVE)
            .documentNumber("12345678")
            .build();
    when(customerService.getCustomerSummaryById("customer-1")).thenReturn(Single.just(response));

    var result = controller.getCustomerSummaryById("customer-1").blockingGet();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void deleteCustomerCompletes() {
    when(customerService.deleteCustomer("customer-1")).thenReturn(Completable.complete());

    controller.deleteCustomer("customer-1").blockingAwait();

    verify(customerService).deleteCustomer("customer-1");
  }

  private CustomerRequest customerRequest() {
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

  private CustomerResponse customerResponse() {
    return CustomerResponse.builder()
        .id("customer-1")
        .type(CustomerType.PERSONAL)
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
