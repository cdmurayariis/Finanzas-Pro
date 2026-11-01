package com.example.aplicacion_financiero.ui.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.Transaction;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions == null ? new ArrayList<>() : transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDescription;
        private final TextView tvCategory;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final TextView tvIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_transaction_description);
            tvCategory = itemView.findViewById(R.id.tv_transaction_category);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvIcon = itemView.findViewById(R.id.tv_category_icon);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onTransactionClick(transactions.get(position));
                }
            });
        }

        public void bind(Transaction transaction) {
            boolean income = "INCOME".equals(transaction.getType());
            int accent = ContextCompat.getColor(itemView.getContext(),
                    income ? R.color.income_green : R.color.expense_red);

            tvDescription.setText(transaction.getDescription());
            String category = transaction.getCategory() == null ? "" : transaction.getCategory();
            String method = paymentLabel(transaction.getPaymentMethod());
            tvCategory.setText(category + " · " + method);
            tvDate.setText(DateUtils.formatShortDate(transaction.getDate()));
            tvAmount.setText(CurrencyUtils.formatSignedCurrency(itemView.getContext(),
                    transaction.getAmountInCents(), income));
            tvAmount.setTextColor(accent);

            String initial = category.trim().isEmpty()
                    ? (income ? "I" : "G")
                    : category.trim().substring(0, 1).toUpperCase(Locale.getDefault());
            tvIcon.setText(initial);
            if (tvIcon.getBackground() instanceof GradientDrawable) {
                GradientDrawable background = (GradientDrawable) tvIcon.getBackground().mutate();
                background.setColor(accent);
            }
        }

        private String paymentLabel(String code) {
            if ("CARD".equals(code)) {
                return "Tarjeta";
            }
            if ("OTHER".equals(code)) {
                return "Otro";
            }
            return "Efectivo";
        }
    }
}
