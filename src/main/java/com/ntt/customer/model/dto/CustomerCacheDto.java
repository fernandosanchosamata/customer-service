package com.ntt.customer.model.dto;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCacheDto implements Serializable {
  private CustomerType type;
  private CustomerProfile profile;
  private CustomerStatus status;
  private String documentNumber;
}
