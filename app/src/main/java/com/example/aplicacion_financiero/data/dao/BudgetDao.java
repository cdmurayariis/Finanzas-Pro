package com.example.aplicacion_financiero.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aplicacion_financiero.data.model.Budget;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    LiveData<List<Budget>> getBudgetsForMonth(int year, int month);

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    List<Budget> getBudgetsForMonthSync(int year, int month);

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND category = :category LIMIT 1")
    Budget getBudgetForCategory(int year, int month, String category);

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND category = :category LIMIT 1")
    LiveData<Budget> observeBudgetForCategory(int year, int month, String category);

    @Query("SELECT COALESCE(SUM(categoryBudgetInCents), 0) FROM budgets "
            + "WHERE year = :year AND month = :month AND category <> :excludedCategory")
    LiveData<Long> observeCategoryBudgetTotal(int year, int month, String excludedCategory);

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    LiveData<List<Budget>> getAllBudgets();

    @Query("DELETE FROM budgets WHERE year = :year AND month = :month AND category = :category")
    void deleteBudgetForCategory(int year, int month, String category);

    @Query("DELETE FROM budgets")
    void deleteAllBudgets();
}
