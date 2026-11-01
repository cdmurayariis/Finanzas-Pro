package com.example.aplicacion_financiero.ui.adapter;

import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.BudgetCategoryItem;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.UiAnimations;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class BudgetCategoryAdapter extends RecyclerView.Adapter<BudgetCategoryAdapter.ViewHolder> {

    private static final Object PAYLOAD_SELECTION = new Object();

    public interface OnCategoryClickListener {
        void onCategoryClick(BudgetCategoryItem item);
    }

    private final List<BudgetCategoryItem> items = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private String selectedCategory;

    public BudgetCategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<BudgetCategoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_SELECTION)) {
            holder.applySelection(selectedCategory != null
                    && selectedCategory.equals(items.get(position).getCategory()), true);
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int findPosition(String category) {
        if (category == null) {
            return RecyclerView.NO_POSITION;
        }
        for (int i = 0; i < items.size(); i++) {
            if (category.equals(items.get(i).getCategory())) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final View categoryIcon;
        private final TextView name, percent, spent, limit, alert;
        private final ProgressBar progress;
        private final int defaultStrokeColor;
        private final int selectedStrokeColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            categoryIcon = itemView.findViewById(R.id.iv_budget_category_icon);
            name = itemView.findViewById(R.id.tv_budget_category_name);
            percent = itemView.findViewById(R.id.tv_budget_category_percent);
            spent = itemView.findViewById(R.id.tv_budget_category_spent);
            limit = itemView.findViewById(R.id.tv_budget_category_limit);
            alert = itemView.findViewById(R.id.tv_budget_category_alert);
            progress = itemView.findViewById(R.id.progress_budget_category);
            defaultStrokeColor = themeColor(com.google.android.material.R.attr.colorOutlineVariant);
            selectedStrokeColor = ContextCompat.getColor(itemView.getContext(),
                    R.color.primary_electric_blue);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                BudgetCategoryItem item = items.get(position);
                int previousPosition = findPosition(selectedCategory);
                boolean selected = !item.getCategory().equals(selectedCategory);
                selectedCategory = selected ? item.getCategory() : null;

                applySelection(selected, true);
                if (previousPosition != RecyclerView.NO_POSITION && previousPosition != position) {
                    notifyItemChanged(previousPosition, PAYLOAD_SELECTION);
                }
                if (listener != null) {
                    listener.onCategoryClick(item);
                }
            });
        }

        void applySelection(boolean selected, boolean animate) {
            UiAnimations.updateCardSelection(card, categoryIcon, selected,
                    defaultStrokeColor, selectedStrokeColor, animate);
        }

        private int themeColor(int attr) {
            TypedValue typedValue = new TypedValue();
            itemView.getContext().getTheme().resolveAttribute(attr, typedValue, true);
            return typedValue.data;
        }

        void bind(BudgetCategoryItem item) {
            name.setText(item.getCategory());
            spent.setText("Gastado: " + CurrencyUtils.formatCurrency(itemView.getContext(),
                    item.getSpentInCents()));

            int percentValue = item.getPercent();
            progress.setProgress(0);
            UiAnimations.animateProgress(progress, percentValue);
            applySelection(selectedCategory != null
                    && selectedCategory.equals(item.getCategory()), false);

            if (item.getLimitInCents() <= 0) {
                percent.setText("—");
                percent.setTextColor(ContextCompat.getColor(itemView.getContext(),
                        R.color.gray_dark));
                limit.setText("Sin límite");
                alert.setVisibility(View.GONE);
                progress.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.gray_medium)));
                return;
            }

            limit.setText("Límite: " + CurrencyUtils.formatCurrency(itemView.getContext(),
                    item.getLimitInCents()));
            percent.setText(percentValue + "%");

            int color;
            if (percentValue >= 100) {
                color = R.color.expense_red;
                alert.setText("Límite superado");
                alert.setVisibility(View.VISIBLE);
            } else if (percentValue >= 80) {
                color = R.color.warning_orange;
                alert.setText("Cerca del límite");
                alert.setVisibility(View.VISIBLE);
            } else {
                color = R.color.income_green;
                alert.setVisibility(View.GONE);
            }

            int resolved = ContextCompat.getColor(itemView.getContext(), color);
            percent.setTextColor(resolved);
            alert.setTextColor(resolved);
            progress.setProgressTintList(ColorStateList.valueOf(resolved));
        }
    }
}
