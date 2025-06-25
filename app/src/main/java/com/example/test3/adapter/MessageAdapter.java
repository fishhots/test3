package com.example.test3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test3.R;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private long currentUserId;

    public MessageAdapter(List<ChatMessage> messages, long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (message.getSenderId() == currentUserId) {
            // 显示发送的消息
            holder.sentLayout.setVisibility(View.VISIBLE);
            holder.receivedLayout.setVisibility(View.GONE);
            holder.sentMessageText.setText(message.getMessage());
        } else {
            // 显示接收的消息
            holder.sentLayout.setVisibility(View.GONE);
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.receivedMessageText.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout sentLayout;
        LinearLayout receivedLayout;
        TextView sentMessageText;
        TextView receivedMessageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            sentLayout = itemView.findViewById(R.id.sent_message_layout);
            receivedLayout = itemView.findViewById(R.id.received_message_layout);
            sentMessageText = itemView.findViewById(R.id.sent_message_text);
            receivedMessageText = itemView.findViewById(R.id.received_message_text);
        }
    }

    public static class ChatMessage {
        private long senderId;
        private String message;
        private long timestamp;

        public ChatMessage(long senderId, String message) {
            this.senderId = senderId;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public long getSenderId() {
            return senderId;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}