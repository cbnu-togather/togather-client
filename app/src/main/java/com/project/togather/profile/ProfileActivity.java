package com.project.togather.profile;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.MainActivity;
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.R;
import com.project.togather.chat.ChatActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.databinding.ActivityProfileBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.user.HandleAndStoreUserInformationPoliciesActivity;
import com.project.togather.user.LoginActivity;
import com.project.togather.profile.likedPost.LikedPostListActivity;
import com.project.togather.profile.myCommunityPost.MyCommunityPostListActivity;
import com.project.togather.profile.myRecruitmentPartyPost.MyRecruitmentPartyPostListActivity;
import com.project.togather.user.SignUpActivity;
import com.project.togather.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private Dialog askLogout_dialog,
            askUnsubscribe_dialog;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private BottomSheetBehavior selectCreatePostTypeBottomSheetBehavior;
    private UserAPI userAPI;
    private TokenManager tokenManager;
    private RetrofitService retrofitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        getUserInfo();

        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();  // 현재 액티비티와 같은 작업에 있는 모든 액티비티를 종료
            }
        });

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

        /** (파티원 모집글) 레이아웃 클릭 시 */
        binding.myRecruitmentPartyPostRelativeLayout.setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, MyRecruitmentPartyPostListActivity.class)));

        /** (커뮤니티 작성글) 레이아웃 클릭 시 */
        binding.myCommunityPostRelativeLayout.setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, MyCommunityPostListActivity.class)));

        /** (관심목록) 레이아웃 클릭 시 */
        binding.likedPostRelativeLayout.setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, LikedPostListActivity.class)));

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

        // 어두운 배경 클릭 이벤트 설정
        binding.backgroundDimmer.setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        });

        selectCreatePostTypeBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.selectCreatePostTypeBottomSheet_layout));

        selectCreatePostTypeBottomSheetBehavior.setDraggable(false);

        selectCreatePostTypeBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        binding.backgroundDimmer.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                        binding.backgroundDimmer.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                binding.backgroundDimmer.setAlpha(slideOffset);
                binding.backgroundDimmer.setVisibility(View.VISIBLE);
            }
        });


        /** "글 쓰기" 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(view -> selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        // 작성할 게시글 유형 선택
        findViewById(R.id.createRecruitmentPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(ProfileActivity.this, CreateRecruitmentPostActivity.class));
            }
        });

        findViewById(R.id.createCommunityPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(ProfileActivity.this, CreateCommunityPostActivity.class));
            }
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
            tokenManager.logout();
            new ToastSuccess("로그아웃 되었어요", ProfileActivity.this);
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
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
            performUnsubscribe();
        });
    }

    // 회원 탈퇴 메서드
    public void performUnsubscribe() {
        Call<Void> call = userAPI.deleteUser();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    new ToastSuccess("회원탈퇴를 완료했어요", ProfileActivity.this);
                    tokenManager.logout();
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ProfileActivity.this);
            }
        });
    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBodyString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBodyString);

                        // JSON 필드 존재 여부 확인
                        if (jsonObject.has("name") && jsonObject.has("photo")) {
                            String userName = jsonObject.getString("name");
                            String photo = jsonObject.getString("photo");

                            // 내 유저 이름을 표시
                            binding.userNameTextView.setText(userName);
                            if (!isDestroyed() && !isFinishing() && photo != null) {
                                // 내 프로필 사진을 표시
                                Glide.with(ProfileActivity.this)
                                        .load(photo)
                                        .placeholder(R.drawable.one_person_logo)
                                        .error(R.drawable.one_person_logo)
                                        .into(binding.userProfileImageRoundedImageView);
                            }

                        } else {
                            // 필요한 필드가 없을 경우 로그 출력
                            Log.e("getUserInfo", "Required fields are missing in the JSON response.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 403) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ProfileActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserInfo();

    }
}