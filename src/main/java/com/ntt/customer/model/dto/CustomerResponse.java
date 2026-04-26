package com.ntt.customer.model.dto;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import com.ntt.customer.model.enums.DocumentType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
  private String id;
  private CustomerType type;
  private CustomerProfile profile;
  private DocumentType documentType;
  private String documentNumber;
  private String firstName;
  private String lastName;
  private String businessName;
  private String email;
  private String phone;
  private CustomerStatus status;
  private LocalDateTime createdAt;
}
