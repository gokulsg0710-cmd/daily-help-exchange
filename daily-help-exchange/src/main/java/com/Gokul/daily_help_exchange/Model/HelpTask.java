package com.Gokul.daily_help_exchange.Model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "help_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank(message = "Task creator name is required")
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "claimed_by", length = 100)
    private String claimedBy;

    @Column(name = "claimed_by_user_id")
    private Long claimedByUserId;

    @NotNull(message = "Due date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Min(
            value = 1,
            message = "Priority must be between 1 and 5"
    )
    @Max(
            value = 5,
            message = "Priority must be between 1 and 5"
    )
    @Column(name = "priority", nullable = false)
    private int priority;

    @PositiveOrZero(
            message = "Reward points cannot be negative"
    )
    @Column(name = "reward_points", nullable = false)
    private int rewardPoints;

    /*
     * New tasks are always created as OPEN.
     *
     * No @NotNull validation here because the status
     * is automatically assigned by HelpTaskService.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;
}