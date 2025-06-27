package com.example.test3.message;

public class Message {
    private int id;
    private String sender;
    private String receiver;
    private String ciphertext;
    private long timestamp;

    // 空构造方法（必须存在，用于 Retrofit 和 Room）
    public Message() {
    }

    public Message(int id, String sender, String receiver, String ciphertext, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.ciphertext = ciphertext;
        this.timestamp = timestamp;
    }

    // 不含 ID 的构造方法（用于新建消息时）
    public Message(String sender, String receiver, String ciphertext, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.ciphertext = ciphertext;
        this.timestamp = timestamp;
    }

    // Getter 和 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
