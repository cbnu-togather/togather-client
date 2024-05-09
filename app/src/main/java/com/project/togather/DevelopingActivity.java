package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.project.togather.databinding.ActivityDevelopingBinding;

public class DevelopingActivity extends AppCompatActivity {

    private ActivityDevelopingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDevelopingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(view -> finish());
    }
}