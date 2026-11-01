package com.example.aplicacion_financiero.ui.savings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aplicacion_financiero.data.model.SavingsGoal;
import com.example.aplicacion_financiero.data.repository.FinanceRepository;

import java.util.List;

public class SavingsViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final LiveData<List<SavingsGoal>> goals;

    public SavingsViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        goals = repository.getAllSavingsGoals();
    }

    public LiveData<List<SavingsGoal>> getGoals() {
        return goals;
    }

    public LiveData<Long> getTotalSaved() {
        return repository.getTotalSaved();
    }

    public void insertGoal(SavingsGoal goal) {
        repository.insertSavingsGoal(goal);
    }

    public void updateGoal(SavingsGoal goal) {
        repository.updateSavingsGoal(goal);
    }

    public void deleteGoal(SavingsGoal goal) {
        repository.deleteSavingsGoal(goal);
    }

    public void addContribution(SavingsGoal goal, long amountInCents) {
        long newSaved = Math.min(goal.getTargetAmountInCents(),
                goal.getSavedAmountInCents() + amountInCents);
        goal.setSavedAmountInCents(newSaved);
        repository.updateSavingsGoal(goal);
    }
}
