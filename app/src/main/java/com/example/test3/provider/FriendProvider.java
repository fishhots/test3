package com.example.test3.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.test3.DatabaseHelper;

public class FriendProvider extends ContentProvider {
    private static final String TAG = "FriendProvider";
    private DatabaseHelper dbHelper;

    // Authority for this provider
    public static final String AUTHORITY = "com.example.test3.provider.friend";

    // Base path for all friend data
    private static final String PATH_FRIENDS = "friends";
    private static final String PATH_SEARCH = "search";

    // Content URI for accessing friend data
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_FRIENDS);
    public static final Uri SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_SEARCH);

    // URI matcher codes
    private static final int FRIENDS = 1;
    private static final int FRIEND_ID = 2;
    private static final int SEARCH_FRIENDS = 3;

    // URI matcher
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, PATH_FRIENDS, FRIENDS);
        uriMatcher.addURI(AUTHORITY, PATH_FRIENDS + "/#", FRIEND_ID);
        uriMatcher.addURI(AUTHORITY, PATH_SEARCH + "/*", SEARCH_FRIENDS);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                // Get all friends for a user
                // selection should contain user_id condition
                cursor = dbHelper.getFriends(Long.parseLong(selectionArgs[0]));
                break;

            case FRIEND_ID:
                // Get specific friend details
                long friendId = ContentUris.parseId(uri);
                cursor = db.query(DatabaseHelper.TABLE_USERS,
                        projection,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[] { String.valueOf(friendId) },
                        null, null, sortOrder);
                break;

            case SEARCH_FRIENDS:
                // Search users by username
                String searchQuery = uri.getLastPathSegment();
                cursor = dbHelper.searchUsers(searchQuery);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_FRIENDS;
            case FRIEND_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_FRIENDS;
            case SEARCH_FRIENDS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_SEARCH;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                // Add a new friend
                id = dbHelper.addFriend(
                        values.getAsLong(DatabaseHelper.COLUMN_USER_ID),
                        values.getAsLong(DatabaseHelper.COLUMN_FRIEND_ID));
                if (id > 0) {
                    Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;
        }

        throw new IllegalArgumentException("Invalid URI for insert: " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case FRIEND_ID:
                // Delete a specific friend relationship
                long userId = Long.parseLong(selectionArgs[0]);
                long friendId = ContentUris.parseId(uri);
                if (dbHelper.deleteFriend(userId, friendId)) {
                    count = 1;
                }
                break;
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        // Currently, we don't support updating friend relationships
        throw new UnsupportedOperationException("Update operation is not supported for friends");
    }
}