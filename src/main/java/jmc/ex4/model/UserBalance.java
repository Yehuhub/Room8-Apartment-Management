package jmc.ex4.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents a user's balance in the system.
 * Contains user information and the amount of balance.
 */
@Data
@AllArgsConstructor
public class UserBalance implements Serializable {
    private UserInfo user;
    private BigDecimal amount;

}
