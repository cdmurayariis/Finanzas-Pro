package com.example.aplicacion_financiero.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_financiero.R;
import com.example.aplicacion_financiero.data.model.SavingsGoal;
import com.example.aplicacion_financiero.utils.CurrencyUtils;
import com.example.aplicacion_financiero.utils.DateUtils;
import com.example.aplicacion_financiero.utils.UiAnimations;

import java.util.ArrayList;
import java.util.List;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.ViewHolder> {

    public interface OnGoalClickListener {
        void onGoalClick(SavingsGoal goal);
    }

    private final List<SavingsGoal> goals = new ArrayList<>();
    private final OnGoalClickListener listener;

    public SavingsGoalAdapter(OnGoalClickListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<SavingsGoal> newGoals) {
        goals.clear();
        if (newGoals != null) {
            goals.addAll(newGoals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(goals.get(position));
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name, deadline, percent, saved, target, remaining;
        private final ProgressBar progress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_savings_name);
            deadline = itemView.findViewById(R.id.tv_savings_deadline);
            percent = itemView.findViewById(R.id.tv_savings_percent);
            saved = itemView.findViewById(R.id.tv_savings_saved);
            target = itemView.findViewById(R.id.tv_savings_target);
            remaining = itemView.findViewById(R.id.tv_savings_remaining);
            progress = itemView.findViewById(R.id.progress_savings);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onGoalClick(goals.get(position));
                }
            });
        }

        void bind(SavingsGoal goal) {
            name.setText(goal.getName());
            deadline.setText(goal.getDeadline() == null
                    ? "Sin fecha límite"
                    : "Fecha límite: " + DateUtils.formatShortDate(goal.getDeadline()));

            int percentValue = (int) Math.min(100, Math.round(goal.getProgressPercentage()));
            percent.setText(percentValue + "%");
            progress.setProgress(0);
            UiAnimations.animateProgress(progress, percentValue);
            saved.setText("Ahorrado: " + CurrencyUtils.formatCurrency(itemView.getContext(),
                    goal.getSavedAmountInCents()));
            target.setText("Meta: " + CurrencyUtils.formatCurrency(itemView.getContext(),
                    goal.getTargetAmountInCents()));

            long remainingAmount = Math.max(0L,
                    goal.getTargetAmountInCents() - goal.getSavedAmountInCents());
            remaining.setText(remainingAmount == 0
                    ? "Meta completada"
                    : "Falta: " + CurrencyUtils.formatCurrency(itemView.getContext(), remainingAmount));
        }
    }
}
