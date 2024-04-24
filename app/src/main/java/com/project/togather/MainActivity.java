package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.project.togather.databinding.ActivityMainBinding;
import com.project.togather.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (시작하기) 버튼 클릭 시 */
        binding.startButton.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, HomeActivity.class)));
    }
}