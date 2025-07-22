package jmc.ex4.repositories;

import jmc.ex4.model.Apartment;
import jmc.ex4.model.Chore;
import jmc.ex4.model.enums.ChoreStatus;
import jmc.ex4.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing Chore entities.
 * Provides methods to query chores based on various criteria.
 */
public interface ChoreRepository extends JpaRepository<Chore, Long> {
    List<Chore> findByApartmentAndScheduledDateBetweenOrderByScheduledDateAsc(
            Apartment apartment, LocalDate startDate, LocalDate endDate);

    List<Chore> findByStatusAndScheduledDateBefore(ChoreStatus status, LocalDate date);

    List<Chore> findByApartmentAndAssignedToAndScheduledDateBetweenOrderByScheduledDateAsc(Apartment apartment, UserInfo assignedTo, LocalDate scheduledDateStart, LocalDate scheduledDateEnd);

    List<Chore> findAllByApartment(Apartment apartment);
}