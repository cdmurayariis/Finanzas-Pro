package com.example.aplicacion_financiero.ui.savings;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.SavingsGoal;
import com.example.aplicacion_financiero.ui.adapter.SavingsGoalAdapter;
import com.example.aplicacion_financiero.ui.common.AnimatedFragment;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.UiAnimations;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;

public class SavingsFragment extends AnimatedFragment {

    private TextView tvTotalSaved, tvTotalTarget, tvCount;
    private View emptyState;
    private RecyclerView rvGoals;

    private SavingsViewModel viewModel;
    private SavingsGoalAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savings, container, false);

        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupButtons(view);
        observeData();

        return view;
    }

    private void initializeViews(View view) {
        tvTotalSaved = view.findViewById(R.id.tv_savings_total_saved);
        tvTotalTarget = view.findViewById(R.id.tv_savings_total_target);
        tvCount = view.findViewById(R.id.tv_savings_count);
        emptyState = view.findViewById(R.id.layout_empty_savings);
        rvGoals = view.findViewById(R.id.rv_savings_goals);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SavingsViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new SavingsGoalAdapter(this::showGoalOptions);
        rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGoals.setAdapter(adapter);
    }

    private void setupButtons(View root) {
        View addGoalButton = root.findViewById(R.id.btn_add_goal);
        UiAnimations.pressScale(addGoalButton);
        addGoalButton.setOnClickListener(v -> showGoalDialog(null));
    }

    private void observeData() {
        viewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            adapter.setGoals(goals);
            updateSummary(goals);
            boolean empty = goals == null || goals.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void updateSummary(List<SavingsGoal> goals) {
        long saved = 0L;
        long target = 0L;
        int count = goals == null ? 0 : goals.size();
        if (goals != null) {
            for (SavingsGoal goal : goals) {
                saved += goal.getSavedAmountInCents();
                target += goal.getTargetAmountInCents();
            }
        }
        tvTotalSaved.setText(CurrencyUtils.formatCurrency(requireContext(), saved));
        tvTotalTarget.setText(CurrencyUtils.formatCurrency(requireContext(), target));
        tvCount.setText(count + (count == 1 ? " objetivo activo" : " objetivos activos"));
    }

    private void showGoalOptions(SavingsGoal goal) {
        boolean completed = goal.getSavedAmountInCents() >= goal.getTargetAmountInCents();
        String[] options = completed
                ? new String[]{"Editar", "Eliminar"}
                : new String[]{"Registrar aporte", "Editar", "Eliminar"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(goal.getName())
                .setItems(options, (dialog, which) -> {
                    if (completed) {
                        if (which == 0) {
                            showGoalDialog(goal);
                        } else {
                            confirmDelete(goal);
                        }
                    } else {
                        if (which == 0) {
                            showContributionDialog(goal);
                        } else if (which == 1) {
                            showGoalDialog(goal);
                        } else {
                            confirmDelete(goal);
                        }
                    }
                })
                .show();
    }

    private void confirmDelete(SavingsGoal goal) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar objetivo")
                .setMessage("¿Estás seguro de eliminar \"" + goal.getName()
                        + "\"? El dinero ahorrado en este objetivo dejará de mostrarse.")
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.deleteGoal(goal))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showContributionDialog(SavingsGoal goal) {
        long remaining = Math.max(0L,
                goal.getTargetAmountInCents() - goal.getSavedAmountInCents());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_amount, null, false);
        TextInputLayout tilAmount = view.findViewById(R.id.til_dialog_amount);
        TextInputEditText etAmount = view.findViewById(R.id.et_dialog_amount);
        TextView tvMessage = view.findViewById(R.id.tv_dialog_message);

        tilAmount.setHint("Aporte (" + CurrencyUtils.getCurrencySymbol(requireContext()) + ")");
        tvMessage.setText("Ahorrado actual: "
                + CurrencyUtils.formatCurrency(requireContext(), goal.getSavedAmountInCents())
                + "\nFalta para la meta: " + CurrencyUtils.formatCurrency(requireContext(), remaining));
        tvMessage.setVisibility(View.VISIBLE);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Aportar a " + goal.getName())
                .setView(view)
                .setPositiveButton("Aportar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    long amount = CurrencyUtils.toCents(etAmount.getText() == null
                            ? "" : etAmount.getText().toString().trim());
                    if (amount <= 0) {
                        tilAmount.setError("Ingresa un monto mayor a 0");
                        return;
                    }
                    boolean completed = amount >= remaining;
                    viewModel.addContribution(goal, amount);
                    if (completed) {
                        Toast.makeText(requireContext(), "¡Meta completada!",
                                Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showGoalDialog(@Nullable SavingsGoal existing) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_savings_goal, null, false);

        TextInputLayout tilName = view.findViewById(R.id.til_goal_name);
        TextInputLayout tilTarget = view.findViewById(R.id.til_goal_target);
        TextInputLayout tilSaved = view.findViewById(R.id.til_goal_saved);
        TextInputLayout tilDeadline = view.findViewById(R.id.til_goal_deadline);
        TextInputEditText etName = view.findViewById(R.id.et_goal_name);
        TextInputEditText etTarget = view.findViewById(R.id.et_goal_target);
        TextInputEditText etSaved = view.findViewById(R.id.et_goal_saved);
        TextInputEditText etDeadline = view.findViewById(R.id.et_goal_deadline);
        com.google.android.material.button.MaterialButton btnClearDeadline =
                view.findViewById(R.id.btn_clear_goal_deadline);

        final Long[] deadline = {existing == null ? null : existing.getDeadline()};

        tilTarget.setHint("Monto objetivo (" + CurrencyUtils.getCurrencySymbol(requireContext()) + ")");
        tilSaved.setHint("Ahorrado actualmente (" + CurrencyUtils.getCurrencySymbol(requireContext()) + ")");

        if (existing != null) {
            etName.setText(existing.getName());
            etTarget.setText(CurrencyUtils.formatAmount(existing.getTargetAmountInCents()));
            etSaved.setText(CurrencyUtils.formatAmount(existing.getSavedAmountInCents()));
        }
        updateDeadlineLabel(etDeadline, btnClearDeadline, deadline[0]);

        Runnable openPicker = () -> {
            Calendar calendar = Calendar.getInstance();
            if (deadline[0] != null) {
                calendar.setTimeInMillis(deadline[0]);
            }
            DatePickerDialog picker = new DatePickerDialog(requireContext(),
                    (pickerView, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth, 12, 0, 0);
                        selected.set(Calendar.MILLISECOND, 0);
                        deadline[0] = selected.getTimeInMillis();
                        updateDeadlineLabel(etDeadline, btnClearDeadline, deadline[0]);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            picker.show();
        };

        etDeadline.setOnClickListener(v -> openPicker.run());
        tilDeadline.setEndIconOnClickListener(v -> openPicker.run());
        btnClearDeadline.setOnClickListener(v -> {
            deadline[0] = null;
            updateDeadlineLabel(etDeadline, btnClearDeadline, null);
            tilDeadline.setError(null);
        });

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Nuevo objetivo de ahorro" : "Editar objetivo")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        tilName.setError("El nombre es obligatorio");
                        return;
                    }
                    long target = CurrencyUtils.toCents(etTarget.getText() == null
                            ? "" : etTarget.getText().toString().trim());
                    if (target <= 0) {
                        tilTarget.setError("Ingresa un monto objetivo mayor a 0");
                        return;
                    }
                    long saved = CurrencyUtils.toCents(etSaved.getText() == null
                            ? "" : etSaved.getText().toString().trim());
                    if (saved < 0) {
                        saved = 0;
                    }
                    if (saved > target) {
                        tilSaved.setError("El ahorro no puede superar el objetivo");
                        return;
                    }

                    if (existing == null) {
                        viewModel.insertGoal(new SavingsGoal(name, target, saved, deadline[0]));
                    } else {
                        existing.setName(name);
                        existing.setTargetAmountInCents(target);
                        existing.setSavedAmountInCents(saved);
                        existing.setDeadline(deadline[0]);
                        viewModel.updateGoal(existing);
                    }
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void updateDeadlineLabel(TextInputEditText etDeadline,
                                     com.google.android.material.button.MaterialButton btnClear,
                                     Long deadline) {
        if (deadline == null) {
            etDeadline.setText("Sin fecha límite");
            btnClear.setVisibility(View.GONE);
        } else {
            etDeadline.setText(DateUtils.formatShortDate(deadline));
            btnClear.setVisibility(View.VISIBLE);
        }
    }
}
