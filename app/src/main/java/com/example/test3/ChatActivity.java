package com.example.test3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test3.adapter.MessageAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_FRIEND_ID = "friend_id";
    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    public static final String ACTION_CHAT_MESSAGE = "com.example.test3.ACTION_CHAT_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_SENDER_ID = "sender_id";
    public static final String EXTRA_RECEIVER_ID = "receiver_id";

    private long friendId;
    private String friendName;
    private long userId;
    private String userName;
    private EditText messageInput;
    private Button sendButton;
    private TextView chatTitle;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<MessageAdapter.ChatMessage> messageList;
    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHAT_MESSAGE.equals(intent.getAction())) {
                long senderId = intent.getLongExtra(EXTRA_SENDER_ID, -1);
                long receiverId = intent.getLongExtra(EXTRA_RECEIVER_ID, -1);
                String message = intent.getStringExtra(EXTRA_MESSAGE);

                // 只处理与当前聊天相关的消息
                if ((senderId == userId && receiverId == friendId) ||
                        (senderId == friendId && receiverId == userId)) {
                    // 添加消息到列表
                    MessageAdapter.ChatMessage chatMessage = new MessageAdapter.ChatMessage(senderId, message);
                    messageAdapter.addMessage(chatMessage);
                    // 滚动到最新消息
                    messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 初始化LocalBroadcastManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // 获取传递的数据
        friendId = getIntent().getLongExtra(EXTRA_FRIEND_ID, -1);
        friendName = getIntent().getStringExtra(EXTRA_FRIEND_NAME);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);

        if (friendId == -1 || userId == -1) {
            Toast.makeText(this, "聊天信息获取失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        chatTitle = findViewById(R.id.chat_title);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);

        chatTitle.setText("与 " + friendName + " 聊天中");

        // 初始化消息列表和适配器
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, userId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            }
        });

        // 注册本地广播接收器
        IntentFilter filter = new IntentFilter(ACTION_CHAT_MESSAGE);
        localBroadcastManager.registerReceiver(messageReceiver, filter);
    }

    private void sendMessage(String message) {
        // 创建并发送广播
        Intent intent = new Intent(ACTION_CHAT_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_SENDER_ID, userId);
        intent.putExtra(EXTRA_RECEIVER_ID, friendId);
        localBroadcastManager.sendBroadcast(intent);

        // 添加消息到本地列表
        MessageAdapter.ChatMessage chatMessage = new MessageAdapter.ChatMessage(userId, message);
        messageAdapter.addMessage(chatMessage);
        // 滚动到最新消息
        messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(messageReceiver);
    }
}