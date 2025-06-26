package com.example.test3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SocialApp.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    public static final String TABLE_USERS = "users";
    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_MESSAGES = "messages";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";

    // User table columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_AVATAR = "avatar";

    // Friends table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FRIEND_ID = "friend_id";
    private static final String COLUMN_STATUS = "status";

    // Messages table columns
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_RECEIVER_ID = "receiver_id";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_READ = "read";

    // Create table statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_AVATAR + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_FRIENDS = "CREATE TABLE " + TABLE_FRIENDS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID + " INTEGER NOT NULL,"
            + COLUMN_FRIEND_ID + " INTEGER NOT NULL,"
            + COLUMN_STATUS + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_FRIEND_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_SENDER_ID + " INTEGER NOT NULL,"
            + COLUMN_RECEIVER_ID + " INTEGER NOT NULL,"
            + COLUMN_MESSAGE + " TEXT NOT NULL,"
            + COLUMN_READ + " INTEGER DEFAULT 0,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_FRIENDS);
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User operations
    public long registerUser(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_EMAIL, email);
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_ID };
        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public void updateUserAvatar(long userId, String avatarPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AVATAR, avatarPath);
        db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[] { String.valueOf(userId) });
    }

    // Friend operations
    public long addFriend(long userId, long friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_FRIEND_ID, friendId);
        values.put(COLUMN_STATUS, "pending");
        return db.insert(TABLE_FRIENDS, null, values);
    }

    public Cursor searchUsers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[] { COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_AVATAR },
                COLUMN_USERNAME + " = ?",
                new String[] { query },
                null, null, null);
    }

    public Cursor getFriends(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.* FROM " + TABLE_USERS + " u " +
                "INNER JOIN " + TABLE_FRIENDS + " f ON u." + COLUMN_ID + " = f." + COLUMN_FRIEND_ID +
                " WHERE f." + COLUMN_USER_ID + " = ?";
        return db.rawQuery(query, new String[] { String.valueOf(userId) });
    }

    public boolean deleteFriend(long userId, long friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_FRIENDS,
                COLUMN_USER_ID + "=? AND " + COLUMN_FRIEND_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(friendId) });
        return result > 0;
    }

    // Message operations
    public long saveMessage(long senderId, long receiverId, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER_ID, senderId);
        values.put(COLUMN_RECEIVER_ID, receiverId);
        values.put(COLUMN_MESSAGE, message);
        return db.insert(TABLE_MESSAGES, null, values);
    }

    public Cursor getUnreadMessages(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MESSAGES,
                null,
                COLUMN_RECEIVER_ID + "=? AND " + COLUMN_READ + "=0",
                new String[] { String.valueOf(userId) },
                null, null, COLUMN_CREATED_AT + " DESC");
    }

    public void markMessageAsRead(long messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_READ, 1);
        db.update(TABLE_MESSAGES, values, COLUMN_ID + "=?", new String[] { String.valueOf(messageId) });
    }

    // User profile operations
    public Cursor getUserProfile(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[] { COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_AVATAR },
                COLUMN_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null, null);
    }

    // 更新用户信息
    public boolean updateUserInfo(long userId, String username, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        int result = db.update(TABLE_USERS, values, COLUMN_ID + "=?",
                new String[] { String.valueOf(userId) });
        return result > 0;
    }

    // 更新用户密码
    public boolean updateUserPassword(long userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 首先验证旧密码
        String[] columns = { COLUMN_ID };
        String selection = COLUMN_ID + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = { String.valueOf(userId), oldPassword };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // 旧密码正确，更新新密码
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, newPassword);
            int result = db.update(TABLE_USERS, values, COLUMN_ID + "=?",
                    new String[] { String.valueOf(userId) });
            cursor.close();
            return result > 0;
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    public void clearUserData(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete user's messages
        db.delete(TABLE_MESSAGES,
                COLUMN_SENDER_ID + "=? OR " + COLUMN_RECEIVER_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(userId) });

        // Delete user's friend relationships
        db.delete(TABLE_FRIENDS,
                COLUMN_USER_ID + "=? OR " + COLUMN_FRIEND_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(userId) });

        // Clear user's avatar
        ContentValues values = new ContentValues();
        values.putNull(COLUMN_AVATAR);
        db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[] { String.valueOf(userId) });
    }

    public long getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        String[] columns = { COLUMN_ID };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
        }

        return userId;
    }

    public boolean isFriend(long userId, long friendId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRIENDS,
                new String[] { COLUMN_ID },
                COLUMN_USER_ID + "=? AND " + COLUMN_FRIEND_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(friendId) },
                null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public String getUsernameById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = null;
        Cursor cursor = db.query(TABLE_USERS,
                new String[] { COLUMN_USERNAME },
                COLUMN_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            cursor.close();
        }
        return username;
    }
}