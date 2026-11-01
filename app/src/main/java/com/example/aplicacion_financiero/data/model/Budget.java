package com.example.aplicacion_financiero.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {

    public static final String TOTAL_CATEGORY = "__TOTAL__";

    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private int year;
    private int month;
    private long totalBudgetInCents;
    private String category;
    private long categoryBudgetInCents;
    
    public Budget(int year, int month, long totalBudgetInCents, String category, long categoryBudgetInCents) {
        this.year = year;
        this.month = month;
        this.totalBudgetInCents = totalBudgetInCents;
        this.category = category;
        this.categoryBudgetInCents = categoryBudgetInCents;
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public long getTotalBudgetInCents() {
        return totalBudgetInCents;
    }
    
    public void setTotalBudgetInCents(long totalBudgetInCents) {
        this.totalBudgetInCents = totalBudgetInCents;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public long getCategoryBudgetInCents() {
        return categoryBudgetInCents;
    }
    
    public void setCategoryBudgetInCents(long categoryBudgetInCents) {
        this.categoryBudgetInCents = categoryBudgetInCents;
    }
}
