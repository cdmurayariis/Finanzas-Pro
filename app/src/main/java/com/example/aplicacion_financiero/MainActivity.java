package com.example.aplicacion_financiero;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.aplicacion_financiero.utils.AuthManager;
import com.example.aplicacion_financiero.utils.PrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(PrefsManager.getThemeMode(this));
        super.onCreate(savedInstanceState);

        if (!AuthManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        if (!PrefsManager.isWelcomeShown(this)) {
            PrefsManager.setWelcomeShown(this);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Bienvenido a Finanzas Pro")
                    .setMessage("Gestiona tus ingresos, gastos, presupuestos y metas de ahorro en un solo lugar. "
                            + "Empieza registrando tu primer movimiento.")
                    .setPositiveButton("Comenzar", null)
                    .show();
        }
    }
}
