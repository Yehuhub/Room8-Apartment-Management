package jmc.ex4.controllers;

import jakarta.validation.Valid;
import jmc.ex4.dto.ExpenseDto;
import jmc.ex4.dto.TransactionDto;
import jmc.ex4.model.Expense;
import jmc.ex4.model.Split;
import jmc.ex4.model.Transaction;
import jmc.ex4.model.UserInfo;
import jmc.ex4.services.DebtSimplificationService;
import jmc.ex4.services.ExpenseService;
import jmc.ex4.services.UserService;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for handling tenant expenses page and related operations.
 * Provides endpoints for viewing, adding expenses, and settling debts.
 */
@Controller
@RequestMapping("/tenant/expenses-page")
@AllArgsConstructor
public class ExpenseController {

    private final CurrentUserProvider currentUserProvider;
    private final DebtSimplificationService debtSimplificationService;
    private final ExpenseService expenseService;
    private final UserService userService;


    /**
     * Displays the tenant expenses page.
     * If the user has no assigned residence, redirects to a page for creating or joining an apartment.
     *
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping
    public String tenantExpenses(Model model){
        UserInfo user = currentUserProvider.getCurrentUser();
        if(user.getResidence() == null){
            return "redirect:/tenant/no-apartment-tenant"; // Redirect to a page indicating no apartment is assigned where user can create/join one
        }

        model.addAttribute("expenseDto", new ExpenseDto());
        model.addAttribute("transactionDto", new TransactionDto());
        populateExpensePage(model, user);
        return "tenant/expenses-page";
    }

    /**
     * Handles the addition of a new expense.
     * Validates the expense data and adds it to the system if valid.
     * If there are validation errors, redisplays the expenses page with an error message.
     *
     * @param expenseDto the DTO containing expense data
     * @param result     the binding result for validation errors
     * @param model      the model to add attributes for the view
     * @return redirect to the expenses page or redisplay with errors
     */
    @PostMapping("/add")
    public String addExpense(@Valid @ModelAttribute("expenseDto") ExpenseDto expenseDto,
                             BindingResult result,
                             Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            populateExpensePage(model, user);
            model.addAttribute("showAddExpenseModal", true);
            return "/tenant/expenses-page";
        }
        expenseService.addExpense(expenseDto);
        return "redirect:/tenant/expenses-page";
    }

    /**
     * Handles the settlement of a debt.
     * Validates the transaction data and processes the debt settlement if valid.
     * If there are validation errors, redisplays the expenses page with an error message.
     *
     * @param transactionDto the DTO containing transaction data
     * @param result         the binding result for validation errors
     * @param model          the model to add attributes for the view
     * @return redirect to the expenses page or redisplay with errors
     */
    @PostMapping("/settle")
    public String settleDebt(@Valid @ModelAttribute("transactionDto") TransactionDto transactionDto,
                             BindingResult result, Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            return handleSettleDebtError(transactionDto, user, model);
        }
        try {
            debtSimplificationService.settleDebt(transactionDto);
        } catch (IllegalArgumentException iae) {
            result.rejectValue("amount", "transaction.amount.exceeded", iae.getMessage() + ". Please enter a valid amount.");

            return handleSettleDebtError(transactionDto, user, model);
        }
        return "redirect:/tenant/expenses-page";
    }

    /**
     * Handles errors that occur during debt settlement.
     * Prepares the model with necessary attributes to redisplay the expenses page with an error message.
     *
     * @param transactionDto the DTO containing transaction data
     * @param user           the current user
     * @param model          the model to add attributes for the view
     * @return the name of the view to render
     */
    private String handleSettleDebtError(TransactionDto transactionDto, UserInfo user, Model model) {
        model.addAttribute("transactionDto", transactionDto);
        model.addAttribute("expenseDto", new ExpenseDto());
        populateExpensePage(model, user);
        model.addAttribute("showSettleDebtModal", true);
        model.addAttribute("toUserFullName", userService.getUserById(transactionDto.getToUserId()).getFirstName());

        return "/tenant/expenses-page";
    }

    /**
     * Populates the model with data for the expenses page.
     * Retrieves debts for the user, debts to the user, and all expenses of the user's apartment.
     * Groups expenses by month and calculates user shares in each expense.
     *
     * @param model the model to add attributes for the view
     * @param user  the current user
     */
    private void populateExpensePage(Model model,UserInfo user){

        List<Transaction> debtsForUser = debtSimplificationService.getDebtsForUser(user);
        List<Transaction> debtsToUser = debtSimplificationService.getDebtsToUser(user);

        // list of all expenses sorted by date last to first
        List<Expense> apartmentExpenses = expenseService.getAllExpensesOfApartment(user.getResidence().getId()).stream()
                .sorted(Comparator.comparing(Expense::getTimestamp).reversed())
                .toList();

        Map<YearMonth, List<Expense>> groupedExpenses = apartmentExpenses.stream()
                .collect(Collectors.groupingBy(expense ->
                        YearMonth.from(expense.getTimestamp())));

        Map<Long, BigDecimal> userShares = new HashMap<>();
        for (Expense expense : apartmentExpenses) {
            // Check if user is a participant in this expense
            Optional<Split> userParticipation = expenseService.getExpenseParticipants(expense.getId()).stream()
                    .filter(split -> split.getUser().getId().equals(user.getId()))
                    .findFirst();

            if (userParticipation.isPresent()) {
                // User is involved - store their share amount
                userShares.put(expense.getId(), userParticipation.get().getAmount());
            } else {
                // User is not involved - explicitly set to zero
                userShares.put(expense.getId(), BigDecimal.ZERO);
            }
        }

        model.addAttribute("groupedExpenses", groupedExpenses);
        model.addAttribute("userShares", userShares);
        model.addAttribute("user", user);
        model.addAttribute("debtsForUser", debtsForUser);
        model.addAttribute("debtsToUser", debtsToUser);
    }
}
