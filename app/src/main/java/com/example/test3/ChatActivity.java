package com.example.test3;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.crypto.CryptoUtil;
import com.example.test3.message.Message;
import com.example.test3.network.ApiService;
import com.example.test3.network.RetrofitInstance;
import com.example.test3.adapter.ChatAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private String userA, userB;
    private long userAId, userBId;
    private ChatAdapter chatAdapter;
    private DatabaseHelper dbHelper;
    private Handler pollHandler;
    private Runnable pollRunnable;

    private RecyclerView recyclerView;
    private EditText inputText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userA = getIntent().getStringExtra("userA");
        userB = getIntent().getStringExtra("userB");

        dbHelper = new DatabaseHelper(this);
        userAId = dbHelper.getUserIdByUsername(userA);
        userBId = dbHelper.getUserIdByUsername(userB);

        TextView chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText("与 " + userB + " 的对话");

        recyclerView = findViewById(R.id.messages_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(new ArrayList<>(), userA);
        recyclerView.setAdapter(chatAdapter);

        inputText = findViewById(R.id.message_input);
        Button sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(view -> sendMessage());

        loadChatHistory();
        startPollingMessages();
    }

    private void loadChatHistory() {
        List<Message> encryptedHistory = dbHelper.getChatHistoryBetweenUsers(userA, userB);
        List<Message> decryptedHistory = new ArrayList<>();
        for (Message msg : encryptedHistory) {
            try {
                SecretKeySpec key = CryptoUtil.generateKey(msg.getSender().equals(userA) ? userB : userA);
                String plainText = CryptoUtil.decrypt(msg.getCiphertext(), key);
                decryptedHistory.add(new Message(msg.getSender(), msg.getReceiver(), plainText, msg.getTimestamp()));
            } catch (Exception e) {
                e.printStackTrace();
                decryptedHistory.add(msg); // 解密失败显示密文也行
            }
        }
        chatAdapter.setMessages(decryptedHistory);
        recyclerView.scrollToPosition(decryptedHistory.size() - 1);
    }

    private void sendMessage() {
        String text = inputText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        inputText.setText(""); // 提前清空输入框，防止重复点击

        try {
            long timestamp = System.currentTimeMillis();

            // 使用 userB 的用户名生成密钥
            SecretKeySpec key = CryptoUtil.generateKey(userB);

            // 加密消息文本
            String ciphertext = CryptoUtil.encrypt(text, key);

            // 创建消息对象，传递明文用于显示
            Message message = new Message(userA, userB, text, timestamp);

            // 添加消息到适配器（显示明文）
            chatAdapter.addMessage(message);
            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

            // 保存密文到本地数据库
            dbHelper.saveMessage(userAId, userBId, ciphertext);

            // 构造发送请求（密文）
            ApiService.SendMessageRequest request = new ApiService.SendMessageRequest(userA, userB, ciphertext);

            RetrofitInstance.getApiService().sendMessage(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("ChatActivity", "消息发送成功");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ChatActivity", "消息发送失败", t);
                    // 这里可以考虑通知用户或者重试机制
                }
            });

        } catch (Exception e) {
            Log.e("ChatActivity", "加密或发送失败", e);
            Toast.makeText(ChatActivity.this, "消息发送失败", Toast.LENGTH_SHORT).show();
        }
    }



    private void startPollingMessages() {
        pollHandler = new Handler();
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                pollNewMessages(); // 执行拉取操作
                pollHandler.postDelayed(this, 1000); // 每隔1秒执行一次
            }
        };
        pollHandler.postDelayed(pollRunnable, 1000); // 初始延迟1秒启动
    }

    private void pollNewMessages() {
        ApiService api = RetrofitInstance.getApiService();

        api.pollMessages(userA).enqueue(new Callback<List<ApiService.EncryptedMessage>>() {
            @Override
            public void onResponse(Call<List<ApiService.EncryptedMessage>> call, Response<List<ApiService.EncryptedMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ApiService.EncryptedMessage em : response.body()) {
                        try {
                            // 生成密钥（使用发送者用户名）
                            SecretKeySpec key = CryptoUtil.generateKey(em.receiver);
                            String plainText = CryptoUtil.decrypt(em.ciphertext, key);

                            long timestamp;
                            try {
                                timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                        .parse(em.createdAt).getTime();
                            } catch (Exception e) {
                                timestamp = System.currentTimeMillis();
                            }

                            // 展示消息
                            Message msg = new Message(em.sender, em.receiver, plainText, timestamp);
                            chatAdapter.addMessage(msg);
                            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

                            // 保存密文
                            long senderId = dbHelper.getUserIdByUsername(em.sender);
                            long receiverId = dbHelper.getUserIdByUsername(em.receiver);
                            dbHelper.saveMessage(senderId, receiverId, em.ciphertext);

                            // ACK 回执
                            api.ackMessage(new ApiService.AckMessageRequest(em.id)).enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                                @Override public void onFailure(Call<Void> call, Throwable t) {}
                            });

                        } catch (Exception e) {
                            Log.e("ChatActivity", "解密失败", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.EncryptedMessage>> call, Throwable t) {
                Log.e("ChatActivity", "拉取消息失败", t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }
}
