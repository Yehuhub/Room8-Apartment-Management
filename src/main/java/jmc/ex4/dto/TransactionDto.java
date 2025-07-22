package jmc.ex4.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link jmc.ex4.model.Transaction}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto implements Serializable {
    @NotNull
    Long fromUserId;
    @NotNull
    Long toUserId;
    @NotNull
    BigDecimal amount;
    @NotNull
    Long apartmentId;
}