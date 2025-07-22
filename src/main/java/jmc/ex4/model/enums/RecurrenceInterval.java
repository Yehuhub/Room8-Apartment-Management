package jmc.ex4.model.enums;

import lombok.Getter;

/**
 * Enum representing different recurrence intervals for events.
 */
@Getter
public enum RecurrenceInterval {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    private final String displayName;

    RecurrenceInterval(String displayName) {
        this.displayName = displayName;
    }

}
