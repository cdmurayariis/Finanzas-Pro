package com.example.aplicacion_financiero.ui.budget;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.Budget;
import com.example.aplicacion_financiero.data.model.BudgetCategoryItem;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.CategoryTotal;
import com.example.aplicacion_financiero.data.model.Period;
import com.example.aplicacion_financiero.ui.adapter.BudgetCategoryAdapter;
import com.example.aplicacion_financiero.ui.common.AnimatedFragment;
import com.example.aplicacion_financiero.utils.CategoryDefaults;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.UiAnimations;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetFragment extends AnimatedFragment {

    private TextView tvPeriod, tvTotalBudget, tvBudgetSpent, tvBudgetRemaining;
    private TextView tvBudgetPercent, tvBudgetAlert;
    private ProgressBar progressBudget;
    private RecyclerView rvCategories;

    private BudgetViewModel viewModel;
    private BudgetCategoryAdapter adapter;

    private final List<Category> expenseCategories = new ArrayList<>();
    private final List<Budget> budgets = new ArrayList<>();
    private final List<Budget> allBudgets = new ArrayList<>();
    private final Map<String, Long> categoryLimits = new HashMap<>();
    private final Map<String, Long> categorySpent = new HashMap<>();

    private long totalBudget;
    private long categoryBudgetTotal;
    private long totalExpense;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupButtons(view);
        observeData();

        return view;
    }

    private void initializeViews(View view) {
        tvPeriod = view.findViewById(R.id.tv_budget_period);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvBudgetSpent = view.findViewById(R.id.tv_budget_spent);
        tvBudgetRemaining = view.findViewById(R.id.tv_budget_remaining);
        tvBudgetPercent = view.findViewById(R.id.tv_budget_percent);
        tvBudgetAlert = view.findViewById(R.id.tv_budget_alert);
        progressBudget = view.findViewById(R.id.progress_budget_total);
        rvCategories = view.findViewById(R.id.rv_budget_categories);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new BudgetCategoryAdapter(this::showCategoryLimitDialog);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(adapter);
    }

    private void setupButtons(View root) {
        View previousButton = root.findViewById(R.id.btn_prev_period);
        View nextButton = root.findViewById(R.id.btn_next_period);
        View setBudgetButton = root.findViewById(R.id.btn_set_budget);
        View historyButton = root.findViewById(R.id.btn_budget_history);
        UiAnimations.pressScale(previousButton);
        UiAnimations.pressScale(nextButton);
        UiAnimations.pressScale(setBudgetButton);
        UiAnimations.pressScale(historyButton);

        previousButton.setOnClickListener(v -> viewModel.previousMonth());
        nextButton.setOnClickListener(v -> viewModel.nextMonth());
        setBudgetButton.setOnClickListener(v -> showTotalBudgetDialog());
        historyButton.setOnClickListener(v -> showHistoryDialog());
    }

    private void observeData() {
        viewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), period -> {
            if (period != null) {
                tvPeriod.setText(DateUtils.formatMonthYear(period.getYear(), period.getMonth()));
            }
        });

        viewModel.getTotalBudget().observe(getViewLifecycleOwner(), budget -> {
            totalBudget = budget == null ? 0L
                    : Math.max(budget.getTotalBudgetInCents(), budget.getCategoryBudgetInCents());
            updateOverview();
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            totalExpense = expense == null ? 0L : expense;
            updateOverview();
        });

        viewModel.getBudgets().observe(getViewLifecycleOwner(), list -> {
            budgets.clear();
            if (list != null) {
                budgets.addAll(list);
            }
            categoryBudgetTotal = 0L;
            for (Budget budget : budgets) {
                if (!Budget.TOTAL_CATEGORY.equals(budget.getCategory())) {
                    categoryBudgetTotal += Math.max(0L, budget.getCategoryBudgetInCents());
                }
            }
            updateOverview();
            rebuildCategoryItems();
        });

        viewModel.getExpensesByCategory().observe(getViewLifecycleOwner(), totals -> {
            categorySpent.clear();
            if (totals != null) {
                for (CategoryTotal total : totals) {
                    categorySpent.put(total.getCategory(), total.getTotal());
                }
            }
            rebuildCategoryItems();
        });

        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), categories -> {
            expenseCategories.clear();
            if (categories != null) {
                expenseCategories.addAll(categories);
            }
            rebuildCategoryItems();
        });

        viewModel.getAllBudgets().observe(getViewLifecycleOwner(), list -> {
            allBudgets.clear();
            if (list != null) {
                allBudgets.addAll(list);
            }
        });
    }

    private void updateOverview() {
        long effectiveBudget = Math.max(totalBudget, categoryBudgetTotal);
        tvTotalBudget.setText(CurrencyUtils.formatCurrency(requireContext(), effectiveBudget));
        tvBudgetSpent.setText(CurrencyUtils.formatCurrency(requireContext(), totalExpense));

        if (effectiveBudget <= 0) {
            UiAnimations.animateProgress(progressBudget, 0);
            tvBudgetPercent.setText("Sin presupuesto definido");
            tvBudgetRemaining.setText("—");
            tvBudgetAlert.setVisibility(View.GONE);
            return;
        }

        long remaining = effectiveBudget - totalExpense;
        tvBudgetRemaining.setText(CurrencyUtils.formatCurrency(requireContext(), remaining));

        int percent = (int) Math.min(100, Math.round(totalExpense * 100.0 / effectiveBudget));
        UiAnimations.animateProgress(progressBudget, percent);
        tvBudgetPercent.setText(percent + "% utilizado");

        int color;
        if (percent >= 100) {
            tvBudgetAlert.setVisibility(View.VISIBLE);
            tvBudgetAlert.setText("Presupuesto agotado. Has superado el límite del período.");
            color = R.color.expense_red;
        } else if (percent >= 80) {
            tvBudgetAlert.setVisibility(View.VISIBLE);
            tvBudgetAlert.setText("Alerta: has utilizado el 80% o más de tu presupuesto.");
            color = R.color.warning_orange;
        } else {
            tvBudgetAlert.setVisibility(View.GONE);
            color = R.color.income_green;
        }

        int resolved = ContextCompat.getColor(requireContext(), color);
        tvBudgetAlert.setTextColor(resolved);
        progressBudget.setProgressTintList(ColorStateList.valueOf(resolved));
        boolean overBudget = remaining < 0;
        tvBudgetRemaining.setTextColor(ContextCompat.getColor(requireContext(),
                overBudget ? R.color.expense_red : R.color.income_green));
    }

    private void rebuildCategoryItems() {
        Map<String, Long> limits = new LinkedHashMap<>();
        for (Budget budget : budgets) {
            if (Budget.TOTAL_CATEGORY.equals(budget.getCategory())) {
                continue;
            }
            limits.put(budget.getCategory(), budget.getCategoryBudgetInCents());
        }
        categoryLimits.clear();
        categoryLimits.putAll(limits);

        Map<String, Long> spent = new HashMap<>(categorySpent);
        Map<String, Long> combined = new LinkedHashMap<>();

        for (Category category : expenseCategories) {
            combined.put(category.getName(), limits.containsKey(category.getName())
                    ? limits.get(category.getName()) : 0L);
        }
        for (String name : CategoryDefaults.forType("EXPENSE")) {
            combined.putIfAbsent(name, 0L);
        }
        for (String name : limits.keySet()) {
            combined.putIfAbsent(name, limits.get(name));
        }
        for (String name : spent.keySet()) {
            combined.putIfAbsent(name, 0L);
        }

        List<BudgetCategoryItem> items = new ArrayList<>();
        for (Map.Entry<String, Long> entry : combined.entrySet()) {
            long spentAmount = spent.containsKey(entry.getKey()) ? spent.get(entry.getKey()) : 0L;
            items.add(new BudgetCategoryItem(entry.getKey(), entry.getValue(), spentAmount));
        }
        adapter.setItems(items);
    }

    private void showTotalBudgetDialog() {
        Period period = viewModel.getSelectedPeriod().getValue();
        if (period == null) {
            return;
        }
        showAmountDialog("Presupuesto mensual",
                "Define el presupuesto total para "
                        + DateUtils.formatMonthYear(period.getYear(), period.getMonth()) + ".",
                totalBudget, true, amount -> {
                    viewModel.saveTotalBudget(period, amount);
                });
    }

    private void showCategoryLimitDialog(BudgetCategoryItem item) {
        Period period = viewModel.getSelectedPeriod().getValue();
        if (period == null) {
            return;
        }
        showAmountDialog("Límite de " + item.getCategory(),
                "Ingresa el límite mensual. Usa 0 para eliminar el límite.",
                item.getLimitInCents(), false, amount -> {
                    viewModel.saveCategoryBudget(period, item.getCategory(), amount);
                });
    }

    private interface OnAmountConfirmed {
        void onConfirmed(long amount);
    }

    private void showAmountDialog(String title, String message, long initialAmount,
                                  boolean required, OnAmountConfirmed callback) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_amount, null, false);
        TextInputLayout tilAmount = view.findViewById(R.id.til_dialog_amount);
        TextInputEditText etAmount = view.findViewById(R.id.et_dialog_amount);
        TextView tvMessage = view.findViewById(R.id.tv_dialog_message);

        tilAmount.setHint("Monto (" + CurrencyUtils.getCurrencySymbol(requireContext()) + ")");
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
        if (initialAmount > 0) {
            etAmount.setText(CurrencyUtils.formatAmount(initialAmount));
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String text = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
                    long amount = CurrencyUtils.toCents(text);
                    if (text.isEmpty() && !required) {
                        amount = 0;
                    }
                    if (required && amount <= 0) {
                        tilAmount.setError("Ingresa un monto mayor a 0");
                        return;
                    }
                    tilAmount.setError(null);
                    callback.onConfirmed(amount);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showHistoryDialog() {
        StringBuilder builder = new StringBuilder();
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Budget budget : allBudgets) {
            if (!Budget.TOTAL_CATEGORY.equals(budget.getCategory())) {
                continue;
            }
            String key = budget.getYear() + "-" + String.format(Locale.US, "%02d", budget.getMonth());
            totals.put(key, Math.max(budget.getTotalBudgetInCents(),
                    budget.getCategoryBudgetInCents()));
        }

        if (totals.isEmpty()) {
            builder.append("Aún no hay presupuestos registrados.");
        } else {
            List<String> keys = new ArrayList<>(totals.keySet());
            keys.sort((a, b) -> b.compareTo(a));
            for (String key : keys) {
                String[] parts = key.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                builder.append(DateUtils.formatMonthYear(year, month))
                        .append(": ")
                        .append(CurrencyUtils.formatCurrency(requireContext(), totals.get(key)))
                        .append("\n");
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Historial de presupuestos")
                .setMessage(builder.toString().trim())
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
