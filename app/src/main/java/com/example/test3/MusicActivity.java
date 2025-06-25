package com.example.test3;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.test3.provider.MusicProvider;
import com.example.test3.service.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MusicActivity extends AppCompatActivity {
    private MusicService musicService;
    private boolean bound = false;
    private ListView musicList;
    private FloatingActionButton playPauseButton;
    private SimpleCursorAdapter adapter;
    private ContentResolver contentResolver;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
            updatePlayPauseButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        contentResolver = getContentResolver();
        musicList = findViewById(R.id.music_list);
        playPauseButton = findViewById(R.id.play_pause_button);

        // 绑定音乐服务
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // 加载设备上的音乐文件
        loadMusicFiles();

        // 设置列表点击事件
        musicList.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) adapter.getItem(position);
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

            // 播放音乐
            if (bound && musicService != null) {
                musicService.playMusic(path);
                updatePlayPauseButton();

                // 添加到播放历史
                ContentValues values = new ContentValues();
                values.put("music_id", id);
                values.put("play_time", System.currentTimeMillis());
                contentResolver.insert(MusicProvider.HISTORY_URI, values);
            }
        });

        // 设置播放/暂停按钮点击事件
        playPauseButton.setOnClickListener(v -> {
            if (bound && musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                } else {
                    musicService.resumeMusic();
                }
                updatePlayPauseButton();
            }
        });
    }

    private void loadMusicFiles() {
        // 查询设备上的音乐文件
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE + " ASC");

        // 设置适配器
        String[] fromColumns = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };
        int[] toViews = {
                android.R.id.text1,
                android.R.id.text2
        };

        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                fromColumns,
                toViews,
                0);

        musicList.setAdapter(adapter);
    }

    private void updatePlayPauseButton() {
        if (bound && musicService != null) {
            if (musicService.isPlaying()) {
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }
}