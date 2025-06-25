package com.example.test3;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.test3.provider.MusicProvider;
import com.example.test3.service.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MusicActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO
    };

    private MusicService musicService;
    private boolean bound = false;
    private ListView musicList;
    private TextView emptyView;
    private FloatingActionButton playPauseButton;
    private SimpleCursorAdapter adapter;
    private ContentResolver contentResolver;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // 检查权限是否已授予
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        loadMusicFiles();
                        return;
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        loadMusicFiles();
                        return;
                    }
                }

                // 如果权限未授予，显示提示并再次请求
                Toast.makeText(this, "需要存储权限来访问音乐文件，请点击允许", Toast.LENGTH_LONG).show();
                showEmptyView("需要存储权限来访问音乐文件\n\n请点击允许按钮授予权限");

                // 延迟1秒后再次请求权限
                musicList.postDelayed(this::checkPermissionAndLoadMusic, 1000);
            });

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
        emptyView = findViewById(R.id.empty_view);
        playPauseButton = findViewById(R.id.play_pause_button);

        // 绑定音乐服务
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // 显示初始提示
        showEmptyView("正在请求访问音乐的权限...");

        // 延迟一下再请求权限，确保UI已经准备好
        musicList.post(() -> {
            // 检查权限并加载音乐
            checkPermissionAndLoadMusic();
        });

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

    private void checkPermissionAndLoadMusic() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // 无论是否已有权限，都主动请求，这样可以确保权限对话框显示
        requestPermissionLauncher.launch(new String[] { permission });
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

        if (cursor == null || cursor.getCount() == 0) {
            showEmptyView("暂无音乐");
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

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
        hideEmptyView();
    }

    private void showEmptyView(String message) {
        if (emptyView != null) {
            emptyView.setText(message);
            emptyView.setVisibility(View.VISIBLE);
            musicList.setVisibility(View.GONE);
            playPauseButton.setVisibility(View.GONE);
        }
    }

    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
            musicList.setVisibility(View.VISIBLE);
            playPauseButton.setVisibility(View.VISIBLE);
        }
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
        if (adapter != null && adapter.getCursor() != null) {
            adapter.getCursor().close();
        }
    }
}