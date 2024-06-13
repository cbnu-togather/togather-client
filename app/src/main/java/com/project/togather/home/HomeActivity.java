package com.project.togather.home;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.togather.GetMyLocation;
import com.project.togather.MainActivity;
import com.project.togather.chat.ChatActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.databinding.ActivityHomeBinding;
import com.project.togather.notification.NotificationActivity;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.R;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.KakaoAPI;
import com.project.togather.retrofit.interfaceAPI.RecruitmentAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.user.LoginActivity;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private LocationManager locationManager;
    private RecyclerViewAdapter adapter;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RecruitmentAPI recruitmentAPI;
    private RetrofitService retrofitService;

    /**
     * 위치 권한 요청 코드의 상숫값
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1981;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2981;

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Callback for Location events.
     */

    private LocationSettingsRequest mLocationSettingsRequest;

    private static MapView mapView;
    private static ViewGroup mapViewContainer;
    private MapPoint currPoint, selectedPoint;
    private MapPOIItem marker;

    /**
     * 위치 설정에 대한 객체 변수
     */
    private static final int REQUEST_CODE_LOCATION = 2;
    private static double currLatitude, currLongitude;
    private static int distance = 300;
    private static String currCategory = "all";

    private Context context = this;
    private Activity activity = this;

    private KakaoAPI kakaoInterface;


    String sp_extractedDong, sp_selectedAddress, sp_addSpotName;

    float sp_selectedLatitude, sp_selectedLongitude;

    private ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();


    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private BottomSheetBehavior selectCreatePostTypeBottomSheetBehavior;
    private BottomSheetBehavior selectDistanceBottomSheetBehavior;
    private BottomSheetBehavior homeAdsBottomSheetBehavior;

    static boolean isAdsClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        recruitmentAPI = retrofitService.getRetrofit().create(RecruitmentAPI.class);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        /** 앱 초기 실행 시 위치 권한 동의 여부에 따라서
         * (권한 획득 요청) 및 (현재 위치 표시)를 수행 */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /** 사용자의 현재 위치 */
        GetMyLocation getMyLocation = new GetMyLocation(this, this);
        Location userLocation = getMyLocation.getMyLocation();
        if (userLocation != null) {
            currLatitude = userLocation.getLatitude(); // 소프트웨어학부 건물 위도, 경도
            currLongitude = userLocation.getLongitude();
            System.out.println("////////////현재 내 위치값 : " + currLatitude + "," + currLongitude);
        }
        Log.d("위치", "lat: " + currLatitude + " lon : " + currLongitude);

        // Add callback listener
        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();  // 현재 액티비티와 같은 작업에 있는 모든 액티비티를 종료
            }
        });

        adapter = new RecyclerViewAdapter();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                PostInfoItem selectedItem = postInfoItems.get(pos);
                Intent intent = new Intent(HomeActivity.this, RecruitmentPostDetailActivity.class);
                intent.putExtra("post_id", selectedItem.getId());
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                PostInfoItem selectedItem = postInfoItems.get(pos);
                Intent intent = new Intent(HomeActivity.this, RecruitmentPostDetailActivity.class);
                intent.putExtra("post_id", selectedItem.getId());
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.postsRecyclerView.setAdapter(adapter);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // 초기 데이터 로드
        loadData();


        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(); // 데이터 새로고침 메소드 호출
            }
        });


        // 내 근처 거리 설정 bottom sheet layout
        selectDistanceBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.selectDistanceBottomSheet_layout));

        selectDistanceBottomSheetBehavior.setDraggable(false);

        selectDistanceBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        // 내 근처 거리 설정 레이아웃 클릭 이벤트 설정
        binding.selectDistanceRelativeLayout.setOnClickListener(view ->
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        // 내 근처 거리 선택 이벤트 설정
        findViewById(R.id.m300_button).setOnClickListener(view -> {
            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                distance = 300;
                binding.distanceTextView.setText("300m");

                postInfoItems.clear();
                loadData();
            }
        });

        findViewById(R.id.m500_button).setOnClickListener(view -> {
            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                distance = 500;
                binding.distanceTextView.setText("500m");

                postInfoItems.clear();
                loadData();
            }
        });

        findViewById(R.id.m1000_button).setOnClickListener(view -> {
            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                distance = 1000;
                binding.distanceTextView.setText("1km");

                postInfoItems.clear();
                loadData();
            }
        });

        // 홈 액티비티 광고 bottom sheet layout
        homeAdsBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.homeAdsBottomSheet_layout));

        homeAdsBottomSheetBehavior.setDraggable(false);

        homeAdsBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        // "오늘 하루동안 보지 않기" 버튼 클릭 시
        findViewById(R.id.notShowUntilToday_button).setOnClickListener(view -> {
            if (homeAdsBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                homeAdsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // "광고 닫기" 버튼 클릭 시
        findViewById(R.id.closeAds_button).setOnClickListener(view -> {
            if (homeAdsBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                homeAdsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        binding.openAdsButton.setOnClickListener(view ->
                homeAdsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        // 0.5초 후에 버튼 클릭 이벤트 실행
        binding.getRoot().postDelayed(() -> {
            if (!isAdsClosed) {
                binding.openAdsButton.performClick();
                isAdsClosed = true;
            }
        }, 500);

        /** "알림" 버튼 클릭 시 */
        binding.notificationImageButton.setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        /** "신규 알림" 버튼 클릭 시 */
        binding.notificationNewImageButton.setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        /** "전체" 탭 버튼 클릭 시 */
        binding.allFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.allFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.allFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.allFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "all";
            filterPostsByCategory(currCategory);

        });

        /** "치킨" 탭 버튼 클릭 시 */
        binding.chickenTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.chickenTabButton.setTypeface(null, Typeface.BOLD);
            binding.chickenTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.chickenTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "치킨";
            filterPostsByCategory(currCategory);
        });

        /** "피자" 탭 버튼 클릭 시 */
        binding.pizzaTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.pizzaTabButton.setTypeface(null, Typeface.BOLD);
            binding.pizzaTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.pizzaTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "피자";
            filterPostsByCategory(currCategory);
        });

        /** "햄버거" 탭 버튼 클릭 시 */
        binding.hamburgerTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.hamburgerTabButton.setTypeface(null, Typeface.BOLD);
            binding.hamburgerTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.hamburgerTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "햄버거";
            filterPostsByCategory(currCategory);
        });

        /** "한식" 탭 버튼 클릭 시 */
        binding.koreanFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.koreanFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.koreanFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.koreanFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "한식";
            filterPostsByCategory(currCategory);
        });

        /** "일식" 탭 버튼 클릭 시 */
        binding.japaneseFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.japaneseFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.japaneseFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.japaneseFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "일식";
            filterPostsByCategory(currCategory);
        });

        /** "중식" 탭 버튼 클릭 시 */
        binding.chineseFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.chineseFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.chineseFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.chineseFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "중식";
            filterPostsByCategory(currCategory);
        });

        /** "양식" 탭 버튼 클릭 시 */
        binding.westernFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.westernFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.westernFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.westernFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "양식";
            filterPostsByCategory(currCategory);
        });

        /** "분식" 탭 버튼 클릭 시 */
        binding.snackTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.snackTabButton.setTypeface(null, Typeface.BOLD);
            binding.snackTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.snackTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "분식";
            filterPostsByCategory(currCategory);
        });

        /** "카페·디저트" 탭 버튼 클릭 시 */
        binding.cafeAndDessertTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.cafeAndDessertTabButton.setTypeface(null, Typeface.BOLD);
            binding.cafeAndDessertTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.cafeAndDessertTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "카페·디저트";
            filterPostsByCategory(currCategory);
        });

        /** "일반" 탭 버튼 클릭 시 */
        binding.generalTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.generalTabButton.setTypeface(null, Typeface.BOLD);
            binding.generalTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.generalTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            currCategory = "일반";
            filterPostsByCategory(currCategory);
        });

        /** "동네생활" 레이아웃 클릭 시 */
        binding.communityActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, CommunityActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        // 어두운 배경 클릭 이벤트 설정
        binding.backgroundDimmer.setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (homeAdsBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                homeAdsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                startActivity(new Intent(HomeActivity.this, CreateRecruitmentPostActivity.class));
            }
        });

        findViewById(R.id.createCommunityPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(HomeActivity.this, CreateCommunityPostActivity.class));
            }
        });

        /** "채팅" 레이아웃 클릭 시 */
        binding.chatActivityRelativeLayout.setOnClickListener(view ->

        {
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });

        /** "내 정보" 레이아웃 클릭 시 */
        binding.profileActivityRelativeLayout.setOnClickListener(view ->

        {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<PostInfoItem> items = new ArrayList<>();

        public interface OnItemClickListener {
            void onItemClick(int pos);
        }

        private OnItemClickListener onItemClickListener = null;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }


        public interface OnLongItemClickListener {
            void onLongItemClick(int pos);
        }

        private OnLongItemClickListener onLongItemClickListener = null;

        public void setOnLongItemClickListener(OnLongItemClickListener listener) {
            this.onLongItemClickListener = listener;
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_home, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(items.get(position));
        }

        public void setPostInfoList(ArrayList<PostInfoItem> list) {
            this.items = list;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout root_relativeLayout;
            RelativeLayout content_relativeLayout;

            ImageView post_imageView;
            ImageView currentPartyMemberNumFirstState_imageView;
            ImageView currentPartyMemberSecondState_imageView;
            ImageView currentPartyMemberNumThirdState_imageView;
            ImageView liked_imageView;

            TextView postTitle_textView;
            TextView category_textView;
            TextView elapsedTime_textView;
            TextView recruitmentComplete_textView;
            TextView likedCnt_textView;

            CardView currentPartyMemberNumFirstState_cardView;
            CardView currentPartyMemberNumSecondState_cardView;
            CardView currentPartyMemberNumThirdState_cardView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (onItemClickListener != null) {
                                onItemClickListener.onItemClick(position);

                                // 클릭된 배경으로 변경
                                root_relativeLayout.setBackgroundColor(itemView.getResources().getColor(R.color.post_clicked_gray_color));
                                content_relativeLayout.setBackgroundColor(itemView.getResources().getColor(R.color.post_clicked_gray_color));

                                // 500 밀리초(0.5초) 후에 이전 배경색으로 변경
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 이전 배경색으로 변경
                                        root_relativeLayout.setBackground(itemView.getResources().getDrawable(R.drawable.list_item_view_border_bottom_white_background));
                                        content_relativeLayout.setBackground(itemView.getResources().getDrawable(R.drawable.list_item_view_border_bottom_white_background));
                                    }
                                }, 500); // 0.5초 지연
                            }
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (onLongItemClickListener != null) {
                                onLongItemClickListener.onLongItemClick(position);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                root_relativeLayout = itemView.findViewById(R.id.root_relativeLayout);
                content_relativeLayout = itemView.findViewById(R.id.content_relativeLayout);

                post_imageView = itemView.findViewById(R.id.post_imageView);

                postTitle_textView = itemView.findViewById(R.id.postTitle_textView);
                category_textView = itemView.findViewById(R.id.category_textView);
                elapsedTime_textView = itemView.findViewById(R.id.elapsedTime_textView);
                recruitmentComplete_textView = itemView.findViewById(R.id.recruitmentCompleteTag_textView);

                currentPartyMemberNumFirstState_cardView = itemView.findViewById(R.id.currentPartyMemberNumFirstState_cardView);
                currentPartyMemberNumSecondState_cardView = itemView.findViewById(R.id.currentPartyMemberNumSecondState_cardView);
                currentPartyMemberNumThirdState_cardView = itemView.findViewById(R.id.currentPartyMemberNumThirdState_cardView);

                currentPartyMemberNumFirstState_imageView = itemView.findViewById(R.id.currentPartyMemberNumFirstState_imageView);
                currentPartyMemberSecondState_imageView = itemView.findViewById(R.id.currentPartyMemberSecondState_imageView);
                currentPartyMemberNumThirdState_imageView = itemView.findViewById(R.id.currentPartyMemberNumThirdState_imageView);

                liked_imageView = itemView.findViewById(R.id.liked_imageView);

                likedCnt_textView = itemView.findViewById(R.id.likedCnt_textView);
            }

            void onBind(PostInfoItem item) {
                if (item.getImg() != null && item.getImg().equals("")) {
                    post_imageView.setImageResource(R.drawable.post_thumbnail_background_logo);
                } else {
                    Glide.with(itemView)
                            .load(item.getImg()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.post_thumbnail_background_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.post_thumbnail_background_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                postTitle_textView.setText(item.getTitle());
                switch (item.getCategory()) {
                    case "치킨":
                        category_textView.setText("치킨");
                        break;
                    case "피자":
                        category_textView.setText("피자");
                        break;
                    case "햄버거":
                        category_textView.setText("햄버거");
                        break;
                    case "한식":
                        category_textView.setText("한식");
                        break;
                    case "일식":
                        category_textView.setText("일식");
                        break;
                    case "중식":
                        category_textView.setText("중식");
                        break;
                    case "양식":
                        category_textView.setText("양식");
                        break;
                    case "분식":
                        category_textView.setText("분식");
                        break;
                    case "카페·디저트":
                        category_textView.setText("카페·디저트");
                        break;
                    case "일반":
                        category_textView.setText("일반");
                        break;
                    default:
                        Log.d("로그: ", item.getCategory() + "는 존재하지 않는 카테고리입니다.");
                }

                long elapsedTime = item.getElapsedTime();
                String elapsedTime_str;
                if (elapsedTime < 60) {
                    elapsedTime_str = elapsedTime + "초 전";
                } else if (elapsedTime < 3600) {
                    elapsedTime_str = elapsedTime / 60 + "분 전";
                } else if (elapsedTime < 86400) {
                    elapsedTime_str = elapsedTime / 3600 + "시간 전";
                } else if (elapsedTime < 86400 * 365) {
                    elapsedTime_str = elapsedTime / 86400 + "일 전";
                } else {
                    elapsedTime_str = elapsedTime / 86400 * 365 + "일 전";
                }
                elapsedTime_textView.setText(elapsedTime_str);

                if (item.isCompleted() || item.getHeadCount() == item.getCurrentCount()) {
                    recruitmentComplete_textView.setVisibility(View.VISIBLE);
                } else {
                    currentPartyMemberNumFirstState_cardView.setVisibility(item.getHeadCount() >= 1 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberNumFirstState_imageView.setImageResource(item.getCurrentCount() >= 1 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

                    currentPartyMemberNumSecondState_cardView.setVisibility(item.getHeadCount() >= 2 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberSecondState_imageView.setImageResource(item.getCurrentCount() >= 2 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

                    currentPartyMemberNumThirdState_cardView.setVisibility(item.getHeadCount() >= 3 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberNumThirdState_imageView.setImageResource(item.getCurrentCount() >= 3 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
                }

                liked_imageView.setImageResource(item.isLiked() ? R.drawable.like_filled : R.drawable.like_normal);
                likedCnt_textView.setText("" + item.getLikes());
            }
        }

    }

    // 음식 카테고리 탭에 설정된 스타일을 제거하는 함수
    void allTabStyleClear() {
        binding.allFoodTabButton.setBackground(null);
        binding.allFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.allFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.chickenTabButton.setBackground(null);
        binding.chickenTabButton.setTypeface(null, Typeface.NORMAL);
        binding.chickenTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.pizzaTabButton.setBackground(null);
        binding.pizzaTabButton.setTypeface(null, Typeface.NORMAL);
        binding.pizzaTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.hamburgerTabButton.setBackground(null);
        binding.hamburgerTabButton.setTypeface(null, Typeface.NORMAL);
        binding.hamburgerTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.koreanFoodTabButton.setBackground(null);
        binding.koreanFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.koreanFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.japaneseFoodTabButton.setBackground(null);
        binding.japaneseFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.japaneseFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.chineseFoodTabButton.setBackground(null);
        binding.chineseFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.chineseFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.westernFoodTabButton.setBackground(null);
        binding.westernFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.westernFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.snackTabButton.setBackground(null);
        binding.snackTabButton.setTypeface(null, Typeface.NORMAL);
        binding.snackTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.cafeAndDessertTabButton.setBackground(null);
        binding.cafeAndDessertTabButton.setTypeface(null, Typeface.NORMAL);
        binding.cafeAndDessertTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.generalTabButton.setBackground(null);
        binding.generalTabButton.setTypeface(null, Typeface.NORMAL);
        binding.generalTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));
    }

    /**
     * 음식 카테고리 탭에 맞게 게시글을 필터링하고 UI를 새로고침하는 함수
     */
    private void filterPostsByCategory(String category) {
        ArrayList<PostInfoItem> filteredItems = new ArrayList<>();
        if (!category.equals("all")) {
            for (PostInfoItem item : postInfoItems) {
                if (item.getCategory().equals(category)) {
                    filteredItems.add(item);
                }
            }
        } else {
            filteredItems.addAll(postInfoItems);
        }

        adapter.setPostInfoList(filteredItems); // 필터링된 리스트를 리사이클러 뷰에 설정
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.postsRecyclerView.setAdapter(adapter); // 어댑터를 다시 설정하여 갱신

    }

    // 데이터 새로고침 함수
    private void refreshData() {
        // 기존 데이터를 비우는 로직 추가
        postInfoItems.clear();

        loadData();

        // 어댑터에 변경된 데이터 리스트를 설정
        adapter.setPostInfoList(postInfoItems);

        // RecyclerView의 레이아웃 매니저와 어댑터를 다시 설정하여 UI를 갱신
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.postsRecyclerView.setAdapter(adapter);

        // 새로고침 아이콘을 숨김 (새로고침이 끝났음을 의미)
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    // 데이터 로딩 함수
    private void loadData() {
        Call<List<PostInfoItem>> call = recruitmentAPI.getRecruitmentPostList(36.6257, 127.4544, distance);
        call.enqueue(new Callback<List<PostInfoItem>>() {
            @Override
            public void onResponse(Call<List<PostInfoItem>> call, Response<List<PostInfoItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postInfoItems.clear();
                    List<PostInfoItem> reversedList = response.body();
                    Collections.reverse(reversedList); // 리스트를 역순으로 변경
                    postInfoItems.addAll(reversedList);
                    adapter.setPostInfoList(postInfoItems);
                    adapter.notifyDataSetChanged();

                    filterPostsByCategory(currCategory);
                }
            }

            @Override
            public void onFailure(Call<List<PostInfoItem>> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), HomeActivity.this);
            }
        });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission denied.
                for (String permission : permissions) {
                    if ("android.permission.ACCESS_FINE_LOCATION".equals(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("지도 사용을 위해 위치 권한을 허용해 주세요.\n(필수권한)");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /** 위치 정보 설정창에서 '설정으로 이동' 클릭 시 */
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /** 위치 정보 설정창에서 '취소' 클릭 시 */
//                                Toast.makeText(MainActivity.this, "Cancel Click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(0, 133, 254));
                                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(123, 123, 123));
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        }
    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), HomeActivity.this);
            }
        });
    }

    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
        loadData();
        currCategory = "all";
        if (distance >= 1000) {
            binding.distanceTextView.setText(distance / 1000 + "km");
        } else {
            binding.distanceTextView.setText(distance + "m");
        }
    }
}
