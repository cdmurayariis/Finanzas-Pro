package com.example.aplicacion_financiero;

import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.aplicacion_financiero.ui.common.RecoveryCodeDialog;
import com.example.aplicacion_financiero.utils.AuthManager;
import com.example.aplicacion_financiero.utils.PrefsManager;
import com.example.aplicacion_financiero.utils.UiAnimations;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etEmail, etPassword, etConfirm;
    private TextView tvTitle, tvSubtitle, tvError, tvSuccess;
    private View forgotPassword, loginCard;
    private MaterialButton btnLogin, btnModeSwitch;
    private ViewGroup container;

    private boolean registerMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(PrefsManager.getThemeMode(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (AuthManager.isLoggedIn(this)) {
            openMain();
            return;
        }

        container = findViewById(R.id.login_container);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirm = findViewById(R.id.til_confirm);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        tvTitle = findViewById(R.id.tv_login_title);
        tvSubtitle = findViewById(R.id.tv_login_subtitle);
        tvError = findViewById(R.id.tv_login_error);
        tvSuccess = findViewById(R.id.tv_login_success);
        forgotPassword = findViewById(R.id.tv_forgot_password);
        loginCard = findViewById(R.id.login_card);
        btnLogin = findViewById(R.id.btn_login);
        btnModeSwitch = findViewById(R.id.btn_mode_switch);

        registerMode = !AuthManager.hasAccount(this);
        setupMode();
        setupListeners();
        runEntranceAnimations();
    }

    private void setupMode() {
        if (registerMode) {
            tvTitle.setText("Crear cuenta");
            tvSubtitle.setText("Registra tu correo electrónico. Tu DNI será la contraseña de acceso.");
            tilConfirm.setVisibility(View.VISIBLE);
            forgotPassword.setVisibility(View.GONE);
            btnLogin.setText("Crear cuenta");
            btnModeSwitch.setText("¿Ya tienes cuenta? Inicia sesión");
        } else {
            tvTitle.setText("Iniciar sesión");
            tvSubtitle.setText("Accede con tu correo electrónico y tu DNI como contraseña.");
            tilConfirm.setVisibility(View.GONE);
            forgotPassword.setVisibility(View.VISIBLE);
            btnLogin.setText("Ingresar");
            btnModeSwitch.setText("¿No tienes cuenta? Regístrate");
            String activeEmail = AuthManager.getEmail(this);
            if (isEmpty(etEmail) && !activeEmail.isEmpty()) {
                etEmail.setText(activeEmail);
            }
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> submit());
        btnModeSwitch.setOnClickListener(v -> switchMode());
        forgotPassword.setOnClickListener(v -> showResetDialog());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (registerMode) {
                    etConfirm.requestFocus();
                } else {
                    submit();
                }
                return true;
            }
            return false;
        });

        etConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });

        UiAnimations.pressScale(btnLogin);
        UiAnimations.pressScale(btnModeSwitch);
    }

    private void runEntranceAnimations() {
        UiAnimations.fadeScaleIn(findViewById(R.id.img_login_hero), 0);
        UiAnimations.floatLoop(findViewById(R.id.img_login_hero), 700);
        UiAnimations.staggerIn(180, 85,
                findViewById(R.id.tv_app_name),
                tvSubtitle,
                loginCard);
    }

    private void switchMode() {
        TransitionManager.beginDelayedTransition(container,
                new AutoTransition().setDuration(260));
        registerMode = !registerMode;
        clearMessages();
        etPassword.setText("");
        etConfirm.setText("");
        if (registerMode) {
            etEmail.setText("");
            etEmail.requestFocus();
        } else {
            etPassword.requestFocus();
        }
        setupMode();
    }

    private void submit() {
        clearMessages();
        String email = textOf(etEmail);
        String password = textOf(etPassword);

        if (!AuthManager.isValidEmail(email)) {
            tilEmail.setError("Ingresa un correo electrónico válido");
            etEmail.requestFocus();
            return;
        }
        if (!AuthManager.isValidDni(password)) {
            tilPassword.setError("El DNI debe tener exactamente 8 dígitos");
            etPassword.requestFocus();
            return;
        }

        if (registerMode) {
            String confirm = textOf(etConfirm);
            if (!password.equals(confirm)) {
                tilConfirm.setError("Los DNI no coinciden");
                etConfirm.requestFocus();
                return;
            }
            if (AuthManager.isRegistered(this, email)) {
                tilEmail.setError("Ese correo ya está registrado. Inicia sesión.");
                etEmail.requestFocus();
                return;
            }
            String recoveryCode = AuthManager.register(this, email, password);
            if (recoveryCode == null) {
                showError("No se pudo crear la cuenta. Inténtalo de nuevo.");
                return;
            }
            onRegistrationSuccess(email, recoveryCode);
        } else {
            if (AuthManager.login(this, email, password)) {
                openMain();
            } else {
                showError("Correo o DNI incorrecto. Verifica tus datos.");
            }
        }
    }

    private void onRegistrationSuccess(String email, String recoveryCode) {
        TransitionManager.beginDelayedTransition(container,
                new AutoTransition().setDuration(260));
        registerMode = false;
        setupMode();
        etEmail.setText(email);
        etPassword.setText("");
        etConfirm.setText("");
        tilPassword.setError(null);
        tilConfirm.setError(null);
        tvError.setVisibility(View.GONE);
        tvSuccess.setText("Cuenta creada correctamente. Ahora inicia sesión con tu correo y DNI.");
        tvSuccess.setVisibility(View.VISIBLE);
        etPassword.requestFocus();
        RecoveryCodeDialog.show(this, recoveryCode);
    }

    private void showResetDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null, false);
        TextInputLayout tilResetEmail = view.findViewById(R.id.til_reset_email);
        TextInputLayout tilResetRecovery = view.findViewById(R.id.til_reset_recovery);
        TextInputLayout tilResetDni = view.findViewById(R.id.til_reset_dni);
        TextInputLayout tilResetConfirm = view.findViewById(R.id.til_reset_confirm);
        TextInputEditText etResetEmail = view.findViewById(R.id.et_reset_email);
        TextInputEditText etResetRecovery = view.findViewById(R.id.et_reset_recovery);
        TextInputEditText etResetDni = view.findViewById(R.id.et_reset_dni);
        TextInputEditText etResetConfirm = view.findViewById(R.id.et_reset_confirm);

        String typedEmail = textOf(etEmail);
        etResetEmail.setText(!typedEmail.isEmpty() ? typedEmail : AuthManager.getEmail(this));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Restablecer contraseña")
                .setView(view)
                .setPositiveButton("Restablecer", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    tilResetEmail.setError(null);
                    tilResetRecovery.setError(null);
                    tilResetDni.setError(null);
                    tilResetConfirm.setError(null);

                    String email = textOf(etResetEmail);
                    String recoveryCode = textOf(etResetRecovery);
                    String newDni = textOf(etResetDni);
                    String confirm = textOf(etResetConfirm);

                    if (!AuthManager.isValidEmail(email)) {
                        tilResetEmail.setError("Ingresa un correo electrónico válido");
                        return;
                    }
                    if (!AuthManager.isRegistered(this, email)) {
                        tilResetEmail.setError("No existe una cuenta con ese correo");
                        return;
                    }
                    if (!AuthManager.hasRecoveryCode(this, email)) {
                        tilResetEmail.setError("Esta cuenta no tiene código de recuperación. "
                                + "Inicia sesión y genera uno en Perfil");
                        return;
                    }
                    if (!AuthManager.isRecoveryCodeValid(this, email, recoveryCode)) {
                        tilResetRecovery.setError("Código de recuperación incorrecto");
                        return;
                    }
                    if (!AuthManager.isValidDni(newDni)) {
                        tilResetDni.setError("El DNI debe tener exactamente 8 dígitos");
                        return;
                    }
                    if (!newDni.equals(confirm)) {
                        tilResetConfirm.setError("Los DNI no coinciden");
                        return;
                    }
                    if (!AuthManager.resetDni(this, email, recoveryCode, newDni)) {
                        Toast.makeText(this, "No se pudo restablecer la contraseña",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dialog.dismiss();
                    clearMessages();
                    registerMode = false;
                    setupMode();
                    etEmail.setText(email);
                    etPassword.setText("");
                    tvSuccess.setText("Contraseña restablecida. Ingresa con tu nuevo DNI.");
                    tvSuccess.setVisibility(View.VISIBLE);
                    etPassword.requestFocus();
                }));
        dialog.show();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void showError(String message) {
        tvSuccess.setVisibility(View.GONE);
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        UiAnimations.shake(loginCard);
    }

    private void clearMessages() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
    }

    private boolean isEmpty(TextView view) {
        return textOf(view).isEmpty();
    }

    private String textOf(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }
}
