package jmc.ex4.services;

import jmc.ex4.dto.ChoreDto;
import jmc.ex4.model.*;
import jmc.ex4.model.enums.ChoreStatus;
import jmc.ex4.model.enums.Periods;
import jmc.ex4.model.enums.RecurrenceInterval;
import jmc.ex4.repositories.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing chores in an apartment.
 * Provides methods to create, update, retrieve, and manage chores.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ChoreService {
    private final ChoreRepository choreRepository;
    private final UserService userService;
    private final ApartmentService apartmentService;

    /**
     * Retrieves all chores for a given apartment.
     * Marks overdue chores before returning the list.
     *
     * @param apartment The apartment for which to retrieve chores.
     * @return List of all chores in the specified apartment.
     */
    public List<Chore> getAllChores(Apartment apartment){
        markOverdueChores(); // Mark overdue chores before returning
        return choreRepository.findAllByApartment(apartment);
    }

    /**
     * Retrieves filtered chores based on the specified period and user ID.
     * Marks overdue chores before returning the list.
     *
     * @param apartment The apartment for which to retrieve chores.
     * @param userId    The ID of the user to filter chores by (can be null).
     * @param period    The period for which to retrieve chores.
     * @return List of filtered chores in the specified apartment and period.
     */
    public List<Chore> getFilteredChores(Apartment apartment, Long userId, Periods period) {
        markOverdueChores(); // Mark overdue chores before returning
        LocalDate startDate = period.getStartDate(period);
        LocalDate endDate = period.getEndDate(period);

        if(userId == null){
            return choreRepository.findByApartmentAndScheduledDateBetweenOrderByScheduledDateAsc(apartment, startDate, endDate);
        }else{
            UserInfo user = userService.getUserById(userId);
            return choreRepository.findByApartmentAndAssignedToAndScheduledDateBetweenOrderByScheduledDateAsc(apartment, user, startDate, endDate);
        }
    }

    /**
     * Creates a new chore based on the provided ChoreDto.
     * Validates the assigned user, creator, and apartment before saving the chore.
     *
     * @param choreDto The data transfer object containing chore details.
     */
    public void createChore(ChoreDto choreDto) {

        UserInfo assignedTo = userService.getUserById(choreDto.getAssignedToId());
        UserInfo createdBy = userService.getUserById(choreDto.getCreatedById());
        Apartment apartment = apartmentService.getApartmentById(choreDto.getApartmentId());

        Chore chore = new Chore();
        chore.setName(choreDto.getName());
        chore.setDescription(choreDto.getDescription());
        chore.setAssignedTo(assignedTo);
        chore.setApartment(apartment);
        chore.setScheduledDate(choreDto.getScheduledDate());
        chore.setCreatedBy(createdBy);
        chore.setIsRecurring(choreDto.getIsRecurring());
        chore.setRecurrenceInterval(choreDto.getRecurrenceInterval());


        choreRepository.save(chore);

    }

    /**
     * Marks a chore as completed by updating its status and completion date.
     * If the chore is recurring, it creates the next recurrence.
     *
     * @param choreId The ID of the chore to mark as completed.
     */
    public void markChoreAsCompleted(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new IllegalArgumentException("Chore not found with id: " + choreId));

        chore.setStatus(ChoreStatus.COMPLETED);
        chore.setCompletedAt(LocalDate.now());
        choreRepository.save(chore);

        if(chore.getIsRecurring()){
            createNextRecurrence(chore);
        }
    }

    /**
     * Creates the next recurrence of a chore based on its recurrence interval.
     * If the chore is not recurring or has no recurrence interval, it does nothing.
     *
     * @param completedChore The completed chore for which to create the next recurrence.
     */
    public void createNextRecurrence(Chore completedChore) {
        if (!completedChore.getIsRecurring() || completedChore.getRecurrenceInterval() == null) {
            return; // No recurrence
        }

        LocalDate nextDate = calculateNextScheduledDate(completedChore.getScheduledDate(),
                completedChore.getRecurrenceInterval());


        Chore nextChore = new Chore();
        nextChore.setName(completedChore.getName());
        nextChore.setDescription(completedChore.getDescription());
        nextChore.setAssignedTo(completedChore.getAssignedTo());
        nextChore.setApartment(completedChore.getApartment());
        nextChore.setScheduledDate(nextDate);
        nextChore.setCreatedBy(completedChore.getCreatedBy());
        nextChore.setIsRecurring(completedChore.getIsRecurring());
        nextChore.setRecurrenceInterval(completedChore.getRecurrenceInterval());


        choreRepository.save(nextChore);
    }

    /**
     * Calculates the next scheduled date for a chore based on its recurrence interval.
     *
     * @param currentDate The current scheduled date of the chore.
     * @param interval    The recurrence interval (daily, weekly, monthly).
     * @return The next scheduled date for the chore.
     */
    private LocalDate calculateNextScheduledDate(LocalDate currentDate, RecurrenceInterval interval) {
        return switch (interval) {
            case DAILY -> currentDate.plusDays(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            default -> throw new IllegalArgumentException("Unsupported recurrence interval: " + interval);
        };
    }

    /**
     * Deletes a chore by its ID.
     * Throws an exception if the chore is not found.
     *
     * @param choreId The ID of the chore to delete.
     */
    public void deleteChore(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new IllegalArgumentException("Chore not found with id: " + choreId));


        choreRepository.delete(chore);
    }

    /**
     * Marks all overdue chores as overdue based on the current date.
     * Overdue chores are those that are pending and scheduled before today.
     */
    public void markOverdueChores() {
        LocalDate today = LocalDate.now();
        List<Chore> overdueChores = choreRepository.findByStatusAndScheduledDateBefore(ChoreStatus.PENDING, today);

        for (Chore chore : overdueChores) {
                chore.setStatus(ChoreStatus.OVERDUE);
        }

        choreRepository.saveAll(overdueChores);
    }

    /**
     * Updates an existing chore with new details.
     * Validates the current user has permission to update the chore.
     *
     * @param choreId        The ID of the chore to update.
     * @param name           The new name for the chore.
     * @param description    The new description for the chore.
     * @param scheduledDate  The new scheduled date for the chore.
     * @param currentUser    The user performing the update, used for permission checks.
     */
    public void updateChore(Long choreId, String name, String description,
                             LocalDate scheduledDate, UserInfo currentUser) {
        Chore chore = choreRepository.findById(choreId).orElseThrow(() -> new IllegalArgumentException("Chore not found with id: " + choreId));

        if(!currentUser.getResidence().getId().equals(chore.getApartment().getId())){
            throw new IllegalStateException("You do not have permission to update this chore");
        }

        chore.setName(name);
        chore.setDescription(description);
        chore.setScheduledDate(scheduledDate);

        choreRepository.save(chore);
    }
}
