package com.example.aplicacion_financiero.ui.transactions;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.database.FinanceDatabase;
import com.example.aplicacion_financiero.data.model.Category;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.utils.CategoryDefaults;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionFormDialog extends DialogFragment {

    public interface OnTransactionSaved {
        void onSaved(Transaction transaction, boolean isEdit);
    }

    private static final String ARG_ID = "id";
    private static final String ARG_TYPE = "type";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DATE = "date";
    private static final String ARG_PAYMENT = "payment";
    private static final String ARG_NOTE = "note";

    private OnTransactionSaved listener;

    private TextInputLayout tilAmount, tilCategory, tilDescription, tilDate;
    private TextInputEditText etAmount, etDescription, etDate, etNote;
    private MaterialAutoCompleteTextView etCategory, etPayment;
    private MaterialButtonToggleGroup typeToggle;

    private long editingId = -1L;
    private long selectedDate = System.currentTimeMillis();
    private String pendingCategory;
    private boolean suppressTypeChange;

    public static void show(@NonNull Fragment host, @Nullable Transaction editing,
                            @NonNull String defaultType, @NonNull OnTransactionSaved listener) {
        TransactionFormDialog dialog = new TransactionFormDialog();
        Bundle args = new Bundle();
        if (editing != null) {
            args.putLong(ARG_ID, editing.getId());
            args.putString(ARG_TYPE, editing.getType());
            args.putLong(ARG_AMOUNT, editing.getAmountInCents());
            args.putString(ARG_CATEGORY, editing.getCategory());
            args.putString(ARG_DESCRIPTION, editing.getDescription());
            args.putLong(ARG_DATE, editing.getDate());
            args.putString(ARG_PAYMENT, editing.getPaymentMethod());
            args.putString(ARG_NOTE, editing.getNote());
        } else {
            args.putLong(ARG_ID, -1L);
            args.putString(ARG_TYPE, defaultType);
        }
        dialog.setArguments(args);
        dialog.listener = listener;
        dialog.show(host.getParentFragmentManager(), "transaction_form");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments() == null ? new Bundle() : getArguments();
        editingId = args.getLong(ARG_ID, -1L);
        String type = args.getString(ARG_TYPE, "EXPENSE");
        pendingCategory = args.getString(ARG_CATEGORY);
        if (args.containsKey(ARG_DATE)) {
            selectedDate = args.getLong(ARG_DATE, System.currentTimeMillis());
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_transaction, null, false);

        tilAmount = view.findViewById(R.id.til_amount);
        tilCategory = view.findViewById(R.id.til_category);
        tilDescription = view.findViewById(R.id.til_description);
        tilDate = view.findViewById(R.id.til_date);
        etAmount = view.findViewById(R.id.et_amount);
        etCategory = view.findViewById(R.id.et_category);
        etDescription = view.findViewById(R.id.et_description);
        etDate = view.findViewById(R.id.et_date);
        etPayment = view.findViewById(R.id.et_payment_method);
        etNote = view.findViewById(R.id.et_note);
        typeToggle = view.findViewById(R.id.toggle_type);

        setupTypeToggle(type);
        setupPaymentMethod(args.getString(ARG_PAYMENT));
        setupDate();

        if (editingId > 0) {
            long amount = args.getLong(ARG_AMOUNT, 0L);
            etAmount.setText(CurrencyUtils.formatAmount(amount));
            etDescription.setText(args.getString(ARG_DESCRIPTION, ""));
            etNote.setText(args.getString(ARG_NOTE, ""));
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editingId > 0 ? "Editar movimiento" : "Nuevo movimiento")
                .setView(view)
                .setPositiveButton(editingId > 0 ? "Actualizar" : "Guardar", null)
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> save()));
        return dialog;
    }

    private void setupTypeToggle(String type) {
        typeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || suppressTypeChange) {
                return;
            }
            pendingCategory = null;
            loadCategories(checkedId == R.id.btn_type_income ? "INCOME" : "EXPENSE");
        });

        boolean income = "INCOME".equals(type);
        suppressTypeChange = true;
        typeToggle.check(income ? R.id.btn_type_income : R.id.btn_type_expense);
        suppressTypeChange = false;
        loadCategories(income ? "INCOME" : "EXPENSE");
    }

    private void loadCategories(String type) {
        FinanceDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> categories = FinanceDatabase.getDatabase(requireContext())
                    .categoryDao().getCategoriesByTypeSync(type);
            List<String> names = new ArrayList<>();
            for (Category category : categories) {
                names.add(category.getName());
            }
            if (names.isEmpty()) {
                names.addAll(CategoryDefaults.forType(type));
            }
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_list_item_1, names);
                etCategory.setAdapter(adapter);
                if (pendingCategory != null) {
                    etCategory.setText(pendingCategory, false);
                    pendingCategory = null;
                }
            });
        });
    }

    private void setupPaymentMethod(@Nullable String paymentCode) {
        String[] labels = {"Efectivo", "Tarjeta", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, labels);
        etPayment.setAdapter(adapter);
        etPayment.setText(labelForCode(paymentCode), false);
    }

    private String labelForCode(@Nullable String code) {
        if ("CARD".equals(code)) {
            return "Tarjeta";
        }
        if ("OTHER".equals(code)) {
            return "Otro";
        }
        return "Efectivo";
    }

    private String codeForLabel(String label) {
        if ("Tarjeta".equals(label)) {
            return "CARD";
        }
        if ("Otro".equals(label)) {
            return "OTHER";
        }
        return "CASH";
    }

    private void setupDate() {
        etDate.setText(DateUtils.formatDate(selectedDate));
        etDate.setOnClickListener(v -> openDatePicker());
        tilDate.setEndIconOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        DatePickerDialog picker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 12, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    selectedDate = selected.getTimeInMillis();
                    etDate.setText(DateUtils.formatDate(selectedDate));
                    tilDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void save() {
        tilAmount.setError(null);
        tilCategory.setError(null);
        tilDescription.setError(null);

        long amount = CurrencyUtils.toCents(textOf(etAmount));
        if (amount <= 0) {
            tilAmount.setError("Ingresa un monto mayor a 0");
            etAmount.requestFocus();
            return;
        }

        String category = textOf(etCategory);
        if (category.isEmpty()) {
            tilCategory.setError("Selecciona una categor\u00EDa");
            return;
        }

        String description = textOf(etDescription);
        if (description.isEmpty()) {
            tilDescription.setError("La descripci\u00F3n es obligatoria");
            etDescription.requestFocus();
            return;
        }

        String type = typeToggle.getCheckedButtonId() == R.id.btn_type_income
                ? "INCOME" : "EXPENSE";
        String payment = codeForLabel(textOf(etPayment));

        Transaction transaction = new Transaction(type, amount, category, description,
                selectedDate, payment, textOf(etNote));
        if (editingId > 0) {
            transaction.setId(editingId);
        }

        if (listener != null) {
            listener.onSaved(transaction, editingId > 0);
        }
        dismiss();
    }

    private String textOf(android.widget.TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }
}
