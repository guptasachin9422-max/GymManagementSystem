package com.example.GymManagementSystem.dto;

import com.example.GymManagementSystem.entity.MembershipPlan;

public record MembershipPlanResponse(
        String name,
        String duration,
        int durationMonths,
        double price
) {
    public static MembershipPlanResponse from(MembershipPlan plan) {
        String duration = plan.getDurationMonths() == 1
                ? "1 Month"
                : plan.getDurationMonths() + " Months";
        return new MembershipPlanResponse(
                plan.name(),
                duration,
                plan.getDurationMonths(),
                plan.getPrice());
    }
}
