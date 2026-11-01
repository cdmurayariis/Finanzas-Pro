package com.example.aplicacion_financiero.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.aplicacion_financiero.data.dao.BudgetDao;
import com.example.aplicacion_financiero.data.dao.CategoryDao;
import com.example.aplicacion_financiero.data.dao.SavingsGoalDao;
import com.example.aplicacion_financiero.data.dao.TransactionDao;
import com.example.aplicacion_financiero.data.database.FinanceDatabase;
import com.example.aplicacion_financiero.data.model.Budget;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.CategoryTotal;
import com.example.aplicacion_financiero.data.model.SavingsGoal;
import com.example.aplicacion_financiero.data.model.Transaction;

import java.util.List;

public class FinanceRepository {

    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final BudgetDao budgetDao;
    private final SavingsGoalDao savingsGoalDao;

    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<List<Category>> allCategories;
    private final LiveData<List<Budget>> allBudgets;
    private final LiveData<List<SavingsGoal>> allSavingsGoals;

    public FinanceRepository(Application application) {
        FinanceDatabase db = FinanceDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        categoryDao = db.categoryDao();
        budgetDao = db.budgetDao();
        savingsGoalDao = db.savingsGoalDao();

        allTransactions = transactionDao.getAllTransactions();
        allCategories = categoryDao.getAllCategories();
        allBudgets = budgetDao.getAllBudgets();
        allSavingsGoals = savingsGoalDao.getAllSavingsGoals();
    }

    public void insertTransaction(Transaction transaction) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> transactionDao.insert(transaction));
    }

    public void updateTransaction(Transaction transaction) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> transactionDao.update(transaction));
    }

    public void deleteTransaction(Transaction transaction) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> transactionDao.delete(transaction));
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getRecentTransactions(int limit) {
        return transactionDao.getRecentTransactions(limit);
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDates(long startDate, long endDate) {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate);
    }

    public LiveData<Long> getTotalIncome(long startDate, long endDate) {
        return transactionDao.observeTotalIncomeBetweenDates(startDate, endDate);
    }

    public LiveData<Long> getTotalExpense(long startDate, long endDate) {
        return transactionDao.observeTotalExpenseBetweenDates(startDate, endDate);
    }

    public LiveData<Long> getTotalBalance() {
        return transactionDao.observeTotalBalance();
    }

    public LiveData<Long> getBalanceBefore(long timestamp) {
        return transactionDao.observeBalanceBefore(timestamp);
    }

    public LiveData<List<CategoryTotal>> getExpenseTotalsByCategory(long startDate, long endDate) {
        return transactionDao.observeExpenseTotalsByCategory(startDate, endDate);
    }

    public LiveData<List<CategoryTotal>> getIncomeTotalsByCategory(long startDate, long endDate) {
        return transactionDao.observeIncomeTotalsByCategory(startDate, endDate);
    }

    public long getTotalIncomeBetweenDates(long startDate, long endDate) {
        return transactionDao.getTotalIncomeBetweenDates(startDate, endDate);
    }

    public long getTotalExpenseBetweenDates(long startDate, long endDate) {
        return transactionDao.getTotalExpenseBetweenDates(startDate, endDate);
    }

    public void insertCategory(Category category) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> categoryDao.insert(category));
    }

    public void insertCategory(Category category, Runnable onDone) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.insert(category);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void deleteCategory(Category category) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> categoryDao.delete(category));
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryDao.getCategoriesByType(type);
    }

    public LiveData<List<Category>> getCustomCategories() {
        return categoryDao.getCustomCategories();
    }

    public LiveData<List<Budget>> getBudgetsForMonth(int year, int month) {
        return budgetDao.getBudgetsForMonth(year, month);
    }

    public LiveData<Budget> getTotalBudget(int year, int month) {
        return budgetDao.observeBudgetForCategory(year, month, Budget.TOTAL_CATEGORY);
    }

    public LiveData<Long> getCategoryBudgetTotal(int year, int month) {
        return budgetDao.observeCategoryBudgetTotal(year, month, Budget.TOTAL_CATEGORY);
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public void saveTotalBudget(int year, int month, long amountInCents) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> {
            Budget existing = budgetDao.getBudgetForCategory(year, month, Budget.TOTAL_CATEGORY);
            if (amountInCents <= 0) {
                if (existing != null) {
                    budgetDao.delete(existing);
                }
                return;
            }
            if (existing == null) {
                budgetDao.insert(new Budget(year, month, amountInCents,
                        Budget.TOTAL_CATEGORY, amountInCents));
            } else {
                existing.setTotalBudgetInCents(amountInCents);
                existing.setCategoryBudgetInCents(amountInCents);
                budgetDao.update(existing);
            }
        });
    }

    public void saveCategoryBudget(int year, int month, String category, long amountInCents) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> {
            Budget existing = budgetDao.getBudgetForCategory(year, month, category);
            if (amountInCents <= 0) {
                if (existing != null) {
                    budgetDao.delete(existing);
                }
                return;
            }
            if (existing == null) {
                budgetDao.insert(new Budget(year, month, 0, category, amountInCents));
            } else {
                existing.setCategoryBudgetInCents(amountInCents);
                budgetDao.update(existing);
            }
        });
    }

    public void insertBudget(Budget budget) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> budgetDao.insert(budget));
    }

    public void updateBudget(Budget budget) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> budgetDao.update(budget));
    }

    public void deleteBudget(Budget budget) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> budgetDao.delete(budget));
    }

    public void insertSavingsGoal(SavingsGoal savingsGoal) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> savingsGoalDao.insert(savingsGoal));
    }

    public void updateSavingsGoal(SavingsGoal savingsGoal) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> savingsGoalDao.update(savingsGoal));
    }

    public void deleteSavingsGoal(SavingsGoal savingsGoal) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> savingsGoalDao.delete(savingsGoal));
    }

    public LiveData<List<SavingsGoal>> getAllSavingsGoals() {
        return allSavingsGoals;
    }

    public LiveData<Long> getTotalSaved() {
        return savingsGoalDao.observeTotalSaved();
    }

    public void clearUserData() {
        FinanceDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.deleteAllTransactions();
            budgetDao.deleteAllBudgets();
            savingsGoalDao.deleteAllSavingsGoals();
            categoryDao.deleteCustomCategories();
        });
    }
}
