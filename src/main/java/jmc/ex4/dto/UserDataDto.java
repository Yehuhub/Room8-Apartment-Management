package jmc.ex4.dto;


import jmc.ex4.model.UserInfo;
import lombok.Data;

/**
 * Data Transfer Object for User Information.
 * This class is used to transfer user data between different layers of the application.
 */
@Data
public class UserDataDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Long apartmentId;
    private String nameRepresentation;

    public UserDataDto(UserInfo user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.nameRepresentation = user.nameRepresentation();
        this.apartmentId = user.getResidence() != null ? user.getResidence().getId() : null;
    }
}
