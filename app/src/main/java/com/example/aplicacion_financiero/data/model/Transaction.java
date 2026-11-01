package com.example.aplicacion_financiero.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String type; // "INCOME" or "EXPENSE"
    private long amountInCents; // Store as cents to avoid floating point issues
    private String category;
    private String description;
    private long date; // timestamp in milliseconds
    private String paymentMethod; // "CASH", "CARD", "OTHER"
    private String note;
    
    public Transaction(String type, long amountInCents, String category, String description, 
                       long date, String paymentMethod, String note) {
        this.type = type;
        this.amountInCents = amountInCents;
        this.category = category;
        this.description = description;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getAmountInCents() {
        return amountInCents;
    }
    
    public void setAmountInCents(long amountInCents) {
        this.amountInCents = amountInCents;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}
