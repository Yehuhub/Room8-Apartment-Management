package jmc.ex4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents an expense in the system.
 * Each expense is associated with an apartment and a payer.
 * It includes a description, total amount, timestamp, and participants.
 */
@Setter
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Expense implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @NotNull
    private Apartment apartment;

    @ManyToOne(optional = false)
    @NotNull
    private UserInfo payer;

    @NotEmpty
    private String description;

    @Positive
    @NotNull
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Split> participants;

    @NotNull
    private Boolean settled = false;

}
