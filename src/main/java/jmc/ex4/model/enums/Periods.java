package jmc.ex4.model.enums;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Enum representing different time periods for filtering or displaying data.
 * Provides methods to get the start and end dates for each period.
 */
@Getter
public enum Periods {
    WEEK("Week"),
    MONTH("Month"),
    UPCOMING("Upcoming");

    private final String displayName;

    Periods(String displayName) {
        this.displayName = displayName;
    }

    public LocalDate getStartDate(Periods period) {
        return switch (period) {
            case WEEK -> LocalDate.now().with(DayOfWeek.MONDAY); // Start of this week
            case MONTH -> LocalDate.now().withDayOfMonth(1);     // First day of this month
            case UPCOMING -> LocalDate.now();
        };
    }

    public LocalDate getEndDate(Periods period) {
        return switch (period) {
            case WEEK -> LocalDate.now().with(DayOfWeek.MONDAY).plusDays(6); // End of this week
            case MONTH ->  LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());     // Last day of this month
            case UPCOMING -> LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        };
    }
}
