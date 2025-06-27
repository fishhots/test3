package com.example.test3;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.test3.broadcast.AuthReceiver;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private DatabaseHelper databaseHelper;
    public static final String PREF_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    private LocalBroadcastManager localBroadcastManager;
    private AuthReceiver authReceiver;

    public static final String LOGIN="LOGIN";
    public static final String ERROR_TYPE="ERROR_TYPE";
    public static final String UNFULL_MESSAGE="UNFULL_MESSAGE";
    public static final String WRONG="WRONG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        databaseHelper = new DatabaseHelper(this);


        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        authReceiver = new AuthReceiver();
        IntentFilter intentFilter = new IntentFilter();
        initReceiver(intentFilter);
    }
    public void initView(){
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                broad(LOGIN,ERROR_TYPE,UNFULL_MESSAGE);
                return;
            }
            if (databaseHelper.checkLogin(username, password)) {
                // 获取用户ID
                long userId = databaseHelper.getUserIdByUsername(username);

                // 保存用户信息到SharedPreferences
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(KEY_USER_ID, userId);
                editor.putString(KEY_USERNAME, username);
                editor.apply();

                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e("LoginActivity", "启动MainActivity失败", e);
                }
            } else {
                broad(LOGIN,ERROR_TYPE,WRONG);

            }
        }
        if (v.getId() == R.id.register_button) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }
    public void initReceiver(IntentFilter intentFilter){
        intentFilter.addAction(LOGIN);
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