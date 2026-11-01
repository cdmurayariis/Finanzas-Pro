package com.example.aplicacion_financiero.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.aplicacion_financiero.data.model.Budget;
import com.example.aplicacion_financiero.data.model.CategoryTotal;
import com.example.aplicacion_financiero.data.model.Period;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.data.repository.FinanceRepository;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final MutableLiveData<Period> selectedPeriod = new MutableLiveData<>();
    private final int firstDay;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        firstDay = PrefsManager.getFirstDayOfPeriod(application);
        long currentStart = DateUtils.getPeriodStartForDate(System.currentTimeMillis(), firstDay);
        selectedPeriod.setValue(new Period(DateUtils.getYear(currentStart),
                DateUtils.getMonth(currentStart)));
    }

    public LiveData<Period> getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setPeriod(Period period) {
        if (period != null) {
            selectedPeriod.setValue(period);
        }
    }

    private long start(Period period) {
        return DateUtils.getPeriodStart(period.getYear(), period.getMonth(), firstDay);
    }

    private long end(Period period) {
        return DateUtils.getPeriodEnd(period.getYear(), period.getMonth(), firstDay);
    }

    public LiveData<Long> getTotalIncome() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getTotalIncome(start(period), end(period)));
    }

    public LiveData<Long> getTotalExpense() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getTotalExpense(start(period), end(period)));
    }

    public LiveData<List<CategoryTotal>> getExpensesByCategory() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getExpenseTotalsByCategory(start(period), end(period)));
    }

    public LiveData<Budget> getTotalBudget() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getTotalBudget(period.getYear(), period.getMonth()));
    }

    public LiveData<Long> getCategoryBudgetTotal() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getCategoryBudgetTotal(period.getYear(), period.getMonth()));
    }

    public LiveData<Long> getTotalBalance() {
        return repository.getTotalBalance();
    }

    public LiveData<Long> getTotalSaved() {
        return repository.getTotalSaved();
    }

    public LiveData<List<Transaction>> getRecentTransactions() {
        return repository.getRecentTransactions(6);
    }

    public void insertTransaction(Transaction transaction) {
        repository.insertTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }
}
