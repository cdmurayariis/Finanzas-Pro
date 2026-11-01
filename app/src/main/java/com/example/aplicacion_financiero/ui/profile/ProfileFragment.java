package com.example.aplicacion_financiero.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.example.aplicacion_financiero.LoginActivity;
import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.ui.common.AnimatedFragment;
import com.example.aplicacion_financiero.ui.common.RecoveryCodeDialog;
import com.example.aplicacion_financiero.utils.AuthManager;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends AnimatedFragment {

    private TextView tvCurrencyValue, tvThemeValue, tvFirstDayValue, tvCustomCategoriesValue;
    private TextView tvAccountEmail;

    private ProfileViewModel viewModel;
    private final List<Category> customCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupViewModel();
        setupListeners(view);
        observeData();
        updatePreferenceValues();

        return view;
    }

    private void initializeViews(View view) {
        tvCurrencyValue = view.findViewById(R.id.tv_currency_value);
        tvThemeValue = view.findViewById(R.id.tv_theme_value);
        tvFirstDayValue = view.findViewById(R.id.tv_first_day_value);
        tvCustomCategoriesValue = view.findViewById(R.id.tv_custom_categories_value);
        tvAccountEmail = view.findViewById(R.id.tv_account_email);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void setupListeners(View root) {
        root.findViewById(R.id.ll_currency).setOnClickListener(v -> showCurrencyDialog());
        root.findViewById(R.id.ll_theme).setOnClickListener(v -> showThemeDialog());
        root.findViewById(R.id.ll_first_day).setOnClickListener(v -> showFirstDayDialog());
        root.findViewById(R.id.ll_manage_categories)
                .setOnClickListener(v -> showManageCategoriesDialog());
        root.findViewById(R.id.ll_clear_data).setOnClickListener(v -> confirmClearData());
        root.findViewById(R.id.ll_about).setOnClickListener(v -> showAboutDialog());
        root.findViewById(R.id.ll_change_password)
                .setOnClickListener(v -> showChangePasswordDialog());
        root.findViewById(R.id.ll_change_email)
                .setOnClickListener(v -> showChangeEmailDialog());
        root.findViewById(R.id.ll_recovery_code)
                .setOnClickListener(v -> showRecoveryCodeDialog());
        root.findViewById(R.id.ll_logout).setOnClickListener(v -> confirmLogout());
    }

    private void observeData() {
        viewModel.getCustomCategories().observe(getViewLifecycleOwner(), categories -> {
            customCategories.clear();
            if (categories != null) {
                customCategories.addAll(categories);
            }
            tvCustomCategoriesValue.setText(String.valueOf(customCategories.size()));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            updatePreferenceValues();
        }
    }

    private void updatePreferenceValues() {
        String code = PrefsManager.getCurrencyCode(requireContext());
        tvCurrencyValue.setText(CurrencyUtils.getCurrencySymbol(requireContext())
                + " " + CurrencyUtils.getCurrencyName(requireContext()) + " (" + code + ")");

        int mode = PrefsManager.getThemeMode(requireContext());
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            tvThemeValue.setText("Claro");
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            tvThemeValue.setText("Oscuro");
        } else {
            tvThemeValue.setText("Sistema");
        }

        tvFirstDayValue.setText("Día " + PrefsManager.getFirstDayOfPeriod(requireContext()));
        tvAccountEmail.setText(AuthManager.getEmail(requireContext()));
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null, false);
        TextInputLayout tilCurrent = view.findViewById(R.id.til_current_dni);
        TextInputLayout tilNew = view.findViewById(R.id.til_new_dni);
        TextInputLayout tilConfirm = view.findViewById(R.id.til_confirm_dni);
        TextInputEditText etCurrent = view.findViewById(R.id.et_current_dni);
        TextInputEditText etNew = view.findViewById(R.id.et_new_dni);
        TextInputEditText etConfirm = view.findViewById(R.id.et_confirm_dni);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar contraseña (DNI)")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    tilCurrent.setError(null);
                    tilNew.setError(null);
                    tilConfirm.setError(null);

                    String current = textOf(etCurrent);
                    String newDni = textOf(etNew);
                    String confirm = textOf(etConfirm);

                    if (!AuthManager.isValidDni(current)) {
                        tilCurrent.setError("El DNI actual debe tener 8 dígitos");
                        return;
                    }
                    if (!AuthManager.isValidDni(newDni)) {
                        tilNew.setError("El nuevo DNI debe tener 8 dígitos");
                        return;
                    }
                    if (!newDni.equals(confirm)) {
                        tilConfirm.setError("Los DNI no coinciden");
                        return;
                    }
                    if (!AuthManager.changeDni(requireContext(), current, newDni)) {
                        tilCurrent.setError("El DNI actual es incorrecto");
                        return;
                    }
                    Toast.makeText(requireContext(), "Contraseña actualizada",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showChangeEmailDialog() {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_email, null, false);
        TextInputLayout tilDni = view.findViewById(R.id.til_email_dni);
        TextInputLayout tilEmail = view.findViewById(R.id.til_new_email);
        TextInputEditText etDni = view.findViewById(R.id.et_email_dni);
        TextInputEditText etEmail = view.findViewById(R.id.et_new_email);
        etEmail.setText(AuthManager.getEmail(requireContext()));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar correo electrónico")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    tilDni.setError(null);
                    tilEmail.setError(null);

                    String dni = textOf(etDni);
                    String newEmail = textOf(etEmail);

                    if (!AuthManager.isValidDni(dni)) {
                        tilDni.setError("El DNI debe tener 8 dígitos");
                        return;
                    }
                    if (!AuthManager.isValidEmail(newEmail)) {
                        tilEmail.setError("Ingresa un correo electrónico válido");
                        return;
                    }
                    if (!AuthManager.changeEmail(requireContext(), dni, newEmail)) {
                        tilDni.setError("El DNI es incorrecto");
                        return;
                    }
                    tvAccountEmail.setText(AuthManager.getEmail(requireContext()));
                    Toast.makeText(requireContext(), "Correo actualizado",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showRecoveryCodeDialog() {
        boolean hasCode = AuthManager.hasRecoveryCode(requireContext());
        String message = hasCode
                ? "Ya tienes un código de recuperación. Si generas uno nuevo, el anterior "
                        + "dejará de funcionar.\n\n¿Generar un nuevo código?"
                : "Genera un código que te permitirá restablecer tu DNI si olvidas tu "
                        + "contraseña. Se mostrará una sola vez.";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(hasCode ? "Regenerar código" : "Código de recuperación")
                .setMessage(message)
                .setPositiveButton(hasCode ? "Regenerar" : "Generar", (dialog, which) -> {
                    String code = AuthManager.generateRecoveryCode(requireContext());
                    if (code == null) {
                        Toast.makeText(requireContext(), "No se pudo generar el código",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RecoveryCodeDialog.show(requireContext(), code);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("Tus datos financieros se conservarán. Podrás volver a ingresar "
                        + "con tu correo electrónico y tu DNI.")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> {
                    AuthManager.logout(requireContext());
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String textOf(android.widget.TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    private void showCurrencyDialog() {
        String current = PrefsManager.getCurrencyCode(requireContext());
        String[] codes = CurrencyUtils.SUPPORTED_CODES;
        String[] labels = new String[codes.length];
        int selected = 0;
        for (int i = 0; i < codes.length; i++) {
            String symbol = symbolForCode(codes[i]);
            labels[i] = symbol + "  " + nameForCode(codes[i]) + " (" + codes[i] + ")";
            if (codes[i].equals(current)) {
                selected = i;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Elegir moneda")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    PrefsManager.setCurrencyCode(requireContext(), codes[which]);
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String symbolForCode(String code) {
        switch (code) {
            case "USD":
                return "$";
            case "EUR":
                return "\u20AC";
            case "MXN":
                return "MX$";
            case "COP":
                return "COL$";
            case "ARS":
                return "AR$";
            case "CLP":
                return "CL$";
            case "PEN":
            default:
                return "S/";
        }
    }

    private String nameForCode(String code) {
        switch (code) {
            case "USD":
                return "Dólar estadounidense";
            case "EUR":
                return "Euro";
            case "MXN":
                return "Peso mexicano";
            case "COP":
                return "Peso colombiano";
            case "ARS":
                return "Peso argentino";
            case "CLP":
                return "Peso chileno";
            case "PEN":
            default:
                return "Sol peruano";
        }
    }

    private void showThemeDialog() {
        String[] labels = {"Claro", "Oscuro", "Sistema"};
        int[] modes = {
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        };
        int currentMode = PrefsManager.getThemeMode(requireContext());
        int selected = currentMode == AppCompatDelegate.MODE_NIGHT_NO ? 0
                : currentMode == AppCompatDelegate.MODE_NIGHT_YES ? 1 : 2;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar tema")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    PrefsManager.setThemeMode(requireContext(), modes[which]);
                    dialog.dismiss();
                    AppCompatDelegate.setDefaultNightMode(modes[which]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showFirstDayDialog() {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_number, null, false);
        TextInputLayout tilNumber = view.findViewById(R.id.til_number);
        TextInputEditText etNumber = view.findViewById(R.id.et_number);
        TextView tvMessage = view.findViewById(R.id.tv_number_message);

        tvMessage.setText("Indica el día del mes en que inicia tu período presupuestario (1 - 28). "
                + "Se usará para calcular presupuestos y reportes.");
        tilNumber.setHint("Día del mes");
        etNumber.setText(String.valueOf(PrefsManager.getFirstDayOfPeriod(requireContext())));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Inicio del período")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String text = etNumber.getText() == null ? "" : etNumber.getText().toString().trim();
                    int day;
                    try {
                        day = Integer.parseInt(text);
                    } catch (Exception e) {
                        tilNumber.setError("Ingresa un número válido");
                        return;
                    }
                    if (day < 1 || day > 28) {
                        tilNumber.setError("El día debe estar entre 1 y 28");
                        return;
                    }
                    PrefsManager.setFirstDayOfPeriod(requireContext(), day);
                    dialog.dismiss();
                    requireActivity().recreate();
                }));
        dialog.show();
    }

    private void showManageCategoriesDialog() {
        if (customCategories.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Categorías personalizadas")
                    .setMessage("Aún no has creado categorías personalizadas.")
                    .setPositiveButton("Crear categoría", (dialog, which) -> showAddCategoryDialog())
                    .setNegativeButton("Cerrar", null)
                    .show();
            return;
        }

        String[] names = new String[customCategories.size()];
        for (int i = 0; i < customCategories.size(); i++) {
            Category category = customCategories.get(i);
            names[i] = category.getName() + ("INCOME".equals(category.getType())
                    ? " (Ingreso)" : " (Gasto)");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Categorías personalizadas")
                .setItems(names, (dialog, which) -> confirmDeleteCategory(customCategories.get(which)))
                .setPositiveButton("Nueva", (dialog, which) -> showAddCategoryDialog())
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void confirmDeleteCategory(Category category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar categoría")
                .setMessage("¿Eliminar \"" + category.getName()
                        + "\"? Los movimientos existentes conservarán su nombre.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.deleteCategory(category);
                    Toast.makeText(requireContext(), "Categoría eliminada",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showAddCategoryDialog() {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_category, null, false);
        TextInputLayout tilName = view.findViewById(R.id.til_category_name);
        TextInputEditText etName = view.findViewById(R.id.et_category_name);
        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggle_category_type);
        toggle.check(R.id.btn_category_expense);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nueva categoría")
                .setView(view)
                .setPositiveButton("Crear", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        tilName.setError("El nombre es obligatorio");
                        return;
                    }
                    String type = toggle.getCheckedButtonId() == R.id.btn_category_income
                            ? "INCOME" : "EXPENSE";
                    viewModel.addCategory(name, type);
                    Toast.makeText(requireContext(), "Categoría creada",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void confirmClearData() {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Borrar todos los datos")
                .setMessage("Esta acción eliminará de forma permanente todas las transacciones, "
                        + "presupuestos, objetivos de ahorro y categorías personalizadas. "
                        + "Las categorías predeterminadas se conservarán.\n\n¿Estás seguro?")
                .setPositiveButton("Borrar todo", (d, which) -> {
                    viewModel.clearAllData();
                    Toast.makeText(requireContext(), "Datos eliminados",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.expense_red)));
        dialog.show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Finanzas Pro")
                .setMessage("Versión 1.0\n\nAplicación para gestionar ingresos, gastos, presupuestos "
                        + "y objetivos de ahorro.\n\nLos datos se almacenan localmente en tu dispositivo. "
                        + "Moneda predeterminada: Sol peruano (PEN), configurable en Preferencias.")
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
