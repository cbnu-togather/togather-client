package com.project.togather.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;

import com.project.togather.databinding.ActivityGroupChatBinding;

import androidx.activity.OnBackPressedCallback;

public class GroupChatActivity extends AppCompatActivity {

    private ActivityGroupChatBinding binding;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(view ->
                startActivity(new Intent(GroupChatActivity.this, ChatActivity.class)));

        // Add callback listener
        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(GroupChatActivity.this, ChatActivity.class));
            }
        });
    }
}