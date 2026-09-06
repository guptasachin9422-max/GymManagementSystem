package com.example.GymManagementSystem.entity;

import java.util.Arrays;

/**
 * The canonical membership plans used by the application.
 *
 * <p>The legacy aliases are intentionally accepted during reads and writes so
 * existing records can be migrated without losing members or payments.</p>
 */
public enum MembershipPlan {
    MONTHLY(1, 1200d),
    QUARTERLY(3, 2500d),
    YEARLY(12, 6000d);

    private final int durationMonths;
    private final double price;

    MembershipPlan(int durationMonths, double price) {
        this.durationMonths = durationMonths;
        this.price = price;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public double getPrice() {
        return price;
    }

    public static MembershipPlan from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Membership plan is required");
        }

        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "BASIC", "MONTH", "MONTHLY", "1 MONTH", "1_MONTH" -> MONTHLY;
            case "PREMIUM", "QUARTER", "QUARTERLY", "3 MONTHS", "3_MONTHS" -> QUARTERLY;
            case "VIP", "YEAR", "YEARLY", "ANNUAL", "12 MONTHS", "12_MONTHS" -> YEARLY;
            default -> Arrays.stream(values())
                    .filter(plan -> plan.name().equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unsupported membership plan: " + value));
        };
    }
}
