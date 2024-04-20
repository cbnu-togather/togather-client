package com.project.togather.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.project.togather.CreatePostActivity;
import com.project.togather.chat.ChatActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.databinding.ActivityChatBinding;
import com.project.togather.databinding.ActivityProfileBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.user.HandleAndStoreUserInformationPoliciesActivity;
import com.project.togather.user.SignUpActivity;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** "홈" 레이아웃 클릭 시 */
        binding.homeActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        /** "동네생활" 레이아웃 클릭 시 */
        binding.communityActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, CommunityActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        /** "글 쓰기" 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });

        /** "채팅" 레이아웃 클릭 시 */
        binding.chatActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        /** "이용약관" 레이아웃 클릭 시 */
        binding.agreeOurPoliciesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, HandleAndStoreUserInformationPoliciesActivity.class);
                startActivity(intent);
            }
        });
    }
}