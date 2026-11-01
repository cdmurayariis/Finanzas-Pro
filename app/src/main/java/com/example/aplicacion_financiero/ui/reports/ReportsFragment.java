package com.example.aplicacion_financiero.ui.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.ui.common.AnimatedFragment;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends AnimatedFragment {

    private static final int MODE_WEEK = 0;
    private static final int MODE_MONTH = 1;
    private static final int MODE_YEAR = 2;

    private TextView tvRange, tvIncome, tvExpense, tvBalance, tvMonthlyBalance, tvNoCategories;
    private LinearLayout topCategories;
    private BarChart barChart;
    private PieChart pieChart;
    private LineChart lineChart;
    private MaterialButtonToggleGroup periodToggle;

    private ReportsViewModel viewModel;

    private int mode = MODE_MONTH;
    private int firstDay = PrefsManager.DEFAULT_FIRST_DAY;
    private long referenceDate = System.currentTimeMillis();
    private List<Transaction> transactions = new ArrayList<>();
    private long startingBalance = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        firstDay = PrefsManager.getFirstDayOfPeriod(requireContext());
        initializeViews(view);
        setupViewModel();
        setupButtons(view);
        setupCharts();
        observeData();
        updateRange();

        return view;
    }

    private void initializeViews(View view) {
        tvRange = view.findViewById(R.id.tv_report_range);
        tvIncome = view.findViewById(R.id.tv_report_income);
        tvExpense = view.findViewById(R.id.tv_report_expense);
        tvBalance = view.findViewById(R.id.tv_report_balance);
        tvMonthlyBalance = view.findViewById(R.id.tv_report_monthly_balance);
        tvNoCategories = view.findViewById(R.id.tv_report_no_categories);
        topCategories = view.findViewById(R.id.layout_top_categories);
        barChart = view.findViewById(R.id.chart_report_flow);
        pieChart = view.findViewById(R.id.chart_report_categories);
        lineChart = view.findViewById(R.id.chart_report_balance);
        periodToggle = view.findViewById(R.id.toggle_report_period);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);
    }

    private void setupButtons(View root) {
        root.findViewById(R.id.btn_reports_back)
                .setOnClickListener(v -> Navigation.findNavController(root).navigateUp());

        periodToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            int newMode = checkedId == R.id.btn_period_week ? MODE_WEEK
                    : checkedId == R.id.btn_period_year ? MODE_YEAR : MODE_MONTH;
            if (newMode != mode) {
                mode = newMode;
                updateRange();
            }
        });
        periodToggle.check(R.id.btn_period_month);

        root.findViewById(R.id.btn_report_prev)
                .setOnClickListener(v -> movePeriod(-1));
        root.findViewById(R.id.btn_report_next)
                .setOnClickListener(v -> movePeriod(1));
    }

    private void movePeriod(int direction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(referenceDate);
        if (mode == MODE_WEEK) {
            calendar.add(Calendar.DAY_OF_MONTH, direction * 7);
        } else if (mode == MODE_MONTH) {
            calendar.add(Calendar.MONTH, direction);
        } else {
            calendar.add(Calendar.YEAR, direction);
        }
        referenceDate = calendar.getTimeInMillis();
        updateRange();
    }

    private void updateRange() {
        long[] range = currentRange();
        viewModel.setRange(range[0], range[1]);

        if (mode == MODE_WEEK) {
            tvRange.setText(DateUtils.formatRange(range[0], range[1]));
        } else if (mode == MODE_YEAR) {
            tvRange.setText("Año " + DateUtils.getYear(range[0]));
        } else {
            tvRange.setText(DateUtils.formatMonthYear(DateUtils.getYear(range[0]),
                    DateUtils.getMonth(range[0])));
        }
    }

    private long[] currentRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(referenceDate);
        int year = calendar.get(Calendar.YEAR);

        if (mode == MODE_WEEK) {
            return new long[]{
                    DateUtils.getStartOfWeek(referenceDate),
                    DateUtils.getEndOfWeek(referenceDate)
            };
        }
        if (mode == MODE_YEAR) {
            return new long[]{
                    DateUtils.getStartOfYear(year),
                    DateUtils.getEndOfYear(year)
            };
        }
        return new long[]{
                DateUtils.getPeriodStartForDate(referenceDate, firstDay),
                DateUtils.getPeriodEndForDate(referenceDate, firstDay)
        };
    }

    private void observeData() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            transactions = list == null ? new ArrayList<>() : list;
            rebuild();
        });

        viewModel.getStartingBalance().observe(getViewLifecycleOwner(), balance -> {
            startingBalance = balance == null ? 0L : balance;
            rebuild();
        });
    }

    private void rebuild() {
        long income = 0L;
        long expense = 0L;
        Map<String, Long> categoryExpenses = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            if ("INCOME".equals(transaction.getType())) {
                income += transaction.getAmountInCents();
            } else {
                expense += transaction.getAmountInCents();
                String category = transaction.getCategory() == null
                        ? "Otros" : transaction.getCategory();
                Long current = categoryExpenses.get(category);
                categoryExpenses.put(category, (current == null ? 0L : current)
                        + transaction.getAmountInCents());
            }
        }

        long balance = income - expense;
        tvIncome.setText(CurrencyUtils.formatCurrency(requireContext(), income));
        tvExpense.setText(CurrencyUtils.formatCurrency(requireContext(), expense));
        tvBalance.setText(CurrencyUtils.formatCurrency(requireContext(), balance));
        tvBalance.setTextColor(ContextCompat.getColor(requireContext(),
                balance >= 0 ? R.color.primary_electric_blue : R.color.expense_red));

        updateBarChart(income, expense);
        updatePieChart(categoryExpenses, expense);
        updateLineChart();
        updateTopCategories(categoryExpenses, expense);
        updateMonthlyBalance(income, expense);
    }

    private void updateBarChart(long income, long expense) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, income / 100f));
        entries.add(new BarEntry(1f, expense / 100f));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.income_green),
                ContextCompat.getColor(requireContext(), R.color.expense_red));
        dataSet.setValueTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return CurrencyUtils.formatCurrency(requireContext(), Math.round(value * 100));
            }
        });

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

    private void updatePieChart(Map<String, Long> categoryExpenses, long totalExpense) {
        if (categoryExpenses.isEmpty()) {
            pieChart.setData(null);
            pieChart.setCenterText("Sin gastos");
            pieChart.invalidate();
            return;
        }

        int[] palette = {
                R.color.primary_electric_blue, R.color.expense_red, R.color.income_green,
                R.color.warning_orange, R.color.accent_blue, R.color.expense_coral,
                R.color.success_green, R.color.gray_dark, R.color.primary_blue_light
        };
        List<Map.Entry<String, Long>> entries = new ArrayList<>(categoryExpenses.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Long> entry = entries.get(i);
            pieEntries.add(new PieEntry(entry.getValue() / 100f, entry.getKey()));
            colors.add(ContextCompat.getColor(requireContext(), palette[i % palette.length]));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText("Gastos\n" + CurrencyUtils.formatCurrency(requireContext(), totalExpense));
        pieChart.animateY(500);
        pieChart.invalidate();
    }

    private void updateLineChart() {
        long[] range = currentRange();
        long start = range[0];
        long end = range[1];

        List<String> labels = new ArrayList<>();
        long[] bucketDeltas;
        if (mode == MODE_YEAR) {
            bucketDeltas = new long[12];
            for (int i = 0; i < 12; i++) {
                labels.add(DateUtils.formatMonthName(i + 1));
            }
        } else if (mode == MODE_WEEK) {
            bucketDeltas = new long[7];
            for (int i = 0; i < 7; i++) {
                labels.add(DateUtils.formatDayMonth(DateUtils.addDays(start, i)));
            }
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(start);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(end);
            while (!cal.after(endCalendar)) {
                labels.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            bucketDeltas = new long[labels.size()];
        }

        List<Transaction> sorted = new ArrayList<>(transactions);
        Collections.sort(sorted, Comparator.comparingLong(Transaction::getDate));
        for (Transaction transaction : sorted) {
            if (transaction.getDate() < start || transaction.getDate() > end) {
                continue;
            }
            int index = bucketIndex(transaction.getDate(), start);
            if (index < 0 || index >= bucketDeltas.length) {
                continue;
            }
            long delta = "INCOME".equals(transaction.getType())
                    ? transaction.getAmountInCents() : -transaction.getAmountInCents();
            bucketDeltas[index] += delta;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();
        chartLabels.add("Inicio");
        entries.add(new Entry(0f, startingBalance / 100f));
        long running = startingBalance;
        for (int i = 0; i < bucketDeltas.length; i++) {
            running += bucketDeltas[i];
            entries.add(new Entry(i + 1f, running / 100f));
            chartLabels.add(labels.get(i));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        int blue = ContextCompat.getColor(requireContext(), R.color.primary_electric_blue);
        dataSet.setColor(blue);
        dataSet.setLineWidth(2.2f);
        dataSet.setCircleColor(blue);
        dataSet.setCircleRadius(2.2f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(blue);
        dataSet.setFillAlpha(40);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                return index >= 0 && index < chartLabels.size() ? chartLabels.get(index) : "";
            }
        });
        lineChart.getXAxis().setLabelCount(Math.min(7, chartLabels.size()), false);
        lineChart.animateX(500);
        lineChart.invalidate();
    }

    private int bucketIndex(long timestamp, long start) {
        if (mode == MODE_YEAR) {
            return DateUtils.getMonth(timestamp) - 1;
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(start);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long dayMillis = calendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        return Math.round(dayMillis / (float) DateUtils.DAY_MILLIS);
    }

    private void updateTopCategories(Map<String, Long> categoryExpenses, long totalExpense) {
        topCategories.removeAllViews();
        if (categoryExpenses.isEmpty()) {
            tvNoCategories.setVisibility(View.VISIBLE);
            return;
        }
        tvNoCategories.setVisibility(View.GONE);

        List<Map.Entry<String, Long>> entries = new ArrayList<>(categoryExpenses.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        int limit = Math.min(5, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Long> entry = entries.get(i);
            int percent = totalExpense <= 0 ? 0
                    : (int) Math.round(entry.getValue() * 100.0 / totalExpense);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(7), 0, dp(7));

            TextView name = new TextView(requireContext());
            name.setText(entry.getKey());
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            name.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface));
            name.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView amount = new TextView(requireContext());
            amount.setText(CurrencyUtils.formatCurrency(requireContext(), entry.getValue())
                    + "  (" + percent + "%)");
            amount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));

            row.addView(name);
            row.addView(amount);
            topCategories.addView(row);
        }
    }

    private void updateMonthlyBalance(long income, long expense) {
        long net = income - expense;
        if (mode == MODE_YEAR) {
            Map<Integer, Long> monthly = new LinkedHashMap<>();
            for (int i = 1; i <= 12; i++) {
                monthly.put(i, 0L);
            }
            for (Transaction transaction : transactions) {
                int month = DateUtils.getMonth(transaction.getDate());
                long delta = "INCOME".equals(transaction.getType())
                        ? transaction.getAmountInCents() : -transaction.getAmountInCents();
                monthly.put(month, monthly.get(month) + delta);
            }
            StringBuilder builder = new StringBuilder("Balance mensual:\n");
            for (Map.Entry<Integer, Long> entry : monthly.entrySet()) {
                builder.append(DateUtils.formatMonthName(entry.getKey()))
                        .append(": ")
                        .append(CurrencyUtils.formatCurrency(requireContext(), entry.getValue()))
                        .append("\n");
            }
            tvMonthlyBalance.setText(builder.toString().trim());
        } else {
            tvMonthlyBalance.setText("Balance del período: "
                    + CurrencyUtils.formatCurrency(requireContext(), net));
        }
    }

    private void setupCharts() {
        int textColor = themeColor(com.google.android.material.R.attr.colorOnSurface);
        int gridColor = themeColor(com.google.android.material.R.attr.colorOutlineVariant);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(textColor);
        barChart.getAxisLeft().setGridColor(gridColor);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getXAxis().setTextColor(textColor);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.setNoDataText("Sin movimientos en el período");
        barChart.setNoDataTextColor(textColor);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(textColor);
        lineChart.getAxisLeft().setGridColor(gridColor);
        lineChart.getXAxis().setTextColor(textColor);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.setNoDataText("Sin movimientos en el período");
        lineChart.setNoDataTextColor(textColor);

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

    private int themeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }
}
