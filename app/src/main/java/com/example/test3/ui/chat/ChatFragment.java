package com.example.test3.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentUris;
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
import com.example.test3.provider.FriendProvider;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements FriendAdapter.OnFriendClickListener {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FriendAdapter friendAdapter;
    private List<FriendAdapter.FriendItem> friendList;
    private ActivityResultLauncher<Intent> addFriendLauncher;
    private long currentUserId = -1;
    private Button add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getLong(LoginActivity.KEY_USER_ID, -1);
        String currentUserName = prefs.getString(LoginActivity.KEY_USERNAME, "");

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

        friendList = new ArrayList<>();

        recyclerView = root.findViewById(R.id.friends_recycler_view);
        emptyView = root.findViewById(R.id.empty_view);
        add = root.findViewById(R.id.fab_add_friend);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendAdapter = new FriendAdapter(getContext(), friendList);
        friendAdapter.setOnFriendClickListener(this);
        recyclerView.setAdapter(friendAdapter);

        add.setOnClickListener(v -> {
            if (currentUserId != -1) {
                Intent intent = new Intent(getActivity(), FriendActivity.class);
                addFriendLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "获取用户信息失败，请重新登录", Toast.LENGTH_SHORT).show();
            }
        });

        loadFriends();

        return root;
    }

    private void loadFriends() {
        if (currentUserId == -1) {
            Toast.makeText(getContext(), "获取用户信息失败，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = requireActivity().getContentResolver().query(
                FriendProvider.CONTENT_URI,
                null,
                null,
                new String[] { String.valueOf(currentUserId) },
                null);

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

        Uri deleteUri = ContentUris.withAppendedId(FriendProvider.CONTENT_URI, friend.getId());
        int count = requireActivity().getContentResolver().delete(
                deleteUri,
                null,
                new String[] { String.valueOf(currentUserId) });

        if (count > 0) {
            Toast.makeText(getContext(), "已删除好友", Toast.LENGTH_SHORT).show();
            loadFriends();
        } else {
            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFriendClick(FriendAdapter.FriendItem friend) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        String currentUserName = prefs.getString(LoginActivity.KEY_USERNAME, "");
        long currentUserId = prefs.getLong(LoginActivity.KEY_USER_ID, -1);

        Log.i("ChatFragment", "启动聊天，当前用户：" + currentUserName + "，好友：" + friend.getUsername());

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("userA", currentUserName);          // 当前用户
        intent.putExtra("userB", friend.getUsername());      // 聊天好友
        // 如果ChatActivity需要ID也可以传，但当前通过用户名查ID，暂不传ID
        // intent.putExtra("userAId", currentUserId);
        // intent.putExtra("userBId", friend.getId());

        startActivity(intent);

        Log.i("ChatFragment", "聊天界面已启动");
    }

}