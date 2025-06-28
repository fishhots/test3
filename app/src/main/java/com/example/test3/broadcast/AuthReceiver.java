package com.example.test3.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.test3.RegisterActivity;
import com.example.test3.LoginActivity;
public class AuthReceiver extends BroadcastReceiver {
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ERROR_TYPE="ERROR_TYPE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = "";
        String action=intent.getAction();
        if (action==ACTION_LOGIN) {
            String error = intent.getStringExtra(ERROR_TYPE);
            switch (error){
                case LoginActivity.WRONG:
                    message="登录失败，用户名或密码错误";
                    break;
                case LoginActivity.UNFULL_MESSAGE:
                    message="请补充所有信息";
                    break;
            }
        }
        else if(action==ACTION_REGISTER){
            String error = intent.getStringExtra(ERROR_TYPE);
            switch (error){
                case RegisterActivity.UNFULL_MESSAGE:
                    message="请补充所有信息";
                    break;
                case RegisterActivity.SIGN_FAILURE:
                    message="注册失败";
                    break;
            }
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

    }
}