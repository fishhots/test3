package com.example.test3;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.test3.databinding.ActivityMainBinding;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private String username;
    private DatabaseHelper dbHelper;
    private CircleImageView navHeaderAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // 设置导航头部的用户信息
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        TextView navContent = headerView.findViewById(R.id.nav_header_content);
        navHeaderAvatar = headerView.findViewById(R.id.nav_header_avatar);

        if (navUsername != null && navContent != null) {
            navUsername.setText(username);
            navContent.setText("hello");
        }

        // 加载用户头像
        loadUserAvatar();

        // 配置导航
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // 设置自定义的导航监听器
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_slideshow) {
                // 启动MusicActivity
                Intent intent = new Intent(MainActivity.this, MusicActivity.class);
                startActivity(intent);
                drawer.closeDrawers();
                return true;
            }
            // 其他菜单项保持默认导航行为
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    private void loadUserAvatar() {
        if (username != null && navHeaderAvatar != null) {
            long userId = dbHelper.getUserIdByUsername(username);
            if (userId != -1) {
                Cursor cursor = dbHelper.getUserProfile(userId);
                if (cursor != null && cursor.moveToFirst()) {
                    String avatarPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AVATAR));
                    if (avatarPath != null && !avatarPath.isEmpty()) {
                        Glide.with(this)
                                .load(avatarPath)
                                .error(R.mipmap.ic_launcher_round)
                                .into(navHeaderAvatar);
                    }
                    cursor.close();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // 启动SettingActivity并传递用户名
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            intent.putExtra("username", username);
            startActivityForResult(intent, 1001); // 使用requestCode 1001
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // 更新用户信息
            username = data.getStringExtra("updated_username");
            String updatedEmail = data.getStringExtra("updated_email");
            String updatedAvatar = data.getStringExtra("updated_avatar");

            // 更新导航头部的用户名
            View headerView = binding.navView.getHeaderView(0);
            TextView navUsername = headerView.findViewById(R.id.nav_header_username);
            if (navUsername != null) {
                navUsername.setText(username);
            }

            // 更新头像
            if (updatedAvatar != null && !updatedAvatar.isEmpty() && navHeaderAvatar != null) {
                Glide.with(this)
                        .load(updatedAvatar)
                        .error(R.mipmap.ic_launcher_round)
                        .into(navHeaderAvatar);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}