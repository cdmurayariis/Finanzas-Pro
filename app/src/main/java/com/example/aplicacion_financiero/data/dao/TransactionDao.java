package com.example.aplicacion_financiero.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aplicacion_financiero.data.model.CategoryTotal;
import com.example.aplicacion_financiero.data.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(long id);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(long startDate, long endDate);

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentTransactions(int limit);

    @Query("SELECT IFNULL(SUM(amountInCents), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    LiveData<Long> observeTotalIncomeBetweenDates(long startDate, long endDate);

    @Query("SELECT IFNULL(SUM(amountInCents), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    LiveData<Long> observeTotalExpenseBetweenDates(long startDate, long endDate);

    @Query("SELECT IFNULL(SUM(CASE WHEN type = 'INCOME' THEN amountInCents ELSE -amountInCents END), 0) FROM transactions")
    LiveData<Long> observeTotalBalance();

    @Query("SELECT IFNULL(SUM(CASE WHEN type = 'INCOME' THEN amountInCents ELSE -amountInCents END), 0) FROM transactions WHERE date < :timestamp")
    LiveData<Long> observeBalanceBefore(long timestamp);

    @Query("SELECT category AS category, SUM(amountInCents) AS total FROM transactions " +
            "WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> observeExpenseTotalsByCategory(long startDate, long endDate);

    @Query("SELECT category AS category, SUM(amountInCents) AS total FROM transactions " +
            "WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> observeIncomeTotalsByCategory(long startDate, long endDate);

    @Query("SELECT IFNULL(SUM(amountInCents), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    long getTotalIncomeBetweenDates(long startDate, long endDate);

    @Query("SELECT IFNULL(SUM(amountInCents), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    long getTotalExpenseBetweenDates(long startDate, long endDate);

    @Query("DELETE FROM transactions")
    void deleteAllTransactions();
}
