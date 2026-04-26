package com.ntt.customer.model.dto;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerSummaryResponse {
  private String id;
  private CustomerType type;
  private CustomerProfile profile;
  private CustomerStatus status;
  private String documentNumber;
}
