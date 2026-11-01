package com.example.aplicacion_financiero.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.aplicacion_financiero.data.dao.BudgetDao;
import com.example.aplicacion_financiero.data.dao.CategoryDao;
import com.example.aplicacion_financiero.data.dao.SavingsGoalDao;
import com.example.aplicacion_financiero.data.dao.TransactionDao;
import com.example.aplicacion_financiero.data.model.Budget;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.SavingsGoal;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.utils.AuthManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Transaction.class, Category.class, Budget.class, SavingsGoal.class},
        version = 1, exportSchema = false)
public abstract class FinanceDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();

    public abstract CategoryDao categoryDao();

    public abstract BudgetDao budgetDao();

    public abstract SavingsGoalDao savingsGoalDao();

    private static final Map<String, FinanceDatabase> INSTANCES = new HashMap<>();
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static FinanceDatabase getDatabase(final Context context) {
        return getDatabase(context, AuthManager.getUserDatabaseName(context));
    }

    public static FinanceDatabase getDatabase(final Context context, final String databaseName) {
        String name = databaseName == null || databaseName.isEmpty()
                ? AuthManager.LEGACY_DATABASE : databaseName;
        synchronized (FinanceDatabase.class) {
            FinanceDatabase existing = INSTANCES.get(name);
            if (existing != null) {
                return existing;
            }
            Context appContext = context.getApplicationContext();
            FinanceDatabase database = Room.databaseBuilder(appContext,
                            FinanceDatabase.class, name)
                    .addCallback(createCallback(appContext, name))
                    .build();
            INSTANCES.put(name, database);
            return database;
        }
    }

    private static RoomDatabase.Callback createCallback(final Context appContext,
                                                        final String databaseName) {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                databaseWriteExecutor.execute(() -> {
                    CategoryDao dao = getDatabase(appContext, databaseName).categoryDao();
                    dao.deleteAllCategories();

                    dao.insert(new Category("Alimentaci\u00F3n", "EXPENSE", "food", false));
                    dao.insert(new Category("Transporte", "EXPENSE", "transport", false));
                    dao.insert(new Category("Educaci\u00F3n", "EXPENSE", "education", false));
                    dao.insert(new Category("Vivienda", "EXPENSE", "home", false));
                    dao.insert(new Category("Salud", "EXPENSE", "health", false));
                    dao.insert(new Category("Entretenimiento", "EXPENSE", "entertainment", false));
                    dao.insert(new Category("Servicios", "EXPENSE", "services", false));
                    dao.insert(new Category("Compras", "EXPENSE", "shopping", false));
                    dao.insert(new Category("Otros", "EXPENSE", "other", false));

                    dao.insert(new Category("Salario", "INCOME", "salary", false));
                    dao.insert(new Category("Negocio", "INCOME", "business", false));
                    dao.insert(new Category("Trabajos adicionales", "INCOME", "extra", false));
                    dao.insert(new Category("Becas", "INCOME", "scholarship", false));
                    dao.insert(new Category("Otros ingresos", "INCOME", "other_income", false));
                });
            }
        };
    }
}
