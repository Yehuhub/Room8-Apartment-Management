package jmc.ex4.repositories;

import jmc.ex4.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Expense entities.
 * Provides methods to perform CRUD operations and custom queries.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByApartmentIdAndSettledFalse(Long apartmentId);


}