package com.example.test3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.test3.broadcast.FriendReceiver;
import com.example.test3.broadcast.SettingReceiver;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextInputEditText editUsername;
    private TextInputEditText editEmail;
    private Button btnChangePassword;
    private Button btnSave;
    private Button btnLogout;
    private String username;
    private DatabaseHelper dbHelper;
    private long userId;
    private String currentAvatarPath;
    private LocalBroadcastManager localBroadcastManager;
    private SettingReceiver settingReceiver;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    // 保存最后请求的操作
    private Runnable lastRequestedOperation = null;
    public static final String ACTION_SETTING = "com.example.test3.ACTION_SETTING";
    public static final String EXTRA_SETTING_TYPE = "setting_type";
    public static final  String UP_PIC_FAILURE="Setting.UP_PIC_FAILURE";
    public static final String CREATE_PIC_FAILURE="Setting.CREATE_PIC_FAILURE";
    public static final String CAMERA_NOT_FOUND="Setting.CAMERA_NOT_FOUND";
    public static final String FULLFILL_PASSWORD="Setting.FULLFILL_PASSWORD";
    public static final String PASSWORD_NOT_SAME="Setting.PASSWORD_NOT_SAME";
    public static final String PASSWORD_IS_SAME="Setting.PASSWORD_IS_SAME";
    public static final String CHANGE_PASSWORD_SUCCESS="Setting.CHANGE_PASSWORD_SUCCESS";
    public static final String WRONG_PASSWORD="Setting.WRONG_PASSWORD";
    public static final String ERROR_TYPE="Setting.ERROR_TYPE";
    public static final String UNFULL_MESSAGE="Setting.UNFULL_MESSAGE";
    public static final String SAVE_SUCCESS="Setting.SAVE_SUCCESS";
    public static final String SAVE_FAILURE="Setting.SAVE_FAILURE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 初始化数据库
        dbHelper = new DatabaseHelper(this);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        settingReceiver = new SettingReceiver();
        // 注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        initReceiver(intentFilter);
        // 初始化视图
        initViews();
        // 设置工具栏
        setupToolbar();
        // 初始化ActivityResultLauncher
        setupActivityResultLaunchers();
        // 加载用户数据
        loadUserData();
        // 设置点击事件
        setupClickListeners();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }

    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // 使用保存的图片路径加载图片
                        if (currentAvatarPath != null) {
                            Glide.with(this)
                                    .load(currentAvatarPath)
                                    .into(profileImage);
                            // 更新数据库中的头像路径
                            dbHelper.updateUserAvatar(userId, currentAvatarPath);
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        // 将图片复制到应用私有目录
                        try {
                            String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                            File destFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                            copyImageToFile(selectedImage, destFile);
                            currentAvatarPath = destFile.getAbsolutePath();

                            Glide.with(this)
                                    .load(currentAvatarPath)
                                    .into(profileImage);
                            // 更新数据库中的头像路径
                            dbHelper.updateUserAvatar(userId, currentAvatarPath);
                        } catch (IOException e) {
                            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });

        // 使用多个权限请求启动器
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限被授予后，重新执行相应的操作
                        if (lastRequestedOperation != null) {
                            lastRequestedOperation.run();
                            lastRequestedOperation = null;
                        }
                    } else {
                        Toast.makeText(this, "需要相应权限才能进行操作", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserData() {
        username = getIntent().getStringExtra("username");
        if (username != null) {
            // 获取用户ID
            userId = dbHelper.getUserIdByUsername(username);
            if (userId != -1) {
                // 获取用户详细信息
                Cursor cursor = dbHelper.getUserProfile(userId);
                if (cursor != null && cursor.moveToFirst()) {
                    // 设置用户名
                    editUsername.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME)));
                    // 设置邮箱
                    editEmail.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL)));
                    // 设置头像
                    String avatarPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AVATAR));
                    if (avatarPath != null && !avatarPath.isEmpty()) {
                        currentAvatarPath = avatarPath;
                        Glide.with(this)
                                .load(Uri.parse(avatarPath))
                                .error(R.drawable.ic_launcher_foreground)
                                .into(profileImage);
                    }
                    cursor.close();
                }
            }
        }
    }

    private void saveImageToInternalStorage(Bitmap bitmap) {
        // 将图片保存到内部存储
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + ".jpg";
        try {
            // 保存图片到内部存储
            java.io.FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // 更新数据库中的头像路径
            currentAvatarPath = getFilesDir() + "/" + fileName;
            dbHelper.updateUserAvatar(userId, currentAvatarPath);
        } catch (Exception e) {
            e.printStackTrace();
            broad(ACTION_SETTING,EXTRA_SETTING_TYPE,UP_PIC_FAILURE);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_change_avatar).setOnClickListener(v -> showImagePickerDialog());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnSave.setOnClickListener(v -> saveUserInfo());

        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void showImagePickerDialog() {
        String[] options = { "拍照", "从相册选择" };
        new AlertDialog.Builder(this)
                .setTitle("选择图片来源")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        lastRequestedOperation = this::openCamera;
                        if (checkCameraPermission()) {
                            openCamera();
                        }
                    } else {
                        lastRequestedOperation = this::openGallery;
                        if (checkStoragePermission()) {
                            openGallery();
                        }
                    }
                })
                .show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上版本使用READ_MEDIA_IMAGES权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                return false;
            }
        } else {
            // Android 13以下版本使用READ_EXTERNAL_STORAGE权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                return false;
            }
        }
        return true;
    }

    private void openCamera() {
        // 创建用于保存照片的文件
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            broad(ACTION_SETTING,EXTRA_SETTING_TYPE,CREATE_PIC_FAILURE);
            return;
        }

        // 如果文件创建成功，继续
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.test3.fileprovider",
                    photoFile);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            try {
                cameraLauncher.launch(takePictureIntent);
            } catch (ActivityNotFoundException e) {
                broad(ACTION_SETTING,EXTRA_SETTING_TYPE,CAMERA_NOT_FOUND);
            }
        }
    }

    private File createImageFile() throws IOException {
        // 创建图片文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // 保存文件路径用于后续使用
        currentAvatarPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhoto);
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText oldPasswordEdit = dialogView.findViewById(R.id.edit_old_password);
        TextInputEditText newPasswordEdit = dialogView.findViewById(R.id.edit_new_password);
        TextInputEditText confirmPasswordEdit = dialogView.findViewById(R.id.edit_confirm_password);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.layout_new_password);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.layout_confirm_password);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改密码")
                .setView(dialogView)
                .setPositiveButton("确认", null)
                .setNegativeButton("取消", null)
                .create();

        // 设置新密码输入监听
        newPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateNewPassword(newPasswordLayout, s.toString());
            }
        });

        // 设置确认密码输入监听
        confirmPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmPassword(confirmPasswordLayout, newPasswordEdit.getText().toString(), s.toString());
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String oldPassword = oldPasswordEdit.getText().toString().trim();
                String newPassword = newPasswordEdit.getText().toString().trim();
                String confirmPassword = confirmPasswordEdit.getText().toString().trim();

                // 验证所有字段是否填写
                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    broad(ACTION_SETTING,EXTRA_SETTING_TYPE,FULLFILL_PASSWORD);
                    Toast.makeText(SettingActivity.this, "请填写所有密码字段", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 验证新密码格式
                if (!isValidPassword(newPassword)) {
                    broad(ACTION_SETTING,EXTRA_SETTING_TYPE,ERROR_TYPE);
                    newPasswordLayout.setError("密码格式不正确");
                    return;
                }
                // 验证两次密码是否匹配
                if (!newPassword.equals(confirmPassword)) {
                    broad(ACTION_SETTING,EXTRA_SETTING_TYPE,PASSWORD_NOT_SAME);
                    confirmPasswordLayout.setError("两次输入的密码不一致");
                    return;
                }

                // 验证新密码不能与旧密码相同
                if (newPassword.equals(oldPassword)) {
                    broad(PASSWORD_IS_SAME);
                    newPasswordLayout.setError("新密码不能与原密码相同");
                    return;
                }

                // 更新密码
                if (dbHelper.updateUserPassword(userId, oldPassword, newPassword)) {
                    broad(ACTION_SETTING,EXTRA_SETTING_TYPE,CHANGE_PASSWORD_SUCCESS);
                    Toast.makeText(SettingActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    broad(ACTION_SETTING,EXTRA_SETTING_TYPE,WRONG_PASSWORD);
                    oldPasswordEdit.setError("原密码错误");
                }
            });
        });

        dialog.show();
    }

    // 验证新密码格式
    private boolean isValidPassword(String password) {
        // 密码至少6位，包含字母和数字
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])$";
        return password.matches(passwordPattern);
    }

    // 验证新密码
    private void validateNewPassword(TextInputLayout layout, String password) {
        if (password.isEmpty()) {
            layout.setError("密码不能为空");
        } else if (!isValidPassword(password)) {
            layout.setError("密码必须至少6位");
        } else {
            layout.setError(null);
        }
    }

    // 验证确认密码
    private void validateConfirmPassword(TextInputLayout layout, String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            layout.setError("请确认密码");
        } else if (!confirmPassword.equals(password)) {
            layout.setError("两次输入的密码不一致");
        } else {
            layout.setError(null);
        }
    }

    private void saveUserInfo() {
        String newUsername = editUsername.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            broad(ACTION_SETTING,EXTRA_SETTING_TYPE,UNFULL_MESSAGE);
            return;
        }

        if (dbHelper.updateUserInfo(userId, newUsername, newEmail)) {
            // 创建返回意图并携带更新后的用户信息
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_username", newUsername);
            resultIntent.putExtra("updated_email", newEmail);
            resultIntent.putExtra("updated_avatar", currentAvatarPath);
            setResult(RESULT_OK, resultIntent);
            broad(SAVE_SUCCESS);
            finish();
        } else {
            broad(SAVE_FAILURE);
        }
    }

    private void copyImageToFile(Uri sourceUri, File destFile) throws IOException {
        InputStream in = getContentResolver().openInputStream(sourceUri);
        OutputStream out = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 清除用户登录状态
                    logout();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void logout() {
        // 清除用户登录状态
        // 这里可以清除SharedPreferences中的登录状态，或者其他需要清除的数据

        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        // 清除任务栈中的其他Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void initReceiver(IntentFilter intentFilter){
        intentFilter.addAction(ACTION_SETTING);
        intentFilter.addAction(SAVE_SUCCESS);
        intentFilter.addAction(SAVE_FAILURE);
        localBroadcastManager.registerReceiver(settingReceiver, intentFilter);
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