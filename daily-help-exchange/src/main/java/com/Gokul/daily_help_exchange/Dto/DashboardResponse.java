package com.Gokul.daily_help_exchange.Dto;

public record DashboardResponse(
        long totalTask,
        long openTask,
        long claimedTask,
        long completedTask,
        long totalRewardPoints
){  
}