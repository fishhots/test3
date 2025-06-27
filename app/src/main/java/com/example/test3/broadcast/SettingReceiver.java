package com.example.test3.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.test3.SettingActivity;

public class SettingReceiver extends BroadcastReceiver {
    public static final String ACTION_SETTING = "com.example.test3.ACTION_SETTING";
    public static final String EXTRA_SETTING_TYPE = "setting_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = "";
        String action=intent.getAction();
        if (action==ACTION_SETTING) {
            String settingType = intent.getStringExtra(EXTRA_SETTING_TYPE);


            switch (settingType) {
                case SettingActivity.UNFULL_MESSAGE:
                    message = "请完善所有信息";
                    break;
                case SettingActivity.CAMERA_NOT_FOUND:
                    message = "未找到相机";
                    break;
                case SettingActivity.CHANGE_PASSWORD_SUCCESS:
                    message = "密码修改成功";
                    break;
                case SettingActivity.CREATE_PIC_FAILURE:
                    message = "创建图像失败";
                    break;
                case SettingActivity.ERROR_TYPE:
                    message = "密码格式不正确";
                    break;
                case SettingActivity.FULLFILL_PASSWORD:
                    message = "请输入完整密码";
                    break;
                case SettingActivity.PASSWORD_IS_SAME:
                    message = "新密码和旧密码重复";
                    break;
                case SettingActivity.PASSWORD_NOT_SAME:
                    message = "两次新密码不一样";
                    break;
                case SettingActivity.WRONG_PASSWORD:
                    message = "密码不正确";
                    break;
            }

        }
        if(action==SettingActivity.SAVE_FAILURE){
            message="保存失败";
        }
        if(action==SettingActivity.SAVE_SUCCESS){
            message="保存成功";
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}