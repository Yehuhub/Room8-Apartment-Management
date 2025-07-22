package jmc.ex4.model.enums;

import lombok.Getter;

/**
 * Enum representing the status of a chore.
 */
@Getter
public enum ChoreStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    OVERDUE("Overdue");

    private final String displayName;

    ChoreStatus(String displayName) {
        this.displayName = displayName;
    }

}
