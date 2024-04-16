package com.project.togather.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.project.togather.databinding.ActivityRecruitmentPostDetailBinding;

public class HomePostDetailActivity extends AppCompatActivity {

    private ActivityRecruitmentPostDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecruitmentPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });
    }
}