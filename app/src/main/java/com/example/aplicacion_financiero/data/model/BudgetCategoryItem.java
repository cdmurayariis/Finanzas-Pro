package com.example.aplicacion_financiero.data.model;

public class BudgetCategoryItem {

    private final String category;
    private final long limitInCents;
    private final long spentInCents;

    public BudgetCategoryItem(String category, long limitInCents, long spentInCents) {
        this.category = category;
        this.limitInCents = limitInCents;
        this.spentInCents = spentInCents;
    }

    public String getCategory() {
        return category;
    }

    public long getLimitInCents() {
        return limitInCents;
    }

    public long getSpentInCents() {
        return spentInCents;
    }

    public int getPercent() {
        if (limitInCents <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(spentInCents * 100.0 / limitInCents));
    }
}
