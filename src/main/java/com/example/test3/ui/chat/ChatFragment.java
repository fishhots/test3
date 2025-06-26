package com.example.test3.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test3.ChatActivity;
import com.example.test3.DatabaseHelper;
import com.example.test3.FriendActivity;
import com.example.test3.LoginActivity;
import com.example.test3.R;
import com.example.test3.adapter.FriendAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements FriendAdapter.OnFriendClickListener {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FriendAdapter friendAdapter;
    private DatabaseHelper databaseHelper;
    private List<FriendAdapter.FriendItem> friendList;
    private ActivityResultLauncher<Intent> addFriendLauncher;
    private long currentUserId = -1;
    private Button add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 从SharedPreferences获取用户ID
        SharedPreferences prefs = requireActivity().getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getLong(LoginActivity.KEY_USER_ID, -1);
        String currentUserName = prefs.getString(LoginActivity.KEY_USERNAME, "");

        // 注册Activity Result回调
        addFriendLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        loadFriends();
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        databaseHelper = new DatabaseHelper(requireContext());
        friendList = new ArrayList<>();

        // 初始化视图
        recyclerView = root.findViewById(R.id.friends_recycler_view);
        emptyView = root.findViewById(R.id.empty_view);
        add = root.findViewById(R.id.fab_add_friend);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendAdapter = new FriendAdapter(getContext(), friendList);
        friendAdapter.setOnFriendClickListener(this);
        recyclerView.setAdapter(friendAdapter);

        // 添加好友按钮点击事件
        add.setOnClickListener(v -> {
            if (currentUserId != -1) {
                Intent intent = new Intent(getActivity(), FriendActivity.class);
                addFriendLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "获取用户信息失败，请重新登录", Toast.LENGTH_SHORT).show();
            }
        });

        // 加载好友列表
        loadFriends();

        return root;
    }

    private void loadFriends() {
        if (currentUserId == -1) {
            Toast.makeText(getContext(), "获取用户信息失败，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前用户的好友列表
        Cursor cursor = databaseHelper.getFriends(currentUserId);

        friendList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int use = cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME);
                int ema = cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL);
                long friendId = cursor.getLong(id);
                String username = cursor.getString(use);
                String email = cursor.getString(ema);

                friendList.add(new FriendAdapter.FriendItem(friendId, username, email));
            } while (cursor.moveToNext());
            cursor.close();
        }

        // 更新UI显示
        if (friendList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        friendAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteClick(FriendAdapter.FriendItem friend) {
        if (currentUserId == -1) {
            Toast.makeText(getContext(), "获取用户信息失败，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 删除好友
        boolean success = databaseHelper.deleteFriend(currentUserId, friend.getId());
        if (success) {
            Toast.makeText(getContext(), "已删除好友", Toast.LENGTH_SHORT).show();
            loadFriends(); // 重新加载好友列表
        } else {
            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFriendClick(FriendAdapter.FriendItem friend) {
        // 从SharedPreferences获取当前用户信息
        SharedPreferences prefs = requireActivity().getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        String currentUserName = prefs.getString(LoginActivity.KEY_USERNAME, "");
        Log.i("abc","2");
        // 创建聊天意图
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_FRIEND_ID, friend.getId());
        intent.putExtra(ChatActivity.EXTRA_FRIEND_NAME, friend.getUsername());
        intent.putExtra(ChatActivity.EXTRA_USER_ID, currentUserId);
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, currentUserName);
        Log.i("abc","3");
        // 启动ChatActivity
        startActivity(intent);
        Log.i("abc","4");
    }
}