package jmc.ex4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jmc.ex4.model.enums.Role;
import jmc.ex4.model.UserInfo;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * DTO for {@link UserInfo}
 */
@Data
public class UsersDto implements Serializable {
    @Email(message = "Invalid email address")
    @NotEmpty(message = "Email address can not be empty")
    String email;

    @Pattern(message = "First name must contain only letters and spaces", regexp = "^[A-Za-z ]+$")
    @NotEmpty(message = "First name cannot be empty")
    String firstName;

    @Pattern(message = "Last name must contain only letters and spaces", regexp = "^[A-Za-z ]+$")
    @NotEmpty(message = "last name cannot be empty")
    String lastName;

    @Length(min = 3, max = 20, message = "Password must be between 3-20 characters")
    @Pattern(regexp = "\\S+", message = "Password must not contain spaces")
    String password;

    @NotNull(message = "Must provide a user role")
    Role role;


    String apartmentReferenceCode; // Optional, used for tenant registration
}