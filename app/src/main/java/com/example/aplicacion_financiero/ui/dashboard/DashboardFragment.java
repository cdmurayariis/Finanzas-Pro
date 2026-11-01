package com.example.aplicacion_financiero.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.Budget;
import com.example.aplicacion_financiero.data.model.CategoryTotal;
import com.example.aplicacion_financiero.data.model.Period;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.ui.adapter.TransactionAdapter;
import com.example.aplicacion_financiero.ui.common.AnimatedFragment;
import com.example.aplicacion_financiero.ui.transactions.TransactionFormDialog;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;
import com.example.aplicacion_financiero.utils.UiAnimations;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends AnimatedFragment {

    private TextView tvTotalBalance, tvTotalIncome, tvTotalExpense, tvTotalSaved;
    private TextView tvBudgetPercentage, tvBudgetStatus, tvEmptyRecent;
    private ProgressBar progressBudget;
    private MaterialAutoCompleteTextView monthSpinner;
    private RecyclerView rvRecentTransactions;
    private BarChart barChart;
    private PieChart pieChart;

    private DashboardViewModel viewModel;
    private TransactionAdapter adapter;

    private final List<Period> periods = new ArrayList<>();
    private long currentIncome = 0L;
    private long currentExpense = 0L;
    private long currentBudget = 0L;
    private long currentTotalBudget = 0L;
    private long currentCategoryBudget = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initializeViews(view);
        setupViewModel();
        setupMonthSpinner();
        setupRecyclerView();
        setupButtons(view);
        setupCharts();
        observeData();

        return view;
    }

    private void initializeViews(View view) {
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvTotalSaved = view.findViewById(R.id.tv_total_saved);
        tvBudgetPercentage = view.findViewById(R.id.tv_budget_percentage);
        tvBudgetStatus = view.findViewById(R.id.tv_budget_status);
        tvEmptyRecent = view.findViewById(R.id.tv_empty_recent);
        progressBudget = view.findViewById(R.id.progress_budget_overview);
        monthSpinner = view.findViewById(R.id.month_spinner);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        barChart = view.findViewById(R.id.chart_income_expense);
        pieChart = view.findViewById(R.id.chart_expenses_category);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    private void setupMonthSpinner() {
        List<String> labels = new ArrayList<>();
        int firstDay = PrefsManager.getFirstDayOfPeriod(requireContext());
        long currentStart = DateUtils.getPeriodStartForDate(System.currentTimeMillis(), firstDay);
        Calendar base = Calendar.getInstance();
        base.setTimeInMillis(currentStart);
        for (int i = -23; i <= 0; i++) {
            Calendar cal = (Calendar) base.clone();
            cal.add(Calendar.MONTH, i);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            periods.add(new Period(year, month));
            labels.add(DateUtils.formatMonthYear(year, month));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        monthSpinner.setAdapter(spinnerAdapter);
        monthSpinner.setText(labels.get(labels.size() - 1), false);
        monthSpinner.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < periods.size()) {
                viewModel.setPeriod(periods.get(position));
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentTransactions.setAdapter(adapter);
        adapter.setOnTransactionClickListener(this::showTransactionOptions);
    }

    private void setupButtons(View root) {
        View incomeButton = root.findViewById(R.id.btn_add_income);
        View expenseButton = root.findViewById(R.id.btn_add_expense);
        UiAnimations.attachPressBounce(incomeButton, root.findViewById(R.id.iv_add_income_icon));
        UiAnimations.attachPressBounce(expenseButton, root.findViewById(R.id.iv_add_expense_icon));

        incomeButton.setOnClickListener(v ->
                TransactionFormDialog.show(this, null, "INCOME",
                        (transaction, isEdit) -> viewModel.insertTransaction(transaction)));

        expenseButton.setOnClickListener(v ->
                TransactionFormDialog.show(this, null, "EXPENSE",
                        (transaction, isEdit) -> viewModel.insertTransaction(transaction)));

        View reportsButton = root.findViewById(R.id.btn_open_reports);
        UiAnimations.pressScale(reportsButton);
        reportsButton.setOnClickListener(v ->
                Navigation.findNavController(root).navigate(R.id.action_dashboard_to_reports));
    }

    private void setupCharts() {
        int textColor = themeColor(com.google.android.material.R.attr.colorOnSurface);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(textColor);
        barChart.getAxisLeft().setGridColor(themeColor(com.google.android.material.R.attr.colorOutlineVariant));
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getXAxis().setTextColor(textColor);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.setNoDataText("Sin movimientos en el per\u00EDodo");
        barChart.setNoDataTextColor(textColor);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setCenterTextColor(textColor);
        pieChart.setCenterTextSize(12f);
        pieChart.setEntryLabelColor(textColor);
        pieChart.getLegend().setTextColor(textColor);
        pieChart.setNoDataText("Sin gastos registrados");
        pieChart.setNoDataTextColor(textColor);
    }

    private void observeData() {
        viewModel.getTotalBalance().observe(getViewLifecycleOwner(), balance ->
                tvTotalBalance.setText(CurrencyUtils.formatCurrency(requireContext(),
                        balance == null ? 0L : balance)));

        viewModel.getTotalSaved().observe(getViewLifecycleOwner(), saved ->
                tvTotalSaved.setText(CurrencyUtils.formatCurrency(requireContext(),
                        saved == null ? 0L : saved)));

        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            currentIncome = income == null ? 0L : income;
            tvTotalIncome.setText(CurrencyUtils.formatCurrency(requireContext(), currentIncome));
            updateBarChart();
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            currentExpense = expense == null ? 0L : expense;
            tvTotalExpense.setText(CurrencyUtils.formatCurrency(requireContext(), currentExpense));
            updateBarChart();
            updateBudgetOverview();
        });

        viewModel.getTotalBudget().observe(getViewLifecycleOwner(), budget -> {
            currentTotalBudget = budget == null ? 0L : Math.max(budget.getTotalBudgetInCents(),
                    budget.getCategoryBudgetInCents());
            refreshBudget();
        });

        viewModel.getCategoryBudgetTotal().observe(getViewLifecycleOwner(), total -> {
            currentCategoryBudget = total == null ? 0L : total;
            refreshBudget();
        });

        viewModel.getExpensesByCategory().observe(getViewLifecycleOwner(), this::updatePieChart);

        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.setTransactions(transactions);
            boolean empty = transactions == null || transactions.isEmpty();
            tvEmptyRecent.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvRecentTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, currentIncome / 100f));
        entries.add(new BarEntry(1f, currentExpense / 100f));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.income_green),
                ContextCompat.getColor(requireContext(), R.color.expense_red));
        dataSet.setValueTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface));
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(currencyFormatter());

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.45f);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0f ? "Ingresos" : "Gastos";
            }
        });
        barChart.setFitBars(true);
        barChart.animateY(500);
        barChart.invalidate();
    }

    private void updatePieChart(List<CategoryTotal> totals) {
        if (totals == null || totals.isEmpty()) {
            pieChart.setData(null);
            pieChart.setCenterText("Sin gastos");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int[] palette = {
                R.color.primary_electric_blue, R.color.expense_red, R.color.income_green,
                R.color.warning_orange, R.color.accent_blue, R.color.expense_coral,
                R.color.success_green, R.color.gray_dark, R.color.primary_blue_light
        };
        for (int i = 0; i < totals.size(); i++) {
            CategoryTotal total = totals.get(i);
            entries.add(new PieEntry(total.getTotal() / 100f, total.getCategory()));
            colors.add(ContextCompat.getColor(requireContext(), palette[i % palette.length]));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText("Gastos\n" + CurrencyUtils.formatCurrency(requireContext(), currentExpense));
        pieChart.animateY(500);
        pieChart.invalidate();
    }

    private void refreshBudget() {
        currentBudget = Math.max(currentTotalBudget, currentCategoryBudget);
        updateBudgetOverview();
    }

    private void updateBudgetOverview() {
        if (currentBudget <= 0) {
            UiAnimations.animateProgress(progressBudget, 0);
            tvBudgetPercentage.setText("Sin presupuesto definido");
            tvBudgetStatus.setText("Define un presupuesto para este per\u00EDodo");
            tvBudgetStatus.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            return;
        }

        int percent = (int) Math.min(100,
                Math.round(currentExpense * 100.0 / currentBudget));
        UiAnimations.animateProgress(progressBudget, percent);
        tvBudgetPercentage.setText(percent + "% del presupuesto utilizado");

        if (percent >= 100) {
            tvBudgetStatus.setText("Presupuesto agotado");
            tvBudgetStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
            progressBudget.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.expense_red)));
        } else if (percent >= 80) {
            tvBudgetStatus.setText("Alerta: has usado el 80% o m\u00E1s");
            tvBudgetStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_orange));
            progressBudget.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.warning_orange)));
        } else {
            long remaining = Math.max(0L, currentBudget - currentExpense);
            tvBudgetStatus.setText("Disponible: "
                    + CurrencyUtils.formatCurrency(requireContext(), remaining));
            tvBudgetStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
            progressBudget.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.primary_electric_blue)));
        }
    }

    private void showTransactionOptions(Transaction transaction) {
        String[] options = {"Editar", "Eliminar"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(transaction.getDescription())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        TransactionFormDialog.show(this, transaction, transaction.getType(),
                                (updated, isEdit) -> viewModel.updateTransaction(updated));
                    } else {
                        confirmDelete(transaction);
                    }
                })
                .show();
    }

    private void confirmDelete(Transaction transaction) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar movimiento")
                .setMessage("\u00BFEst\u00E1s seguro de eliminar este movimiento?")
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.deleteTransaction(transaction))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private ValueFormatter currencyFormatter() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return CurrencyUtils.formatCurrency(requireContext(), Math.round(value * 100));
            }
        };
    }

    private int themeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}
