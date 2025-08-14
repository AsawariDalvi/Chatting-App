package com.mountreach.chattingapp.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mountreach.chattingapp.Model.Message;
import com.mountreach.chattingapp.R;

import java.util.ArrayList;
import java.util.Date;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final ArrayList<Message> messageList;
    private final String senderUid;

    public MessageAdapter(Context context, ArrayList<Message> messageList, String senderUid) {
        this.context = context;
        this.messageList = messageList;
        this.senderUid = senderUid;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message != null && senderUid.equals(message.getSenderId())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sender, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_receiver, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (message == null) return;

        // Format timestamp safely
        String timeFormatted = "";
        long timeMillis = message.getTimestampMillis();
        if (timeMillis > 0) {
            try {
                timeFormatted = DateFormat.format("hh:mm a", new Date(timeMillis)).toString();
            } catch (Exception e) {
                e.printStackTrace();
                timeFormatted = "";
            }
        }
        if (message.getTimestamp() != null) {
            long millis = message.getTimestampMillis();
            // Format and show time
        } else {
            // Optionally skip or show "Just now"
        }
        Log.d("LoadedMessage", "Msg: " + message.getMessage() + ", Time: " + message.getTimestamp());


        // Set message and time safely
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.msgText.setText(!TextUtils.isEmpty(message.getMessage()) ? message.getMessage() : "");
            sentHolder.msgTime.setText(timeFormatted);
        } else {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.msgText.setText(!TextUtils.isEmpty(message.getMessage()) ? message.getMessage() : "");
            receivedHolder.msgTime.setText(timeFormatted);
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView msgText, msgTime;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = itemView.findViewById(R.id.tvSenderMsg);
            msgTime = itemView.findViewById(R.id.tvSenderTime);
        }
    }

    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView msgText, msgTime;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = itemView.findViewById(R.id.tvReceiverMsg);
            msgTime = itemView.findViewById(R.id.tvReceiverTime);
        }
    }

}
