package com.example.test3.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.test3.RegisterActivity;
import com.example.test3.R;

public class RegisterService extends Service {
    private static final String CHANNEL_ID = "RegisterServiceChannel";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("RegisterService", "Service onCreate");
        createNotificationChannel();
        startForegroundWithNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("RegisterService", "Service onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("RegisterService", "Service onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "注册服务通道",
                    NotificationManager.IMPORTANCE_HIGH);
            serviceChannel.setDescription("用于显示注册服务的通知");
            serviceChannel.enableLights(true);
            serviceChannel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.i("RegisterService", "Notification channel created");
            }
        }
    }

    private void startForegroundWithNotification() {
        Log.i("RegisterService", "Creating notification");

        // 创建通知的点击意图
        Intent notificationIntent = new Intent(this, RegisterActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // 创建通知
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("注册服务")
                .setContentText("注册服务正在运行中...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();

        Log.i("RegisterService", "Starting foreground service");
        startForeground(NOTIFICATION_ID, notification);
    }
}