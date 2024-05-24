package com.project.togather.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.GetMyLocation;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.databinding.ActivityRecruitmentPostDetailBinding;
import com.project.togather.editPost.recruitment.EditRecruitmentPostActivity;
import com.project.togather.editPost.recruitment.EditRecruitmentPostSelectMeetingSpotActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.user.LoginActivity;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecruitmentPostDetailActivity extends AppCompatActivity {

    private ActivityRecruitmentPostDetailBinding binding;

    private BottomSheetBehavior selectPostManagementBottomSheetBehavior;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RetrofitService retrofitService;
    private Dialog askDeletePost_dialog, askJoinParty_dialog, askStopRecruitment_dialog;

    private boolean isWriter, isLiked, isRecruitmentComplete;

    private static MapView mapView;
    private static ViewGroup mapViewContainer;
    private MapPoint selectedPoint;
    private MapPOIItem marker;

    /**
     * 위치 설정에 대한 객체 변수
     */
    private LocationManager locationManager;
    private static double selectedLatitude, selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecruitmentPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        binding.activityHeaderRelativeLayout.bringToFront();

        /** (게시글 삭제 확인) 다이얼로그 변수 초기화 및 설정 */
        askDeletePost_dialog = new Dialog(RecruitmentPostDetailActivity.this);  // Dialog 초기화
        askDeletePost_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askDeletePost_dialog.setContentView(R.layout.dialog_ask_delete_post); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askDeletePost_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /** (손 들기 확인) 다이얼로그 변수 초기화 및 설정 */
        askJoinParty_dialog = new Dialog(RecruitmentPostDetailActivity.this);  // Dialog 초기화
        askJoinParty_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askJoinParty_dialog.setContentView(R.layout.dialog_ask_join_party); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askJoinParty_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /** (모집 마감 확인) 다이얼로그 변수 초기화 및 설정 */
        askStopRecruitment_dialog = new Dialog(RecruitmentPostDetailActivity.this);  // Dialog 초기화
        askStopRecruitment_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askStopRecruitment_dialog.setContentView(R.layout.dialog_ask_stop_recruitment); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askStopRecruitment_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(view -> finish());

        /** (홈 이미지) 클릭 시 */
        binding.homeImageButton.setOnClickListener(view -> startActivity(new Intent(RecruitmentPostDetailActivity.this, HomeActivity.class)));

        /** (더 보기) 클릭 시 */
        binding.moreImageButton.setOnClickListener(view -> selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        binding.moreImageButton.setVisibility(isWriter ? View.VISIBLE : View.GONE);

        // 스크롤 뷰에 리스너 추가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.activityBodySrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    // 예를 들어 스크롤 위치가 573px 이상이면 액티비티 헤더 스타일 변경
                    binding.backImageButton.setImageResource(scrollY >= 573 ? R.drawable.arrow_back : R.drawable.arrow_back_white);
                    binding.homeImageButton.setImageResource(scrollY >= 573 ? R.drawable.home_normal : R.drawable.home_normal_white);
                    binding.moreImageButton.setImageResource(scrollY >= 573 ? R.drawable.more : R.drawable.more_white);
                    binding.activityHeaderRelativeLayout.setBackground(scrollY >= 573 ? getResources().getDrawable(R.drawable.light_gray_border_bottom) : null);
                }
            });
        }

        binding.likeImageView.setImageResource(isLiked ? R.drawable.like_filled : R.drawable.like_bolder_gray);

        binding.likeImageView.setOnClickListener(view -> {
            isLiked = !isLiked;
            binding.likeImageView.setImageResource(isLiked ? R.drawable.like_filled : R.drawable.like_bolder_gray);
        });

        recruitmentComplete();

        selectPostManagementBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.selectPostManagementBottomSheet_layout));

        selectPostManagementBottomSheetBehavior.setDraggable(false);
        selectPostManagementBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        // 어두운 배경 클릭 이벤트 설정
        binding.backgroundDimmer.setOnClickListener(view -> {
            if (selectPostManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // (더 보기 버튼) -> 수정
        findViewById(R.id.editPost_button).

                setOnClickListener(view ->

                {
                    if (selectPostManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        startActivity(new Intent(RecruitmentPostDetailActivity.this, EditRecruitmentPostActivity.class));
                    }
                });

        // (더 보기 버튼) -> 삭제
        findViewById(R.id.deletePost_button).

                setOnClickListener(view ->

                {
                    if (selectPostManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        showDialog_askDeletePost_dialog();
                    }
                });


        // (기능) 버튼 클릭 이벤트 설정
        binding.functionButton.setOnClickListener(view -> {
            if (binding.functionButton.getText().toString().equals("손 들기")) {
                showDialog_askJoinParty_dialog();
                return;
            }

            showDialog_askStopRecruitment_dialog();
        });

        binding.functionButton.setText(isWriter ? "마감하기" : "손 들기");

        // 모임 희망 장소 레이아웃 클릭 이벤트 설정
        binding.spotInfoRelativeLayout.setOnClickListener(view -> {
            binding.mapRelativeLayout.removeView(mapView);
            startActivity(new Intent(RecruitmentPostDetailActivity.this, SelectedSpotActivity.class));
        });
    }

    /**
     * (askDeletePost_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askDeletePost_dialog() {
        askDeletePost_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askDeletePost_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askDeletePost_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askDeletePost_dialog.dismiss());

        // (삭제) 버튼
        askDeletePost_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askDeletePost_dialog.dismiss(); // 다이얼로그 닫기
            startActivity(new Intent(RecruitmentPostDetailActivity.this, HomeActivity.class));
        });
    }

    /**
     * (askJoinParty_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askJoinParty_dialog() {
        askJoinParty_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askJoinParty_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askJoinParty_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askJoinParty_dialog.dismiss());

        // (확인) 버튼
        askJoinParty_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askJoinParty_dialog.dismiss(); // 다이얼로그 닫기
            new ToastSuccess("요청이 전송되었어요", RecruitmentPostDetailActivity.this);
        });
    }

    /**
     * (askStopRecruitment_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askStopRecruitment_dialog() {
        askStopRecruitment_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askStopRecruitment_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askStopRecruitment_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askStopRecruitment_dialog.dismiss());

        // (확인) 버튼
        askStopRecruitment_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askStopRecruitment_dialog.dismiss(); // 다이얼로그 닫기
            new ToastSuccess("모집이 마감되었어요", RecruitmentPostDetailActivity.this);
            isRecruitmentComplete = true;
            recruitmentComplete();
        });
    }

    // 모집이 완료된 경우 UI 표시/숨김 및 스타일, 허용성 수정
    public void recruitmentComplete() {
        binding.currentPartyMemberNumStateRelativeLayout.setVisibility(isRecruitmentComplete ? View.GONE : View.VISIBLE);
        binding.recruitmentCompleteTagTextView.setVisibility(isRecruitmentComplete ? View.VISIBLE : View.GONE);
        binding.functionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(isRecruitmentComplete ? R.color.disabled_widget_background_light_gray_color : R.color.theme_color)));
        binding.functionButton.setEnabled(isRecruitmentComplete ? false : true);
    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(RecruitmentPostDetailActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), RecruitmentPostDetailActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserInfo();

        /** 다음 카카오맵 지도를 띄우는 코드 */
        mapView = new MapView(this);
        mapView.setZoomLevel(2, true);

        mapViewContainer = binding.mapRelativeLayout;
        mapViewContainer.addView(mapView);
        binding.centerPointImageView.bringToFront();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /** 사용자의 현재 위치 */
        GetMyLocation getMyLocation = new GetMyLocation(this, this);
        Location userLocation = getMyLocation.getMyLocation();
        if (userLocation != null) {
            selectedLatitude = 36.625264039836026;
            selectedLongitude = 127.45708706510892;
            selectedPoint = MapPoint.mapPointWithGeoCoord(selectedLatitude, selectedLongitude);

            /** 중심점 변경 */
            mapView.setMapCenterPoint(selectedPoint, true);
        }
    }
}