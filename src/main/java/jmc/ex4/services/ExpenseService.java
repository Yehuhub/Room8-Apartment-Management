package jmc.ex4.services;

import jmc.ex4.dto.ExpenseDto;
import jmc.ex4.model.Apartment;
import jmc.ex4.model.Expense;
import jmc.ex4.model.Split;
import jmc.ex4.model.UserInfo;
import jmc.ex4.repositories.ExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for managing expenses in an apartment.
 * Handles adding, deleting, and retrieving expenses and their participants.
 */
@Service
@AllArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final ApartmentService apartmentService;

    /**
     * Adds a new expense to the system.
     * The expense is shared equally among all tenants of the apartment.
     *
     * @param expenseDto the DTO containing expense details
     */
    @Transactional
    public void addExpense(ExpenseDto expenseDto) {

        Expense expense = new Expense();
        expense.setDescription(expenseDto.getDescription());
        expense.setTotalAmount(expenseDto.getTotalAmount());

        UserInfo payer = userService.getUserById(expenseDto.getPayerId());
        Apartment apartment = apartmentService.getApartmentById(expenseDto.getApartmentId());
        expense.setPayer(payer);
        expense.setApartment(apartment);

        BigDecimal shareAmount = expense.getTotalAmount().divide(BigDecimal.valueOf(apartment.getTenants().size()), RoundingMode.HALF_UP);

        var tenants = apartment.getTenants();
        Set<Split> allSplits = new HashSet<>();
        for (UserInfo tenant : tenants) {
            Split split = new Split();
            split.setExpense(expense);
            split.setUser(tenant);
            split.setAmount(shareAmount);
            allSplits.add(split);
        }
        expense.setParticipants(allSplits);


        expenseRepository.save(expense);
    }

    /**
     * Deletes an expense by its ID.
     * Throws an exception if the expense does not exist.
     *
     * @param expenseId the ID of the expense to delete
     */
    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        expenseRepository.delete(expense);
    }

    /**
     * Retrieves an expense by its ID.
     * Throws an exception if the expense does not exist.
     *
     * @param expenseId the ID of the expense to retrieve
     * @return the Expense object
     */
    public Expense getExpenseById(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
    }

    /**
     * Retrieves all expenses for a specific apartment that are not settled.
     *
     * @param apartmentId the ID of the apartment
     * @return a list of expenses for the specified apartment
     */
    public List<Expense> getAllExpensesOfApartment(Long apartmentId) {
        return expenseRepository.findAllByApartmentIdAndSettledFalse(apartmentId); // list of expenses for a specific apartment, can return empty list if no expenses found
    }

    /**
     * Retrieves all participants of a specific expense.
     *
     * @param expenseId the ID of the expense
     * @return a list of Split objects representing the participants
     */
    @Transactional
    public List<Split> getExpenseParticipants(Long expenseId) {
        Expense expense = getExpenseById(expenseId);
        return List.copyOf(expense.getParticipants());
    }
}
