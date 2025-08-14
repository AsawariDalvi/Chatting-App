package com.mountreach.chattingapp.Adapters;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mountreach.chattingapp.Model.SettingsItem;
import com.mountreach.chattingapp.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private List<SettingsItem> settingsList;
    private List<SettingsItem> allItems;

    public SettingsAdapter(List<SettingsItem> settingsList) {
        this.settingsList = settingsList;
        this.allItems = new ArrayList<>(settingsList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingsItem item = settingsList.get(position);
        holder.optionIcon.setImageResource(item.getIconResId());
        holder.optionTitle.setText(item.getTitle());
        holder.optionDesc.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public void filter(String query) {
        settingsList.clear();
        if (query.isEmpty()) {
            settingsList.addAll(allItems);
        } else {
            String lowerQuery = query.toLowerCase();
            for (SettingsItem item : allItems) {
                if (item.getTitle().toLowerCase().contains(lowerQuery)) {
                    settingsList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView optionIcon;
        TextView optionTitle, optionDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            optionIcon = itemView.findViewById(R.id.optionIcon);
            optionTitle = itemView.findViewById(R.id.optionTitle);
            optionDesc = itemView.findViewById(R.id.optionDesc);
        }
    }
}
