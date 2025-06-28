package com.example.test3.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MusicProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.test3.provider.music";
    private static final String PLAYLIST_TABLE = "playlist";
    private static final String HISTORY_TABLE = "history";

    private static final int PLAYLIST = 1;
    private static final int PLAYLIST_ID = 2;
    private static final int HISTORY = 3;
    private static final int HISTORY_ID = 4;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "playlist", PLAYLIST);
        uriMatcher.addURI(AUTHORITY, "playlist/#", PLAYLIST_ID);
        uriMatcher.addURI(AUTHORITY, "history", HISTORY);
        uriMatcher.addURI(AUTHORITY, "history/#", HISTORY_ID);
    }

    public static final Uri PLAYLIST_URI = Uri.parse("content://" + AUTHORITY + "/playlist");
    public static final Uri HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/history");

    private DatabaseHelper dbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "music.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建播放列表表
            db.execSQL("CREATE TABLE " + PLAYLIST_TABLE + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "artist TEXT," +
                    "path TEXT," +
                    "duration INTEGER)");

            // 创建播放历史表
            db.execSQL("CREATE TABLE " + HISTORY_TABLE + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "music_id INTEGER," +
                    "play_time INTEGER)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case PLAYLIST:
                cursor = db.query(PLAYLIST_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PLAYLIST_ID:
                selection = "_id = ?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                cursor = db.query(PLAYLIST_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case HISTORY:
                cursor = db.query(HISTORY_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case HISTORY_ID:
                selection = "_id = ?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                cursor = db.query(HISTORY_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
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
            case PLAYLIST:
                return "vnd.android.cursor.dir/vnd.example.playlist";
            case PLAYLIST_ID:
                return "vnd.android.cursor.item/vnd.example.playlist";
            case HISTORY:
                return "vnd.android.cursor.dir/vnd.example.history";
            case HISTORY_ID:
                return "vnd.android.cursor.item/vnd.example.history";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        Uri resultUri;

        switch (uriMatcher.match(uri)) {
            case PLAYLIST:
                id = db.insert(PLAYLIST_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(PLAYLIST_URI, id);
                break;
            case HISTORY:
                id = db.insert(HISTORY_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(HISTORY_URI, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case PLAYLIST:
                count = db.delete(PLAYLIST_TABLE, selection, selectionArgs);
                break;
            case PLAYLIST_ID:
                selection = "_id = ?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                count = db.delete(PLAYLIST_TABLE, selection, selectionArgs);
                break;
            case HISTORY:
                count = db.delete(HISTORY_TABLE, selection, selectionArgs);
                break;
            case HISTORY_ID:
                selection = "_id = ?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                count = db.delete(HISTORY_TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case PLAYLIST:
                count = db.update(PLAYLIST_TABLE, values, selection, selectionArgs);
                break;
            case PLAYLIST_ID:
                selection = "_id = ?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                count = db.update(PLAYLIST_TABLE, values, selection, selectionArgs);
                break;
            case HISTORY:
                count = db.update(HISTORY_TABLE, values, selection, selectionArgs);
                break;
            case HISTORY_ID:
                selection = "_id = ?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                count = db.update(HISTORY_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}