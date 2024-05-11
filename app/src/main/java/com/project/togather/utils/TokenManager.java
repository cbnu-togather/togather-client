package com.project.togather.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

public class TokenManager {
    private final SharedPreferences prefs;
    private static TokenManager INSTANCE = null;

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences("JWT_TOKEN_PREFS", Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TokenManager(context);
        }
        return INSTANCE;
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("JWT_TOKEN", token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString("JWT_TOKEN", null);
    }

    public void saveUserInfo(JSONObject jsonObject) {
        String userId = jsonObject.optString("id");
        String phone = jsonObject.optString("phone");
        String username = jsonObject.optString("name");
        String photo = jsonObject.optString("photo");

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id", userId);
        editor.putString("phone", phone);
        editor.putString("name", username);
        editor.putString("photo", photo);
        editor.apply();
    }

    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        // 토큰 제거
        editor.remove("JWT_TOKEN");
        // 사용자 정보 제거
        editor.remove("id");
        editor.remove("phone");
        editor.remove("name");
        editor.remove("photo");
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString("id", null);
    }

    public String getPhone() {
        return prefs.getString("phone", null);
    }

    public String getUsername() {
        return prefs.getString("name", null);
    }

    public String getPhoto() {
        return prefs.getString("photo", null);
    }

}
