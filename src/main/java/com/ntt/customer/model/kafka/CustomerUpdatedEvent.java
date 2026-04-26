package com.ntt.customer.model.kafka;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerUpdatedEvent {
  private String customerId;
  private CustomerType type;
  private CustomerProfile profile;
  private String documentNumber;
  private String email;
  private String phone;
  @Builder.Default private String eventType = "CUSTOMER_UPDATED";
}
