package com.project.togather.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.project.togather.CreatePostActivity;
import com.project.togather.R;
import com.project.togather.chat.ChatActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.databinding.ActivityChatBinding;
import com.project.togather.databinding.ActivityProfileBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.user.HandleAndStoreUserInformationPoliciesActivity;
import com.project.togather.user.LoginActivity;
import com.project.togather.user.SignUpActivity;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private Dialog askLogout_dialog,
            askUnsubscribe_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (로그아웃 확인) 다이얼로그 변수 초기화 및 설정 */
        askLogout_dialog = new Dialog(ProfileActivity.this);  // Dialog 초기화
        askLogout_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askLogout_dialog.setContentView(R.layout.dialog_ask_logout); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askLogout_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /** (회원탈퇴 확인) 다이얼로그 변수 초기화 및 설정 */
        askUnsubscribe_dialog = new Dialog(ProfileActivity.this);  // Dialog 초기화
        askUnsubscribe_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askUnsubscribe_dialog.setContentView(R.layout.dialog_ask_unsubscribe); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askUnsubscribe_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /** (프로필 수정) 레이아웃 클릭 시 */
        binding.editProfileButton.setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, EditMyProfile.class)));

        /** (이용약관) 레이아웃 클릭 시 */
        binding.agreeOurPoliciesTextView.setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, HandleAndStoreUserInformationPoliciesActivity.class)));

        /** (로그아웃) 텍스트뷰 클릭 시 */
        binding.logoutTextView.setOnClickListener(view -> showDialog_askLogout_dialog());

        /** (회원탈퇴) 텍스트뷰 클릭 시 */
        binding.unsubscribeTextView.setOnClickListener(view -> showDialog_askUnsubscribe_dialog());

        /** (홈) 레이아웃 클릭 시 */
        binding.homeActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        /** (동네생활) 레이아웃 클릭 시 */
        binding.communityActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, CommunityActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        /** (글 쓰기) 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        /** (채팅) 레이아웃 클릭 시 */
        binding.chatActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, ChatActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    /**
     * (askLogout_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askLogout_dialog() {
        askLogout_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askLogout_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askLogout_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askLogout_dialog.dismiss());

        // (확인) 버튼
        askLogout_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askLogout_dialog.dismiss(); // 다이얼로그 닫기
            new ToastSuccess("로그아웃 되었어요", ProfileActivity.this);
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        });
    }

    /**
     * (askUnsubscribe_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askUnsubscribe_dialog() {
        askUnsubscribe_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askUnsubscribe_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askUnsubscribe_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askUnsubscribe_dialog.dismiss());

        // (확인) 버튼
        askUnsubscribe_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askUnsubscribe_dialog.dismiss(); // 다이얼로그 닫기
            new ToastSuccess("회원탈퇴를 완료했어요", ProfileActivity.this);
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        });
    }
}