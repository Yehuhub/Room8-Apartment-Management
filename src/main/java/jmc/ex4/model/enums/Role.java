package jmc.ex4.model.enums;

import lombok.Getter;

/**
 * Enum representing different roles in the application.
 * Each role has a display name for user interface purposes.
 */
@Getter
public enum Role {
    TENANT("Tenant"),
    OWNER("Apartment Owner");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

}
