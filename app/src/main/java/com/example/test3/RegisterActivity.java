package com.example.test3;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.test3.broadcast.AuthReceiver;
import com.example.test3.service.RegisterService;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText emailInput;
    private Button registerButton;
    private DatabaseHelper databaseHelper;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LocalBroadcastManager localBroadcastManager;
    private AuthReceiver authReceiver;
    public static final String REGISTER="REGISTER";
    public static final String ERROR_TYPE="ERROR_TYPE";
    public static final String SIGN_FAILURE="SIGN_FAILURE";
    public static final String UNFULL_MESSAGE="UNFURL_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        authReceiver = new AuthReceiver();
        IntentFilter intentFilter = new IntentFilter();
        initReceiver(intentFilter);
        // 初始化权限请求启动器
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限被授予，启动服务
                        startRegisterService();
                    } else {
                        // 权限被拒绝
                        Toast.makeText(this, "需要通知权限才能显示服务状态", Toast.LENGTH_LONG).show();
                    }
                });

        // 检查并请求通知权限
        checkAndRequestNotificationPermission();

        databaseHelper = new DatabaseHelper(this);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        emailInput = findViewById(R.id.email);
        registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // 已经有权限，直接启动服务
                startRegisterService();
            } else {
                // 请求权限
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Android 13以下版本，直接启动服务
            startRegisterService();
        }
    }

    private void startRegisterService() {
        Log.i("RegisterActivity", "Starting RegisterService");
        Intent serviceIntent = new Intent(this, RegisterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Log.i("RegisterActivity", "RegisterService started");
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            broad(REGISTER,ERROR_TYPE,UNFULL_MESSAGE);
            return;
        }

        long result = databaseHelper.registerUser(username, password, email);
        if (result != -1) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            broad(REGISTER,ERROR_TYPE,SIGN_FAILURE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("RegisterActivity", "Stopping RegisterService");
        stopService(new Intent(this, RegisterService.class));
    }
    public void initReceiver(IntentFilter intentFilter){
        intentFilter.addAction(REGISTER);
        localBroadcastManager.registerReceiver(authReceiver, intentFilter);
    }
    public void broad(String flag,String tag,String value){
        Intent intent = new Intent(flag);
        intent.putExtra(tag, value);
        localBroadcastManager.sendBroadcast(intent);
    }
    public void broad(String flag){
        Intent intent = new Intent(flag);
        localBroadcastManager.sendBroadcast(intent);
    }

}