package com.example.aplicacion_financiero.ui.budget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.aplicacion_financiero.data.model.Budget;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.CategoryTotal;
import com.example.aplicacion_financiero.data.model.Period;
import com.example.aplicacion_financiero.data.repository.FinanceRepository;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final MutableLiveData<Period> selectedPeriod = new MutableLiveData<>();
    private final int firstDay;

    public BudgetViewModel(@NonNull Application application) {
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

    public void previousMonth() {
        Period period = selectedPeriod.getValue();
        if (period != null) {
            selectedPeriod.setValue(period.previous());
        }
    }

    public void nextMonth() {
        Period period = selectedPeriod.getValue();
        if (period != null) {
            selectedPeriod.setValue(period.next());
        }
    }

    private long start(Period period) {
        return DateUtils.getPeriodStart(period.getYear(), period.getMonth(), firstDay);
    }

    private long end(Period period) {
        return DateUtils.getPeriodEnd(period.getYear(), period.getMonth(), firstDay);
    }

    public LiveData<List<Budget>> getBudgets() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getBudgetsForMonth(period.getYear(), period.getMonth()));
    }

    public LiveData<Budget> getTotalBudget() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getTotalBudget(period.getYear(), period.getMonth()));
    }

    public LiveData<Long> getTotalExpense() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getTotalExpense(start(period), end(period)));
    }

    public LiveData<List<CategoryTotal>> getExpensesByCategory() {
        return Transformations.switchMap(selectedPeriod,
                period -> repository.getExpenseTotalsByCategory(start(period), end(period)));
    }

    public LiveData<List<Category>> getExpenseCategories() {
        return repository.getCategoriesByType("EXPENSE");
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return repository.getAllBudgets();
    }

    public void saveTotalBudget(Period period, long amount) {
        repository.saveTotalBudget(period.getYear(), period.getMonth(), amount);
    }

    public void saveCategoryBudget(Period period, String category, long amount) {
        repository.saveCategoryBudget(period.getYear(), period.getMonth(), category, amount);
    }
}
