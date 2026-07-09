package com.ims.backend.entity.enums;

public enum PremiumFrequency {
    MONTHLY(1), QUARTERLY(3), YEARLY(12);

    private final int months;

    PremiumFrequency(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}
