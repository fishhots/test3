package com.example.test3.network;

import com.example.test3.data.AuthResponse;
import com.example.test3.data.LoginRequest;
import com.example.test3.data.RegisterRequest;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== 用户认证 ====================

    @POST("api/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/auth/login")
    Call<AuthResponse> loginUser(@Body LoginRequest loginRequest);


    // ==================== 消息发送 ====================

    /**
     * 发送加密消息请求体
     */
    class SendMessageRequest {
        @SerializedName("sender")
        public String sender;

        @SerializedName("receiver")
        public String receiver;

        @SerializedName("ciphertext")
        public String ciphertext;

        public SendMessageRequest(String sender, String receiver, String ciphertext) {
            this.sender = sender;
            this.receiver = receiver;
            this.ciphertext = ciphertext;
        }
    }

    @POST("message/send")
    Call<Void> sendMessage(@Body SendMessageRequest request);


    // ==================== 消息轮询 ====================

    /**
     * 加密消息结构体（从服务器拉取时使用）
     */
    class EncryptedMessage {
        @SerializedName("id")
        public int id;

        @SerializedName("sender")
        public String sender;

        @SerializedName("receiver")
        public String receiver;

        @SerializedName("ciphertext")
        public String ciphertext;

        @SerializedName("created_at")
        public String createdAt;
    }

    @GET("message/poll")
    Call<List<EncryptedMessage>> pollMessages(@Query("user") String user);


    // ==================== 确认已读 ====================

    class AckMessageRequest {
        @SerializedName("messageId")
        public int messageId;

        public AckMessageRequest(int messageId) {
            this.messageId = messageId;
        }
    }

    @POST("message/ack")
    Call<Void> ackMessage(@Body AckMessageRequest request);
}
