package com.sampleapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.history.OnboardingInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OnboardingHistoryAdapter extends RecyclerView.Adapter<OnboardingHistoryAdapter.ViewHolder> {
    private List<OnboardingInfo> items;

    public OnboardingHistoryAdapter(List<OnboardingInfo> historyItems) {
        this.items = historyItems;
    }

    public void setItems(List<OnboardingInfo> items) {
        this.items = items;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OnboardingInfo item = items.get(position);
        String formattedTime = Utils.getFormattedTimeStr(item.getOnboardingTime());
        String onboardingType = Utils.getFormattedOnboardingType(item.getOnboardingType());

        holder.textDeviceId.setText(item.getDeviceId());
        holder.textAccountId.setText(item.getAccountId());
        holder.textOnboardingTime.setText(formattedTime);
        holder.textOnboardingType.setText(onboardingType);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDeviceId;
        private final TextView textAccountId;
        private final TextView textOnboardingTime;
        private final TextView textOnboardingType;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textDeviceId = itemView.findViewById(R.id.text_device_id);
            textAccountId = itemView.findViewById(R.id.text_account_id);
            textOnboardingTime = itemView.findViewById(R.id.text_onboarding_time);
            textOnboardingType = itemView.findViewById(R.id.text_onboarding_type);
        }
    }
}
