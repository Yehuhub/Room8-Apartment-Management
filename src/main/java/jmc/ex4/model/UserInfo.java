package jmc.ex4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jmc.ex4.dto.UsersDto;
import jmc.ex4.model.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a user in the system.
 * Implements UserDetails for Spring Security integration.
 */
@Setter
@Getter
@Entity
public class UserInfo implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Email address can not be empty")
    @Email(message = "Invalid email address")
    private String email;


    @NotEmpty(message = "First name cannot be empty")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @NotEmpty(message = "last name cannot be empty")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters and spaces")
    private String lastName;


    @NotEmpty
    private String password;

    @NotNull(message = "Must provide a user role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner")
    private List<Apartment> ownedApartments = new ArrayList<>();

    @ManyToOne(optional = true)
    @JoinColumn(name = "residence_apartment_id")
    private Apartment residence;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Split> splits = new ArrayList<>();

    public UserInfo() {

    }

    public UserInfo(UsersDto dto){
        this.email = dto.getEmail();
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.role = dto.getRole();
    }


    /**
     * Adds an apartment to the user's list of owned apartments.
     * Also sets the owner of the apartment to this user.
     *
     * @param apartment the apartment to be added
     */
    public void addOwnedApartment(Apartment apartment) {
        ownedApartments.add(apartment);
        apartment.setOwner(this);
    }

    @Override
    public String toString() {
        return "User: " + this.id;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public String nameRepresentation() {
        return firstName + " " + lastName.charAt(0) + ".";
    }
}
