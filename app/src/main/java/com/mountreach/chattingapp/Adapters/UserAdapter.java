package com.mountreach.chattingapp.Adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.mountreach.chattingapp.Model.User;
import com.mountreach.chattingapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private List<User> userList;
    private final int layoutType;
    private final OnUserClickListener listener;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public static final int TYPE_CHAT = 0;
    public static final int TYPE_SIMPLE = 1;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserClickListener listener, int layoutType) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        this.layoutType = layoutType;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile;
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTime;
        ImageView btnWhatsApp;

        public UserViewHolder(@NonNull View itemView, int layoutType) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            tvName = itemView.findViewById(R.id.tvUserName);

            if (layoutType == TYPE_CHAT) {
                btnWhatsApp = itemView.findViewById(R.id.btnWhatsApp);
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            } else {
                btnWhatsApp = null;
                tvLastMessage = null;
                tvTime = null;
            }
        }
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = layoutType == TYPE_CHAT
                ? inflater.inflate(R.layout.item_chat_user, parent, false)
                : inflater.inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view, layoutType);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getUserName());
        String phone = user.getFullPhone(); // Full phone number like "919876543210"

        // ✅ Only attach WhatsApp click if layout is TYPE_CHAT
        if (layoutType == TYPE_CHAT && holder.btnWhatsApp != null) {
            holder.btnWhatsApp.setOnClickListener(v -> {
                openWhatsApp(context, phone);
            });
        }

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.icon_profile)
                    .into(holder.ivUserProfile);
        } else {
            holder.ivUserProfile.setImageResource(R.drawable.icon_profile);
        }

        if (layoutType == TYPE_CHAT && holder.tvLastMessage != null && holder.tvTime != null) {
            fetchLastMessage(user.getUid(), holder.tvLastMessage, holder.tvTime);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    private void openWhatsApp(Context context, String phoneNumber) {
        String url = "https://wa.me/" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setPackage("com.whatsapp");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLastMessage(String otherUserUid, TextView tvLastMessage, TextView tvTime) {
        String chatId = getChatId(currentUserUid, otherUserUid);

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastMsg = queryDocumentSnapshots.getDocuments().get(0);
                        String message = lastMsg.getString("message");

                        Object timestampObj = lastMsg.get("timestamp");
                        Timestamp timestamp = null;

                        if (timestampObj instanceof Timestamp) {
                            timestamp = (Timestamp) timestampObj;
                        } else if (timestampObj instanceof Long) {
                            timestamp = new Timestamp(new Date((Long) timestampObj));
                        }

                        tvLastMessage.setText(message != null ? message : "");

                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                            tvTime.setText(sdf.format(date));
                        } else {
                            tvTime.setText("");
                        }
                    } else {
                        tvLastMessage.setText("No messages yet");
                        tvTime.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    tvLastMessage.setText("Failed to load");
                    tvTime.setText("");
                });
    }

    private String getChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }
}
