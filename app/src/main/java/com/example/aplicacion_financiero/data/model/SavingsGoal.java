package com.example.aplicacion_financiero.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "savings_goals")
public class SavingsGoal {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private long targetAmountInCents;
    private long savedAmountInCents;
    private Long deadline; // timestamp, nullable
    private long createdAt;
    
    public SavingsGoal(String name, long targetAmountInCents, long savedAmountInCents, Long deadline) {
        this.name = name;
        this.targetAmountInCents = targetAmountInCents;
        this.savedAmountInCents = savedAmountInCents;
        this.deadline = deadline;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getTargetAmountInCents() {
        return targetAmountInCents;
    }
    
    public void setTargetAmountInCents(long targetAmountInCents) {
        this.targetAmountInCents = targetAmountInCents;
    }
    
    public long getSavedAmountInCents() {
        return savedAmountInCents;
    }
    
    public void setSavedAmountInCents(long savedAmountInCents) {
        this.savedAmountInCents = savedAmountInCents;
    }
    
    public Long getDeadline() {
        return deadline;
    }
    
    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public float getProgressPercentage() {
        if (targetAmountInCents <= 0) return 0;
        return (savedAmountInCents * 100f) / targetAmountInCents;
    }
}
