package com.example.aplicacion_financiero.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aplicacion_financiero.data.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByType(String type);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    List<Category> getCategoriesByTypeSync(String type);

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND type = :type")
    int countByNameAndType(String name, String type);

    @Query("SELECT * FROM categories WHERE isCustom = 1 ORDER BY name ASC")
    LiveData<List<Category>> getCustomCategories();

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getCategoryById(long id);

    @Query("DELETE FROM categories WHERE isCustom = 1")
    void deleteCustomCategories();

    @Query("DELETE FROM categories")
    void deleteAllCategories();
}
