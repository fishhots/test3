package com.example.test3.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MusicReceiver extends BroadcastReceiver {
    public static final String ACTION_MUSIC_CONTROL = "com.example.test3.ACTION_MUSIC_CONTROL";
    public static final String EXTRA_CONTROL_TYPE = "control_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_MUSIC_CONTROL.equals(intent.getAction())) {
            String controlType = intent.getStringExtra(EXTRA_CONTROL_TYPE);
            String message = "";
            switch (controlType) {
                case "play":
                    message = "开始播放音乐";
                    break;
                case "pause":
                    message = "暂停播放音乐";
                    break;
                case "stop":
                    message = "停止播放音乐";
                    break;
                case "next":
                    message = "下一首";
                    break;
                case "previous":
                    message = "上一首";
                    break;
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}