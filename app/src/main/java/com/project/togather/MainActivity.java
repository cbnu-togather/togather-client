package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.project.togather.databinding.ActivityMainBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.profile.EditMyProfile;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.user.LoginActivity;
import com.project.togather.user.SignUpActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (시작하기) 버튼 클릭 시 */
        binding.startButton.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }
}