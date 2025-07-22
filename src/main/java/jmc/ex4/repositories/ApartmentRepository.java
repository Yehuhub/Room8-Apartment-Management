package jmc.ex4.repositories;

import jmc.ex4.model.Apartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Apartment entities.
 * Provides methods to perform CRUD operations and custom queries.
 */
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    boolean existsByAddressIgnoreCase(String address);
    Optional<Apartment> findByReferenceCode(String referenceCode);
    boolean existsByReferenceCode(String referenceCode);

    @EntityGraph(attributePaths = {"tenants"})
    List<Apartment> findByOwnerId(Long ownerId);
}