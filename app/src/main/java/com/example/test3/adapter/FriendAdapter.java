package com.example.test3.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test3.ChatActivity;
import com.example.test3.LoginActivity;
import com.example.test3.R;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context context;
    private List<FriendItem> friendList;
    private OnFriendClickListener listener;

    public FriendAdapter(Context context, List<FriendItem> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    public interface OnFriendClickListener {
        void onDeleteClick(FriendItem friend);

        void onFriendClick(FriendItem friend);
    }

    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendItem friend = friendList.get(position);
        holder.usernameTextView.setText(friend.getUsername());
        holder.emailTextView.setText(friend.getEmail());

        // 设置最后一条消息
        // TODO: 从数据库获取最后一条消息
        holder.lastMessageTextView.setText("我们已经是好友了，来聊天吧！");

        // 设置删除按钮点击事件
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(friend);
            }
        });

        // 设置整个项目的点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.i("abc", "111");
                listener.onFriendClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void setFriendList(List<FriendItem> friendList) {
        this.friendList = friendList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView emailTextView;
        TextView lastMessageTextView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.friend_username);
            emailTextView = itemView.findViewById(R.id.friend_email);
            lastMessageTextView = itemView.findViewById(R.id.last_message);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public static class FriendItem {
        private long id;
        private String username;
        private String email;

        public FriendItem(long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }
}