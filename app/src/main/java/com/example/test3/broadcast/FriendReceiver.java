package com.example.test3.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.test3.FriendActivity;

public class FriendReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        switch (action) {
            case FriendActivity.ACTION_FRIEND_SEARCH:
                String query = intent.getStringExtra(FriendActivity.EXTRA_SEARCH_QUERY);
                Toast.makeText(context, "正在搜索用户: " + query, Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.SEARCH_FIRST:
                Toast.makeText(context, "请先进行好友搜索", Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.SEARCH_SUCCESS:
                String name = intent.getStringExtra(FriendActivity.EXTRA_FRIEND_NAME);
                Toast.makeText(context, "搜索到用户: " + name, Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.ACTION_FRIEND_ADD:
                String friendName = intent.getStringExtra(FriendActivity.EXTRA_FRIEND_NAME);
                Toast.makeText(context, "已成功添加好友: " + friendName, Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.NONE_INPUT:
                Toast.makeText(context, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.ACTION_FRIEND_NOT_FOUND:
                Toast.makeText(context, "未找到该用户", Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.ADD_FAILURE:
                Toast.makeText(context, "添加好友失败", Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.ALREADY_FRIEND:
                Toast.makeText(context, "该用户已经是你的好友", Toast.LENGTH_SHORT).show();
                break;

            case FriendActivity.ADD_SELF:
                Toast.makeText(context, "不能添加自己为好友", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}