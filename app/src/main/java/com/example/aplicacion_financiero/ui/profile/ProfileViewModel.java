package com.example.aplicacion_financiero.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.repository.FinanceRepository;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final LiveData<List<Category>> customCategories;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        customCategories = repository.getCustomCategories();
    }

    public LiveData<List<Category>> getCustomCategories() {
        return customCategories;
    }

    public void addCategory(String name, String type) {
        repository.insertCategory(new Category(name, type, "custom", true));
    }

    public void deleteCategory(Category category) {
        repository.deleteCategory(category);
    }

    public void clearAllData() {
        repository.clearUserData();
    }
}
