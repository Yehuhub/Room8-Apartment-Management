package jmc.ex4.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jmc.ex4.model.enums.Role;
import jmc.ex4.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing UserInfo entities.
 * Provides methods to check existence by email, find by ID and role, and find by email.
 */
@Repository
public interface UsersRepository extends JpaRepository<UserInfo, Long> {

    boolean existsByEmailIgnoreCase(String email);
    Optional<UserInfo> findByIdAndRole(Long id, Role role);
    Optional<UserInfo> findByEmailIgnoreCase(@Email @NotEmpty String email);
}
