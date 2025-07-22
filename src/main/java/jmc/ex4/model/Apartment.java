package jmc.ex4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an apartment entity with its details and relationships.
 * This class is part of the JMC Ex4 project.
 * It includes fields for the apartment's address, owner, tenants, expenses, and a unique reference code.
 * The class uses Lombok annotations for boilerplate code reduction.
 */
@Setter
@Getter
@Entity
public class Apartment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique=true)
    private String address;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserInfo owner;

    @OneToMany(mappedBy = "residence", cascade = CascadeType.ALL)
    private List<UserInfo> tenants = new ArrayList<>();

    @OneToMany(mappedBy = "apartment")
    private List<Expense> expenses = new ArrayList<>();

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String referenceCode;

}
