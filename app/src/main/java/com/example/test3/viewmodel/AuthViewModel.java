package com.example.test3.viewmodel; // 确认这是你的 ViewModel 包名

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.test3.data.AuthResponse;
import com.example.test3.data.ErrorResponse; // 如果你在 handleAuthResponse 中解析错误
import com.example.test3.data.LoginRequest;
import com.example.test3.data.RegisterRequest;
import com.example.test3.data.UserData;
import com.example.test3.network.ApiService;
import com.example.test3.network.RetrofitInstance;
import com.google.gson.Gson; // 用于解析 ErrorResponse

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response; // 确保是 retrofit2.Response

public class AuthViewModel extends ViewModel {

    private final MutableLiveData<AuthResult> registrationResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> loginResult = new MutableLiveData<>();
    private final ApiService apiService;
    private final Gson gson; // 用于解析JSON错误体

    public AuthViewModel() {
        apiService = RetrofitInstance.getApiService(); // 获取ApiService单例
        gson = new Gson(); // 初始化Gson实例
    }

    public LiveData<AuthResult> getRegistrationResult() {
        return registrationResult;
    }

    public LiveData<AuthResult> getLoginResult() {
        return loginResult;
    }

    public void registerUser(RegisterRequest registerRequest) {
        registrationResult.setValue(AuthResult.loading()); // 设置加载状态

        apiService.registerUser(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                handleAuthResponse(response, registrationResult);
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                registrationResult.setValue(AuthResult.error("Network request failed: " + t.getMessage()));
            }
        });
    }

    public void loginUser(LoginRequest loginRequest) {
        loginResult.setValue(AuthResult.loading()); // 设置加载状态

        apiService.loginUser(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                handleAuthResponse(response, loginResult);
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loginResult.setValue(AuthResult.error("Network request failed: " + t.getMessage()));
            }
        });
    }


    private void handleAuthResponse(Response<AuthResponse> response, MutableLiveData<AuthResult> liveDataToUpdate) {
        if (response.isSuccessful() && response.body() != null) {
            AuthResponse authResponse = response.body();

            // 安全处理 token 可能为 null 的情况（可选）
            //String token = authResponse.getToken(); // 你可以把这行删掉，如果你没有 token
            String msg = authResponse.getMsg() != null ? authResponse.getMsg() : "登录成功";
            UserData userData = authResponse.getUser(); // 获取用户数据
            liveDataToUpdate.setValue(AuthResult.success(msg, null,userData));
            return;
        }

        // 失败处理逻辑
        String errorMessage = "请求失败";
        int responseCode = response.code();

        try {
            if (response.errorBody() != null) {
                String errorBodyString = response.errorBody().string();

                if (!errorBodyString.isEmpty()) {
                    ErrorResponse errorResponse = gson.fromJson(errorBodyString, ErrorResponse.class);

                    if (errorResponse != null) {
                        if (errorResponse.getMsg() != null && !errorResponse.getMsg().isEmpty()) {
                            errorMessage = errorResponse.getMsg();
                        } else if (errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                            errorMessage = String.join(". ", errorResponse.getErrors());
                        } else {
                            errorMessage = errorBodyString; // fallback
                        }
                    } else {
                        errorMessage = errorBodyString;
                    }
                } else {
                    errorMessage = "Error " + responseCode + ": 空的错误响应体";
                }
            } else {
                errorMessage = "Error " + responseCode + ": " + response.message();
            }
        } catch (Exception e) {
            errorMessage = "解析错误响应失败 (" + e.getMessage() + ")";
        }

        liveDataToUpdate.setValue(AuthResult.error(errorMessage));
    }


}