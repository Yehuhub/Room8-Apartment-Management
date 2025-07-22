package jmc.ex4.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jmc.ex4.model.Apartment;
import jmc.ex4.model.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO for {@link jmc.ex4.model.Expense}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto implements Serializable {
    @NotNull
    Long apartmentId;

    @NotNull
    Long payerId;

    @NotEmpty
    String description;

    @NotNull
    @Positive
    BigDecimal totalAmount;
}