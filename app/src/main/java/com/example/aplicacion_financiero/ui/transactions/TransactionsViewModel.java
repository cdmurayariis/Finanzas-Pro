package com.example.aplicacion_financiero.ui.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.data.repository.FinanceRepository;

import java.util.List;

public class TransactionsViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final LiveData<List<Transaction>> allTransactions;

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        allTransactions = repository.getAllTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Category>> getAllCategories() {
        return repository.getAllCategories();
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
