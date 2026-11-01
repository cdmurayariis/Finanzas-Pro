package com.example.aplicacion_financiero.ui.transactions;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.Period;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.ui.adapter.TransactionAdapter;
import com.example.aplicacion_financiero.ui.common.AnimatedFragment;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.PrefsManager;
import com.example.aplicacion_financiero.utils.UiAnimations;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TransactionsFragment extends AnimatedFragment {

    private RecyclerView rvTransactions;
    private View emptyState;
    private TextView tvResultsCount;
    private TextInputEditText etSearch;
    private MaterialAutoCompleteTextView filterType, filterCategory, filterPeriod, filterSort;

    private TransactionsViewModel viewModel;
    private TransactionAdapter adapter;

    private final List<Transaction> allTransactions = new ArrayList<>();
    private final List<String> categoryNames = new ArrayList<>();
    private final List<Period> periodOptions = new ArrayList<>();

    private String selectedType = "Todos";
    private String selectedCategory = "Todas";
    private int selectedPeriodIndex = 0;
    private int selectedSortIndex = 0;
    private int firstDay = PrefsManager.DEFAULT_FIRST_DAY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupFilters();
        setupSearch();
        setupButtons(view);
        observeData();

        return view;
    }

    private void initializeViews(View view) {
        rvTransactions = view.findViewById(R.id.rv_transactions);
        emptyState = view.findViewById(R.id.layout_empty_transactions);
        tvResultsCount = view.findViewById(R.id.tv_results_count);
        etSearch = view.findViewById(R.id.et_search);
        filterType = view.findViewById(R.id.act_filter_type);
        filterCategory = view.findViewById(R.id.act_filter_category);
        filterPeriod = view.findViewById(R.id.act_filter_period);
        filterSort = view.findViewById(R.id.act_filter_sort);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(adapter);
        adapter.setOnTransactionClickListener(this::showTransactionOptions);
    }

    private void setupFilters() {
        setDropDown(filterType, new String[]{"Todos", "Ingresos", "Gastos"});
        filterType.setText(selectedType, false);
        filterType.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = (String) parent.getItemAtPosition(position);
            applyFilters();
        });

        setDropDown(filterCategory, new String[]{"Todas"});
        filterCategory.setText(selectedCategory, false);
        filterCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = (String) parent.getItemAtPosition(position);
            applyFilters();
        });

        List<String> periodLabels = new ArrayList<>();
        periodLabels.add("Todos los per\u00EDodos");
        firstDay = PrefsManager.getFirstDayOfPeriod(requireContext());
        long currentStart = DateUtils.getPeriodStartForDate(System.currentTimeMillis(), firstDay);
        Calendar base = Calendar.getInstance();
        base.setTimeInMillis(currentStart);
        for (int i = 0; i < 12; i++) {
            Calendar cal = (Calendar) base.clone();
            cal.add(Calendar.MONTH, -i);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            periodOptions.add(new Period(year, month));
            periodLabels.add(DateUtils.formatMonthYear(year, month));
        }
        setDropDown(filterPeriod, periodLabels.toArray(new String[0]));
        filterPeriod.setText(periodLabels.get(0), false);
        filterPeriod.setOnItemClickListener((parent, view, position, id) -> {
            selectedPeriodIndex = position;
            applyFilters();
        });

        String[] sortOptions = {"Fecha (reciente)", "Fecha (antigua)", "Monto (mayor)", "Monto (menor)"};
        setDropDown(filterSort, sortOptions);
        filterSort.setText(sortOptions[0], false);
        filterSort.setOnItemClickListener((parent, view, position, id) -> {
            selectedSortIndex = position;
            applyFilters();
        });
    }

    private void setDropDown(MaterialAutoCompleteTextView view, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, values);
        view.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupButtons(View root) {
        View addButton = root.findViewById(R.id.btn_add_transaction);
        UiAnimations.pressScale(addButton);
        addButton.setOnClickListener(v ->
                TransactionFormDialog.show(this, null, "EXPENSE",
                        (transaction, isEdit) -> viewModel.insertTransaction(transaction)));
    }

    private void observeData() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions.clear();
            if (transactions != null) {
                allTransactions.addAll(transactions);
            }
            applyFilters();
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryNames.clear();
            categoryNames.add("Todas");
            if (categories != null) {
                for (Category category : categories) {
                    if (!categoryNames.contains(category.getName())) {
                        categoryNames.add(category.getName());
                    }
                }
            }
            setDropDown(filterCategory, categoryNames.toArray(new String[0]));
            filterCategory.setText(selectedCategory, false);
        });
    }

    private void applyFilters() {
        if (adapter == null) {
            return;
        }
        String query = etSearch.getText() == null ? ""
                : etSearch.getText().toString().trim().toLowerCase(Locale.getDefault());

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (!selectedType.equals("Todos")) {
                boolean income = "INCOME".equals(transaction.getType());
                if (selectedType.equals("Ingresos") && !income) {
                    continue;
                }
                if (selectedType.equals("Gastos") && income) {
                    continue;
                }
            }

            if (!selectedCategory.equals("Todas")
                    && !selectedCategory.equals(transaction.getCategory())) {
                continue;
            }

            if (selectedPeriodIndex > 0 && selectedPeriodIndex <= periodOptions.size()) {
                Period period = periodOptions.get(selectedPeriodIndex - 1);
                long start = DateUtils.getPeriodStart(period.getYear(), period.getMonth(), firstDay);
                long end = DateUtils.getPeriodEnd(period.getYear(), period.getMonth(), firstDay);
                if (transaction.getDate() < start || transaction.getDate() > end) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                String description = transaction.getDescription() == null
                        ? "" : transaction.getDescription().toLowerCase(Locale.getDefault());
                String note = transaction.getNote() == null
                        ? "" : transaction.getNote().toLowerCase(Locale.getDefault());
                if (!description.contains(query) && !note.contains(query)) {
                    continue;
                }
            }

            filtered.add(transaction);
        }

        if (selectedSortIndex == 0) {
            Collections.sort(filtered, (a, b) -> Long.compare(b.getDate(), a.getDate()));
        } else if (selectedSortIndex == 1) {
            Collections.sort(filtered, (a, b) -> Long.compare(a.getDate(), b.getDate()));
        } else if (selectedSortIndex == 2) {
            Collections.sort(filtered, (a, b) -> Long.compare(b.getAmountInCents(), a.getAmountInCents()));
        } else {
            Collections.sort(filtered, (a, b) -> Long.compare(a.getAmountInCents(), b.getAmountInCents()));
        }

        adapter.setTransactions(filtered);
        tvResultsCount.setText(filtered.size() + (filtered.size() == 1 ? " movimiento" : " movimientos"));
        boolean empty = filtered.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showTransactionOptions(Transaction transaction) {
        String[] options = {"Editar", "Eliminar"};
        new MaterialAlertDialogBuilder(requireContext())
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
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar movimiento")
                .setMessage("¿Estás seguro de eliminar este movimiento? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.deleteTransaction(transaction))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
