package jmc.ex4.dto;

import jmc.ex4.model.Chore;
import jmc.ex4.model.enums.ChoreStatus;
import jmc.ex4.model.enums.RecurrenceInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data Transfer Object for displaying chore information.
 * This DTO is used to transfer chore data between the service layer and the presentation layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoreDisplayDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String assignedToName;
    private String apartmentName;
    private LocalDate scheduledDate;
    private String createdByName;
    private Boolean isRecurring;
    private RecurrenceInterval recurrenceInterval;
    private LocalDate completedDate;
    private Boolean isOverdue; // computed field
    private ChoreStatus status;

    public ChoreDisplayDto(Chore chore) {
        this.id = chore.getId();
        this.name = chore.getName();
        this.description = chore.getDescription();
        this.assignedToName = chore.getAssignedTo().nameRepresentation();
        this.apartmentName = chore.getApartment().getAddress();
        this.scheduledDate = chore.getScheduledDate();
        this.createdByName = chore.getCreatedBy().nameRepresentation();
        this.isRecurring = chore.getIsRecurring();
        this.recurrenceInterval = chore.getRecurrenceInterval();
        this.completedDate = chore.getCompletedAt();
        this.status = chore.getStatus();
        this.isOverdue = chore.isOverdue(); // Set isOverdue based on chore status
    }
}
