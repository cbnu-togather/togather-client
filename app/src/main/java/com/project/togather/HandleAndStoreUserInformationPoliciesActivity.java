package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.project.togather.databinding.ActivityHandleAndStoreUserInformationPoliciesBinding;

public class HandleAndStoreUserInformationPoliciesActivity extends AppCompatActivity {

    private ActivityHandleAndStoreUserInformationPoliciesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHandleAndStoreUserInformationPoliciesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** 뒤로가기 버튼 기능 */
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });
    }
}