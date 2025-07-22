package jmc.ex4.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link jmc.ex4.model.Apartment}
 */
@Data
public class CreateApartmentDto implements Serializable {
    @NotEmpty
    String address;
}