package com.Gokul.daily_help_exchange.Dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateTaskRequest(

        @NotBlank(message = "Task title is required")
        String title,

        String description,

        @NotNull(message = "Due date is required")
        @FutureOrPresent(message = "Due date cannot be in the past")
        LocalDate dueDate,

        @Min(value = 1, message = "Priority must be between 1 and 5")
        @Max(value = 5, message = "Priority must be between 1 and 5")
        int priority,

        @PositiveOrZero(message = "Reward points cannot be negative")
        int rewardPoints
) {
}