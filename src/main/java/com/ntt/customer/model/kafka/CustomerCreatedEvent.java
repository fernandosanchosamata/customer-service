package com.ntt.customer.model.kafka;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerCreatedEvent {
  private String customerId;
  private CustomerType type;
  private CustomerProfile profile;
  private String documentNumber;
  private String email;
  @Builder.Default private String eventType = "CUSTOMER_CREATED";
}
