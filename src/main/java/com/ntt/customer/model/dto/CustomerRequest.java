package com.ntt.customer.model.dto;

import com.ntt.customer.model.enums.CustomerProfile;
import com.ntt.customer.model.enums.CustomerType;
import com.ntt.customer.model.enums.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerRequest {

  @NotNull(message = "El tipo de cliente no puede ser nulo")
  private CustomerType type;

  private CustomerProfile profile; // Opcional, por defecto REGULAR

  @NotNull(message = "El tipo de documento no puede ser nulo")
  private DocumentType documentType;

  @NotBlank(message = "El numero de documento es obligatorio")
  private String documentNumber;

  // Estos podrían ser nulos dependiendo si es Personal o Empresarial
  private String firstName;
  private String lastName;
  private String businessName;

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "Debe ser un email válido")
  private String email;

  @NotBlank(message = "El telefono es obligatorio")
  private String phone;
}
