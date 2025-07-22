package jmc.ex4.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jmc.ex4.model.enums.RecurrenceInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link jmc.ex4.model.Chore}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoreDto implements Serializable {
    @NotNull
    @NotEmpty(message = "Chore name is required")
    String name;
    String description;
    @NotNull
    Long assignedToId;
    @NotNull
    Long apartmentId;
    @NotNull(message = "Scheduled date is required")
    LocalDate scheduledDate;
    @NotNull
    Long createdById;
    @NotNull
    Boolean isRecurring = false;
    RecurrenceInterval recurrenceInterval;
}