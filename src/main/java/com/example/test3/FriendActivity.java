package com.example.test3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;

public class FriendActivity extends AppCompatActivity {

    private EditText searchInput;
    private Button searchButton;
    private Button addButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        databaseHelper = new DatabaseHelper(this);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        addButton = findViewById(R.id.add_button);

        searchButton.setOnClickListener(v -> searchUser());
        addButton.setOnClickListener(v -> addFriend());
    }

    private void searchUser() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = databaseHelper.searchUsers(query);
        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
            String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
            long userId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));

            // 显示搜索结果
            showSearchResult(userId, username, email);
            cursor.close();
        } else {
            Toast.makeText(this, "未找到用户", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSearchResult(long userId, String username, String email) {
        // 这里可以显示一个对话框来确认是否添加好友
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("搜索结果")
                .setMessage("用户名: " + username + "\n邮箱: " + email)
                .setPositiveButton("添加好友", (dialog, which) -> {
                    addFriendById(userId);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void addFriend() {
        String username = searchInput.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        // 先搜索用户
        Cursor cursor = databaseHelper.searchUsers(username);
        if (cursor != null && cursor.moveToFirst()) {
            long userId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            addFriendById(userId);
            cursor.close();
        } else {
            Toast.makeText(this, "未找到用户", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFriendById(long friendId) {
        // 获取当前用户ID（这里需要从登录信息或其他地方获取）
        long currentUserId = getCurrentUserId();

        if (currentUserId == friendId) {
            Toast.makeText(this, "不能添加自己为好友", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.addFriend(currentUserId, friendId);
        if (result != -1) {
            Toast.makeText(this, "添加好友成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "添加好友失败", Toast.LENGTH_SHORT).show();
        }
    }

    private long getCurrentUserId() {
        // 这里需要实现获取当前登录用户ID的逻辑
        // 可以从SharedPreferences或其他地方获取
        return 1; // 临时返回1，需要修改为实际的用户ID
    }
}