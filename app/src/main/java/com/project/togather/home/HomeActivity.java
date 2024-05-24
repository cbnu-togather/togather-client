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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
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
import com.project.togather.retrofit.interfaceAPI.RecruitmentAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.user.LoginActivity;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapPoint;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private LocationManager locationManager;
    private RecyclerViewAdapter adapter;
    private TokenManager tokenManager;
    private RecruitmentAPI recruitmentAPI;
    private RetrofitService retrofitService;

    /**
     * 위치 권한 요청 코드의 상숫값
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1981;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2981;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    private ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();

    private static double currLatitude, currLongitude;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private BottomSheetBehavior selectCreatePostTypeBottomSheetBehavior;
    private BottomSheetBehavior selectDistanceBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        recruitmentAPI = retrofitService.getRetrofit().create(RecruitmentAPI.class);

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
                Intent intent = new Intent(HomeActivity.this, RecruitmentPostDetailActivity.class);
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                Intent intent = new Intent(HomeActivity.this, RecruitmentPostDetailActivity.class);
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.postsRecyclerView.setAdapter(adapter);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(); // 데이터 새로고침 메소드 호출
            }
        });

        // 초기 데이터 로드
        loadData();

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
        findViewById(R.id.m100_button).setOnClickListener(view -> {
            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                binding.distanceTextView.setText("100m");
            }
        });

        findViewById(R.id.m300_button).setOnClickListener(view -> {
            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                binding.distanceTextView.setText("300m");
            }
        });

        findViewById(R.id.m500_button).setOnClickListener(view -> {
            if (selectDistanceBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectDistanceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                binding.distanceTextView.setText("500m");
            }
        });

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
            filterPostsByCategory("all");
        });

        /** "치킨" 탭 버튼 클릭 시 */
        binding.chickenTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.chickenTabButton.setTypeface(null, Typeface.BOLD);
            binding.chickenTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.chickenTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("chicken");
        });

        /** "피자" 탭 버튼 클릭 시 */
        binding.pizzaTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.pizzaTabButton.setTypeface(null, Typeface.BOLD);
            binding.pizzaTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.pizzaTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("pizza");
        });

        /** "햄버거" 탭 버튼 클릭 시 */
        binding.hamburgerTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.hamburgerTabButton.setTypeface(null, Typeface.BOLD);
            binding.hamburgerTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.hamburgerTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("hamburger");
        });

        /** "한식" 탭 버튼 클릭 시 */
        binding.koreanFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.koreanFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.koreanFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.koreanFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("korean_food");
        });

        /** "일식" 탭 버튼 클릭 시 */
        binding.japaneseFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.japaneseFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.japaneseFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.japaneseFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("japanese_food");
        });

        /** "중식" 탭 버튼 클릭 시 */
        binding.chineseFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.chineseFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.chineseFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.chineseFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("chinese_food");
        });

        /** "양식" 탭 버튼 클릭 시 */
        binding.westernFoodTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.westernFoodTabButton.setTypeface(null, Typeface.BOLD);
            binding.westernFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.westernFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("western_food");
        });

        /** "분식" 탭 버튼 클릭 시 */
        binding.snackTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.snackTabButton.setTypeface(null, Typeface.BOLD);
            binding.snackTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.snackTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("snack");
        });

        /** "카페·디저트" 탭 버튼 클릭 시 */
        binding.cafeAndDessertTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.cafeAndDessertTabButton.setTypeface(null, Typeface.BOLD);
            binding.cafeAndDessertTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.cafeAndDessertTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("cafe_and_dessert");
        });

        /** "일반" 탭 버튼 클릭 시 */
        binding.generalTabButton.setOnClickListener(view -> {
            allTabStyleClear();
            binding.generalTabButton.setTypeface(null, Typeface.BOLD);
            binding.generalTabButton.setTextColor(getResources().getColor(R.color.text_color));
            binding.generalTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            filterPostsByCategory("general");
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
                if (item.getPostThumbnailImageUrl().equals("")) {
                    post_imageView.setImageResource(R.drawable.post_thumbnail_background_logo);
                } else {
                    Glide.with(itemView)
                            .load(item.getPostThumbnailImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.post_thumbnail_background_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.post_thumbnail_background_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                postTitle_textView.setText(item.getTitle());
                switch (item.getCategory()) {
                    case "chicken":
                        category_textView.setText("치킨");
                        break;
                    case "pizza":
                        category_textView.setText("피자");
                        break;
                    case "hamburger":
                        category_textView.setText("햄버거");
                        break;
                    case "korean_food":
                        category_textView.setText("한식");
                        break;
                    case "japanese_food":
                        category_textView.setText("일식");
                        break;
                    case "chinese_food":
                        category_textView.setText("중식");
                        break;
                    case "western_food":
                        category_textView.setText("양식");
                        break;
                    case "snack":
                        category_textView.setText("분식");
                        break;
                    case "cafe_and_dessert":
                        category_textView.setText("카페·디저트");
                        break;
                    case "general":
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

                if (item.getMaxPartyMemberNum() == item.getCurrentPartyMemberNum()) {
                    recruitmentComplete_textView.setVisibility(View.VISIBLE);
                } else {
                    currentPartyMemberNumFirstState_cardView.setVisibility(item.getMaxPartyMemberNum() >= 1 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberNumFirstState_imageView.setImageResource(item.getCurrentPartyMemberNum() >= 1 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

                    currentPartyMemberNumSecondState_cardView.setVisibility(item.getMaxPartyMemberNum() >= 2 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberSecondState_imageView.setImageResource(item.getCurrentPartyMemberNum() >= 2 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

                    currentPartyMemberNumThirdState_cardView.setVisibility(item.getMaxPartyMemberNum() >= 3 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberNumThirdState_imageView.setImageResource(item.getCurrentPartyMemberNum() >= 3 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
                }

                liked_imageView.setImageResource(item.isLikedState() ? R.drawable.like_filled : R.drawable.like_normal);
                likedCnt_textView.setText("" + item.getLikedCnt());
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

        // 새 데이터 추가 (하드 코딩) : 새로고침 했더니 게시글이 두 개만 남았다는 가정
//        postInfoItems.add(new PostInfoItem("https://cdn.mkhealth.co.kr/news/photo/202306/64253_68458_1153.png", "개신동 교촌치킨 파티 구함", "chicken", 320, 3, 2, false, 1));
//        postInfoItems.add(new PostInfoItem("https://cdn.dominos.co.kr/admin/upload/goods/20240214_8rBc1T61.jpg?RS=350x350&SP=1", "도미노 피자 드실분 구해요", "pizza", 160, 3, 3, false, 0));
        loadData();
        // 어댑터에 변경된 데이터 리스트를 설정
        adapter.setPostInfoList(postInfoItems);

        // RecyclerView의 레이아웃 매니저와 어댑터를 다시 설정하여 UI를 갱신
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.postsRecyclerView.setAdapter(adapter);

        // 새로고침 아이콘을 숨김 (새로고침이 끝났음을 의미)
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    // 초기 데이터 로딩 함수
    private void loadData() {
        // Adapter 안에 아이템의 정보 담기 (하드 코딩)

        Call<ResponseBody> call = recruitmentAPI.getRecruitmentPostList(33, 35);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("responseBody", "onResponse: " + responseBody);
                        Type listType = new TypeToken<ArrayList<PostInfoResponse>>(){}.getType();
                        ArrayList<PostInfoResponse> postList = new Gson().fromJson(responseBody, listType);

                        Log.d("postlist", "onResponse: " + postList);

                        LocalDateTime now = LocalDateTime.now();

                        for (PostInfoResponse post : postList) {
                            LocalDateTime createdAt = LocalDateTime.parse(post.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME);
                            long elapsedTime = Duration.between(now, createdAt).getSeconds();

                            PostInfoItem item = new PostInfoItem(
                                    post.getImg(),
                                    post.getTitle(),
                                    post.getCategory(),
                                    elapsedTime,
                                    post.getHeadCount(),
                                    post.getCurrentCount(),
                                    post.isLiked(),
                                    post.getLikes()
                            );

                            postInfoItems.add(item);

                        }
                        adapter.setPostInfoList(postInfoItems);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), HomeActivity.this);
            }
        });

//        postInfoItems.add(new PostInfoItem("https://cdn.mkhealth.co.kr/news/photo/202306/64253_68458_1153.png", "개신동 교촌치킨 파티 구함", "chicken", 320, 3, 2, false, 1));
//        postInfoItems.add(new PostInfoItem("https://cdn.dominos.co.kr/admin/upload/goods/20240214_8rBc1T61.jpg?RS=350x350&SP=1", "도미노 피자 드실분 구해요", "pizza", 160, 3, 3, false, 0));
//        postInfoItems.add(new PostInfoItem("https://mblogthumb-phinf.pstatic.net/MjAyMjA3MjhfMTY5/MDAxNjU4OTkyODg0NTA3.z8WzaZAOKBvo4JkSm9lTMOTiNsKEUNHZJYRB-DPZCdEg.0WdqohiJPsSM5pXWYl-HvTE3JUVlUPe7LT-U6wvjUQwg.JPEG.duwlsrjdwb/KakaoTalk_20220728_151114228_10.jpg?type=w800", "사창동 우리집 닭강정 파티!!", "chicken", 500, 1, 0, false, 0));
//        postInfoItems.add(new PostInfoItem("https://image.kmib.co.kr/online_image/2024/0131/2024013114261427977_1706678775_0019120339.jpg", "맘스터치 배달 파티 999~~", "hamburger", 600, 3, 3, true, 2));
//        postInfoItems.add(new PostInfoItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS6LTXILpqDk2KY425YAGSIAdF84ogxh-OFRz2P51EPvA&s", "행컵 그룹 구해용", "korean_food", 550, 2, 1, false, 0));
//        postInfoItems.add(new PostInfoItem("", "짚신 스시 & 롤 배달 구해요", "japanese_food", 555, 1, 0, true, 1));
//        postInfoItems.add(new PostInfoItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRj0BYs-iE7kn3fmdg0eVBxtqO89kwVRBFe_3Y8uZrgMA&s", "대장집 파티 구", "chinese_food", 560, 2, 1, false, 0));
//        postInfoItems.add(new PostInfoItem("https://d12zq4w4guyljn.cloudfront.net/750_750_20201122041810_photo1_5831aaf849cf.jpg", "파브리카 배달 구해용", "western_food", 700, 2, 2, false, 1));
//        postInfoItems.add(new PostInfoItem("https://media-cdn.tripadvisor.com/media/photo-s/12/31/92/d9/1519804025288-largejpg.jpg", "신전 떡볶이 구해유", "snack", 900, 3, 2, false, 2));
//        postInfoItems.add(new PostInfoItem("https://d12zq4w4guyljn.cloudfront.net/750_750_20230517093845_photo1_edd2f5913a1b.jpg", "메가커피 999", "cafe_and_dessert", 1000, 1, 0, false, 2));
//        postInfoItems.add(new PostInfoItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ-1FF9Hpe-_ERtrBHcUDeeckMOeOzm6IWylD_mJJlJEQ&s", "컴포즈 배달 구해요!!!", "cafe_and_dessert", 1500, 1, 1, false, 1));
//
//        adapter.setPostInfoList(postInfoItems);
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
    @Override
    protected void onResume() {
        super.onResume();

        // 토큰 값이 없다면 메인 액티비티로 이동
        if (tokenManager.getToken() == null) {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }




    }
}
