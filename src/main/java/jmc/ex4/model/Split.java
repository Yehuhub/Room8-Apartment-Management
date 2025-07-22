package jmc.ex4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a split of an expense among users.
 * Each split indicates how much a user owes for a specific expense.
 */
@Getter
@Setter
@Entity
public class Split implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
