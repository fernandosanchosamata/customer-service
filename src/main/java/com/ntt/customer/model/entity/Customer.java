package com.ntt.customer.model.entity;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerStatus;
import com.ntt.customer.model.enums.CustomerType;
import com.ntt.customer.model.enums.DocumentType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

  @Id private String id;

  private CustomerType type;

  @Builder.Default private CustomerProfile profile = CustomerProfile.REGULAR;

  private DocumentType documentType;

  @Indexed(unique = true)
  private String documentNumber;

  private String firstName;

  private String lastName;

  private String businessName;

  private String email;

  private String phone;

  @Builder.Default private CustomerStatus status = CustomerStatus.ACTIVE;

  @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
