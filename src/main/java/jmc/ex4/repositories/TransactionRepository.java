package jmc.ex4.repositories;

import jmc.ex4.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing Transaction entities.
 * Provides methods to perform CRUD operations and custom queries.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findAllByApartmentId(Long apartmentId);
}