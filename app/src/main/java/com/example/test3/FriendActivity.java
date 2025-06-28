package com.example.test3;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Context;
import android.content.IntentFilter;

import com.example.test3.provider.FriendProvider;
import com.example.test3.service.FriendService;
import com.example.test3.broadcast.FriendReceiver;
import com.example.test3.util.Util;

public class FriendActivity extends AppCompatActivity {

    private EditText searchInput;
    private Button searchButton;
    private Button addButton;
    private LocalBroadcastManager localBroadcastManager;
    private FriendReceiver friendReceiver;
    private DatabaseHelper dbHelper;
    // 定义广播Action
    public static final String ACTION_FRIEND_ADD = "com.example.test3.ACTION_FRIEND_ADD";
    public static final String ACTION_FRIEND_SEARCH = "com.example.test3.ACTION_FRIEND_SEARCH";
    public static final String ACTION_FRIEND_NOT_FOUND = "com.example.test3.ACTION_FRIEND_NOT_FOUND";
    public static final String NONE_INPUT = "com.example.test3.NONE_INPUT";
    public static final String ADD_FAILURE = "com.example.test3.ADD_FAILURE";
    public static final String SEARCH_SUCCESS = "com.example.test3.SEARCH_SUCCESS";
    public static final String SEARCH_FIRST = "com.example.test3.SEARCH_FIRST";
    public static final String ALREADY_FRIEND = "com.example.test3.ALREADY_FRIEND";
    public static final String ADD_SELF = "com.example.test3.ADD_SELF";
    public static final String EXTRA_FRIEND_NAME = "friend_name";

    public static final String EXTRA_SEARCH_QUERY = "search_query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // 初始化DatabaseHelper
        dbHelper = new DatabaseHelper(this);
        // 初始化LocalBroadcastManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        friendReceiver = new FriendReceiver();
        // 注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        initReceiver(intentFilter);

        // 启动FriendService
        Intent serviceIntent = new Intent(this, FriendService.class);
        startService(serviceIntent);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        addButton = findViewById(R.id.add_button);

        searchButton.setOnClickListener(v -> searchUser());
        addButton.setOnClickListener(v -> addFriend());
    }

    private void searchUser() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            broad(NONE_INPUT, EXTRA_SEARCH_QUERY, query);
            return;
        }
        // 发送搜索广播
        broad(ACTION_FRIEND_SEARCH, EXTRA_SEARCH_QUERY, query);

        // 执行搜索
        Uri searchUri = Uri.withAppendedPath(FriendProvider.SEARCH_URI, query);
        Cursor cursor = getContentResolver().query(searchUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // 处理搜索结果
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME);
            long userId = cursor.getLong(idIndex);
            String username = cursor.getString(usernameIndex);

            broad(SEARCH_SUCCESS, EXTRA_FRIEND_NAME, username);
            // 显示添加按钮
            addButton.setVisibility(View.VISIBLE);
            addButton.setTag(userId);

            cursor.close();
        } else {
            broad(ACTION_FRIEND_NOT_FOUND);
            addButton.setVisibility(View.GONE);
        }
    }

    private void addFriend() {
        Object tag = addButton.getTag();
        if (tag == null) {
            broad(SEARCH_FIRST);
            return;
        }
        long friendId = (Long) tag;
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        long currentUserId = prefs.getLong(LoginActivity.KEY_USER_ID, -1);

        if (currentUserId == -1) {
            broad(ADD_FAILURE);
            return;
        }

        // 检查是否添加自己
        if (currentUserId == friendId) {
            broad(ADD_SELF);
            return;
        }

        // 检查是否已经是好友
        if (dbHelper.isFriend(currentUserId, friendId)) {
            broad(ALREADY_FRIEND);
            return;
        }

        // 添加好友到数据库
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, currentUserId);
        values.put(DatabaseHelper.COLUMN_FRIEND_ID, friendId);

        Uri uri = getContentResolver().insert(FriendProvider.CONTENT_URI, values);
        if (uri != null) {
            // 获取好友用户名
            String friendUsername = dbHelper.getUsernameById(friendId);
            if (friendUsername != null) {
                // 发送添加好友成功的广播
                broad(ACTION_FRIEND_ADD, EXTRA_FRIEND_NAME, friendUsername);
                Toast.makeText(this,"已添加好友："+friendUsername,Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                broad(ADD_FAILURE);
            }
        } else {
            broad(ADD_FAILURE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        if (friendReceiver != null) {
            localBroadcastManager.unregisterReceiver(friendReceiver);
        }
        // 停止FriendService
        stopService(new Intent(this, FriendService.class));
    }
    public void initReceiver(IntentFilter intentFilter){
        intentFilter.addAction(ACTION_FRIEND_ADD);
        intentFilter.addAction(ACTION_FRIEND_SEARCH);
        intentFilter.addAction(ACTION_FRIEND_NOT_FOUND);
        intentFilter.addAction(SEARCH_SUCCESS);
        intentFilter.addAction(SEARCH_FIRST);
        intentFilter.addAction(NONE_INPUT);
        intentFilter.addAction(ALREADY_FRIEND);
        intentFilter.addAction(ADD_SELF);
        localBroadcastManager.registerReceiver(friendReceiver, intentFilter);
    }
    public void broad(String flag, String tag, String value) {
        Intent intent = new Intent(flag);
        intent.putExtra(tag, value);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void broad(String flag) {
        Intent intent = new Intent(flag);
        localBroadcastManager.sendBroadcast(intent);
    }

}