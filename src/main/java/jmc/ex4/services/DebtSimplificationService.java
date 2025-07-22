package jmc.ex4.services;

import jmc.ex4.dto.TransactionDto;
import jmc.ex4.model.*;
import jmc.ex4.model.enums.Role;
import jmc.ex4.repositories.ExpenseRepository;
import jmc.ex4.repositories.TransactionRepository;
import jmc.ex4.repositories.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for simplifying debts among users in an apartment.
 * It calculates balances, minimizes transactions, and provides methods to get debts for specific users.
 */
@Service
@AllArgsConstructor
public class DebtSimplificationService {

    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;
    private final UsersRepository usersRepository;

    /**
     * Calculates the debts among users in a specific apartment.
     * It retrieves unsettled expenses and existing transactions, calculates balances,
     * and minimizes the transactions to simplify debts.
     *
     * @param apartmentId the ID of the apartment
     * @return a list of simplified transactions representing debts
     */
    @Transactional
    public List<Transaction> calculateDebts(Long apartmentId) {
        Map<UserInfo, BigDecimal> balances = calculateBalances(apartmentId);
        return minimizeTransactions(balances);
    }

    /**
     * Retrieves debts for a specific user in their apartment.
     * It filters the transactions to find those where the user is the debtor.
     *
     * @param user the user for whom to retrieve debts
     * @return a list of transactions representing debts for the user
     */
    @Transactional
    public List<Transaction> getDebtsForUser(UserInfo user) {
        if(user.getResidence() == null) {
            throw new IllegalArgumentException("User does not belong to any apartment");
        }

        Long apartmentId = user.getResidence().getId();
        Long userId = user.getId();

        return calculateDebts(apartmentId).stream()
                .filter(transaction -> transaction.getFromUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves debts owed to a specific user in their apartment.
     * It filters the transactions to find those where the user is the creditor.
     *
     * @param user the user for whom to retrieve debts owed to them
     * @return a list of transactions representing debts owed to the user
     */
    @Transactional
    public List<Transaction> getDebtsToUser(UserInfo user){
        if(user.getResidence() == null) {
            throw new IllegalArgumentException("User does not belong to any apartment");
        }

        Long apartmentId = user.getResidence().getId();
        Long userId = user.getId();

        return calculateDebts(apartmentId).stream()
                .filter(transaction -> transaction.getToUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the balances of all users in an apartment.
     * It sums up the amounts owed by each user based on unsettled expenses and transactions.
     *
     * @param apartmentId the ID of the apartment
     * @return a map where keys are users and values are their respective balances
     */
    @Transactional(readOnly = true)
    public Map<UserInfo, BigDecimal> calculateBalances(Long apartmentId){
        List<Expense> unsettledExpenses = expenseRepository.findAllByApartmentIdAndSettledFalse(apartmentId);
        List<Transaction> transactionList = transactionRepository.findAllByApartmentId(apartmentId);

        Map<UserInfo, BigDecimal> balances = new HashMap<>();

        for (Expense expense : unsettledExpenses) {
            UserInfo payer = expense.getPayer();

            balances.put(payer, balances.getOrDefault(payer, BigDecimal.ZERO).add(expense.getTotalAmount()));


            for (Split split : expense.getParticipants()) {
                UserInfo participant = split.getUser();
                BigDecimal amount = split.getAmount();
                balances.put(participant, balances.getOrDefault(participant, BigDecimal.ZERO).subtract(amount));

            }
        }

        for(Transaction transaction: transactionList){
            UserInfo fromUser = transaction.getFromUser();
            UserInfo toUser = transaction.getToUser();
            BigDecimal amount = transaction.getAmount();

            balances.put(fromUser, balances.getOrDefault(fromUser, BigDecimal.ZERO).add(amount));
            balances.put(toUser, balances.getOrDefault(toUser, BigDecimal.ZERO).subtract(amount));
        }

        return balances;
    }

    /**
     * Creates a list of creditors from the balances map.
     * Creditors are users with a positive balance.
     *
     * @param balances a map of user balances
     * @return a sorted list of UserBalance objects representing creditors
     */
    private List<UserBalance> createCreditorsList(Map<UserInfo, BigDecimal> balances) {
        return balances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> new UserBalance(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    int amountComparison = b.getAmount().compareTo(a.getAmount()); // Descending by amount
                    if (amountComparison != 0) {
                        return amountComparison;
                    }
                    // If amounts are equal, sort by user ID for deterministic ordering
                    return a.getUser().getId().compareTo(b.getUser().getId());
                })
                .collect(Collectors.toList());
    }

    /**
     * Creates a list of debtors (users who owe money) sorted by the amount they owe.
     * It filters out users with a balance greater than or equal to zero.
     *
     * @param balances a map of user balances
     * @return a list of debtors sorted by the amount owed
     */
    private List<UserBalance> createDebtorsList(Map<UserInfo, BigDecimal> balances) {
        return balances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .map(entry -> new UserBalance(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    int amountComparison = a.getAmount().compareTo(b.getAmount()); // Ascending by amount (most negative first)
                    if (amountComparison != 0) {
                        return amountComparison;
                    }
                    // If amounts are equal, sort by user ID for deterministic ordering
                    return a.getUser().getId().compareTo(b.getUser().getId());
                })
                .collect(Collectors.toList());
    }

    /**
     * Minimizes the transactions by settling debts between creditors and debtors.
     * It creates a list of transactions that represent the simplified debts.
     *
     * @param balances a map of user balances
     * @return a list of minimized transactions
     */
    private List<Transaction> minimizeTransactions(Map<UserInfo, BigDecimal> balances){
        List<UserBalance> creditors = createCreditorsList(balances);
        List<UserBalance> debtors = createDebtorsList(balances);

        List<Transaction> transactions = new ArrayList<>();
        BigDecimal EPSILON = BigDecimal.valueOf(0.001); // Smaller epsilon for better precision

        int creditorIndex = 0;
        int debtorIndex = 0;

        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            UserBalance creditor = creditors.get(creditorIndex);
            UserBalance debtor = debtors.get(debtorIndex);

            BigDecimal credit = creditor.getAmount();
            BigDecimal debt = debtor.getAmount().abs(); // Convert to positive for comparison

            BigDecimal settledAmount = credit.min(debt);

            if (settledAmount.compareTo(EPSILON) > 0) {
                Transaction transaction = new Transaction();
                transaction.setFromUser(debtor.getUser());
                transaction.setToUser(creditor.getUser());
                transaction.setAmount(settledAmount);
                transaction.setApartment(debtor.getUser().getResidence());
                transactions.add(transaction);
            }

            BigDecimal newCredit = credit.subtract(settledAmount);
            BigDecimal newDebt = debt.subtract(settledAmount);

            // Update the creditor's amount
            creditor.setAmount(newCredit);

            // Update the debtor's amount (keep it negative)
            debtor.setAmount(newDebt.negate());

            // Move to next creditor if current one is settled
            if (newCredit.compareTo(EPSILON) <= 0) {
                creditorIndex++;
            }

            // Move to next debtor if current one is settled
            if (newDebt.compareTo(EPSILON) <= 0) {
                debtorIndex++;
            }
        }

        return transactions;
    }

    /**
     * Settles a debt between two users by creating a transaction.
     * It checks if the transaction amount is valid and updates the balances accordingly.
     *
     * @param transactionDto the DTO containing transaction details
     */
    @Transactional
    public void settleDebt(TransactionDto transactionDto){
        UserInfo from = usersRepository.findByIdAndRole(transactionDto.getFromUserId(), Role.TENANT).orElseThrow(() -> new IllegalArgumentException("User not found or not a tenant."));
        UserInfo to = usersRepository.findByIdAndRole(transactionDto.getToUserId(), Role.TENANT).orElseThrow(() -> new IllegalArgumentException("User not found or not a tenant."));
        BigDecimal owed = calculateRemainingOwed(from, to);
        BigDecimal amount = transactionDto.getAmount();

        if(amount.compareTo(owed) > 0) {
            System.out.println(amount + " > " + owed);
            throw new IllegalArgumentException("Transaction amount exceeds the owed amount");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        Transaction t = new Transaction();
        t.setFromUser(from);
        t.setToUser(to);
        t.setAmount(amount);
        t.setApartment(from.getResidence());
        t.setPaidAt(LocalDateTime.now());

        transactionRepository.save(t);
    }

    /**
     * Calculates the remaining amount owed between two users.
     * It retrieves the balances and minimizes the transactions to find the specific debt.
     *
     * @param from the user who owes money
     * @param to the user who is owed money
     * @return the remaining amount owed
     */
    @Transactional
    public BigDecimal calculateRemainingOwed(UserInfo from, UserInfo to) {
        Map<UserInfo, BigDecimal> balances = calculateBalances(from.getResidence().getId());
        List<Transaction> minimizedTransactions = minimizeTransactions(balances);

        return minimizedTransactions.stream()
                .filter(transaction -> transaction.getFromUser().equals(from) &&
                        transaction.getToUser().equals(to))
                .findFirst()
                .map(Transaction::getAmount)
                .orElse(BigDecimal.ZERO);
    }

}
