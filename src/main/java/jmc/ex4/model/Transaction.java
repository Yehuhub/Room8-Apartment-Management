package jmc.ex4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a transaction between two users for an apartment.
 * Contains details about the transaction such as the amount, the users involved, and the apartment.
 */
@Getter
@Setter
@Entity
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserInfo fromUser;
    @ManyToOne(optional = false)
    private UserInfo toUser;

    @NotNull
    private BigDecimal amount;

    @ManyToOne(optional = false)
    private Apartment apartment;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime paidAt;

}
