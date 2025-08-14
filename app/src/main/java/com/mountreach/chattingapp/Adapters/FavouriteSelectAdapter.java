package com.mountreach.chattingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mountreach.chattingapp.Model.User;
import com.mountreach.chattingapp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavouriteSelectAdapter extends RecyclerView.Adapter<FavouriteSelectAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private Set<Integer> selectedPositions = new HashSet<>();

    public FavouriteSelectAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users != null ? users : new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvUserName);
            checkBox = view.findViewById(R.id.checkboxUser);
        }
    }

    @NonNull
    @Override
    public FavouriteSelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteSelectAdapter.ViewHolder holder, int position) {
        User user = users.get(position);

        // Avoid null username
        holder.tvName.setText(user.getUserName() != null ? user.getUserName() : "Unknown");

        // Remove old listener
        holder.checkBox.setOnCheckedChangeListener(null);

        // Set checkbox state
        holder.checkBox.setChecked(selectedPositions.contains(position));

        // Handle checkbox state changes
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
        });

        // Handle click on the item itself
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !holder.checkBox.isChecked();
            holder.checkBox.setChecked(newState);
        });
    }

    public ArrayList<User> getSelectedUsers() {
        ArrayList<User> selected = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos >= 0 && pos < users.size()) {
                selected.add(users.get(pos));
            }
        }
        return selected;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
