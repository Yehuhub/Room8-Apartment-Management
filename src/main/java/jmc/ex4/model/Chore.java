package jmc.ex4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jmc.ex4.model.enums.ChoreStatus;
import jmc.ex4.model.enums.RecurrenceInterval;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a chore assigned to a user in an apartment.
 * Chores can be one-time or recurring, and have various statuses.
 */
@Getter
@Setter
@Entity
public class Chore implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotEmpty(message = "Chore name is required")
  private String name;

  private String description;

  @NotNull(message = "Chore must be assigned to a user")
  @ManyToOne(optional = false)
  @JoinColumn(name = "assigned_to_id", nullable = false)
  private UserInfo assignedTo;

  @ManyToOne(optional = false)
  @JoinColumn(name = "apartment_id", nullable = false)
  private Apartment apartment;

  @NotNull(message = "Scheduled date is required")
  @Column(nullable = false)
  private LocalDate scheduledDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChoreStatus status = ChoreStatus.PENDING;

  @ManyToOne(optional = false)
  @JoinColumn(name = "created_by", nullable = false)
  private UserInfo createdBy;

  @Column(name = "completed_at")
  private LocalDate completedAt;

  // Recurring chore fields
  @Column(name = "is_recurring", nullable = false)
  private Boolean isRecurring = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "recurrence_interval")
  private RecurrenceInterval recurrenceInterval;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Chore() {}

  public Chore(String name, UserInfo assignedTo, LocalDate scheduledDate, UserInfo createdBy, Apartment apartment) {
    this.name = name;
    this.assignedTo = assignedTo;
    this.scheduledDate = scheduledDate;
    this.createdBy = createdBy;
    this.apartment = apartment;
  }

  public boolean isOverdue() {
    return status == ChoreStatus.OVERDUE;
  }
  
}