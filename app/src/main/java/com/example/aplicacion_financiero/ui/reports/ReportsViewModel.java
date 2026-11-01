package com.example.aplicacion_financiero.ui.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.data.repository.FinanceRepository;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;

import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final MutableLiveData<long[]> range = new MutableLiveData<>();

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        int firstDay = PrefsManager.getFirstDayOfPeriod(application);
        long now = System.currentTimeMillis();
        range.setValue(new long[]{
                DateUtils.getPeriodStartForDate(now, firstDay),
                DateUtils.getPeriodEndForDate(now, firstDay)
        });
    }

    public void setRange(long start, long end) {
        range.setValue(new long[]{start, end});
    }

    public LiveData<List<Transaction>> getTransactions() {
        return Transformations.switchMap(range,
                value -> repository.getTransactionsBetweenDates(value[0], value[1]));
    }

    public LiveData<Long> getStartingBalance() {
        return Transformations.switchMap(range,
                value -> repository.getBalanceBefore(value[0]));
    }
}
