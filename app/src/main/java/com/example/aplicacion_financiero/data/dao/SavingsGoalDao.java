package com.example.aplicacion_financiero.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aplicacion_financiero.data.model.SavingsGoal;

import java.util.List;

@Dao
public interface SavingsGoalDao {

    @Insert
    long insert(SavingsGoal savingsGoal);

    @Update
    void update(SavingsGoal savingsGoal);

    @Delete
    void delete(SavingsGoal savingsGoal);

    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    LiveData<List<SavingsGoal>> getAllSavingsGoals();

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    SavingsGoal getSavingsGoalById(long id);

    @Query("SELECT IFNULL(SUM(savedAmountInCents), 0) FROM savings_goals")
    LiveData<Long> observeTotalSaved();

    @Query("DELETE FROM savings_goals")
    void deleteAllSavingsGoals();
}
