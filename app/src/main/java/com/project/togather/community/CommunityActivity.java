package com.project.togather.community;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.databinding.ActivityCommunityBinding;
import com.project.togather.notification.NotificationActivity;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.R;
import com.project.togather.home.HomeActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.CommunityAPI;
import com.project.togather.retrofit.interfaceAPI.KakaoAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class CommunityActivity extends AppCompatActivity {

    private ActivityCommunityBinding binding;

    private RecyclerViewAdapter adapter;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private CommunityAPI communityAPI;
    private RetrofitService retrofitService;
    private LocationManager locationManager;

    ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();

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

    String sp_extractedDong, sp_selectedAddress;


    private Context context = this;
    private Activity activity = this;

    private KakaoAPI kakaoInterface;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private BottomSheetBehavior selectCreatePostTypeBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        communityAPI = retrofitService.getRetrofit().create(CommunityAPI.class);

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
                Intent intent = new Intent(CommunityActivity.this, CommunityPostDetailActivity.class);
                intent.putExtra("post_id", selectedItem.getId());
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                PostInfoItem selectedItem = postInfoItems.get(pos);
                Intent intent = new Intent(CommunityActivity.this, CommunityPostDetailActivity.class);
                intent.putExtra("post_id", selectedItem.getId());
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

        /** "알림" 버튼 클릭 시 */
        binding.notificationImageButton.setOnClickListener(view ->
                startActivity(new Intent(CommunityActivity.this, NotificationActivity.class)));

        /** "신규 알림" 버튼 클릭 시 */
        binding.notificationNewImageButton.setOnClickListener(view ->
                startActivity(new Intent(CommunityActivity.this, NotificationActivity.class)));

        /** "홈" 레이아웃 클릭 시 */
        binding.homeActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(CommunityActivity.this, HomeActivity.class));
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
                startActivity(new Intent(CommunityActivity.this, CreateRecruitmentPostActivity.class));
            }
        });

        findViewById(R.id.createCommunityPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(CommunityActivity.this, CreateCommunityPostActivity.class));
            }
        });

        /** "채팅" 레이아웃 클릭 시 */
        binding.chatActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(CommunityActivity.this, ChatActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        /** "내 정보" 레이아웃 클릭 시 */
        binding.profileActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(CommunityActivity.this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    /**
     * 리스트뷰 어댑터
     */
    public class ListViewAdapter extends BaseAdapter {
        ArrayList<PostInfoItem> items = new ArrayList<PostInfoItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(PostInfoItem item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();
            final PostInfoItem postInfoItem = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_view_item_community, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            ImageView post_imageView = convertView.findViewById(R.id.post_imageView);

            TextView postTitle_textView = convertView.findViewById(R.id.postTitle_textView);
            TextView district_textView = convertView.findViewById(R.id.district_textView);
            TextView elapsedTime_textView = convertView.findViewById(R.id.elapsedTime_textView);


            ImageView liked_imageView = convertView.findViewById(R.id.liked_imageView);
            TextView likedCnt_textView = convertView.findViewById(R.id.likedCnt_textView);

            postTitle_textView.setText(postInfoItem.getTitle());
            district_textView.setText(postInfoItem.getAddress());

            long elapsedTime = postInfoItem.getElapsedTime();
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

            post_imageView.setImageResource(R.drawable.community_temp_image_1);

            likedCnt_textView.setText("" + postInfoItem.getAddress());

            //각 아이템 선택 event
            convertView.setOnClickListener(view ->
                    startActivity(new Intent(CommunityActivity.this, CommunityPostDetailActivity.class)));

            return convertView;  //뷰 객체 반환
        }
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<PostInfoItem> items = new ArrayList<>();

        public interface OnItemClickListener {
            void onItemClick(int pos);
        }

        private RecyclerViewAdapter.OnItemClickListener onItemClickListener = null;

        public void setOnItemClickListener(RecyclerViewAdapter.OnItemClickListener listener) {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_community, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
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
            ImageView liked_imageView;

            TextView hotPostTag_textView;
            TextView categoryTag_textView;
            TextView postTitle_textView;
            TextView postContent_textView;
            TextView district_textView;
            TextView elapsedTime_textView;
            TextView likedCnt_textView;

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

                hotPostTag_textView = itemView.findViewById(R.id.hotPostTag_textView);
                categoryTag_textView = itemView.findViewById(R.id.categoryTag_textView);
                postTitle_textView = itemView.findViewById(R.id.postTitle_textView);
                postContent_textView = itemView.findViewById(R.id.postContent_textView);
                district_textView = itemView.findViewById(R.id.district_textView);
                elapsedTime_textView = itemView.findViewById(R.id.elapsedTime_textView);

                likedCnt_textView = itemView.findViewById(R.id.likedCnt_textView);
            }

            void onBind(PostInfoItem item) {
                if (item.getImg() != null && item.getImg().equals("")) {
                    post_imageView.setVisibility(View.GONE);
                    postTitle_textView.setMaxWidth(1000);
                } else {
                    Glide.with(itemView)
                            .load(item.getImg()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                hotPostTag_textView.setVisibility(item.getLikes() > 5 ? View.VISIBLE : View.GONE);
                categoryTag_textView.setText(item.getCategory());

                String postTitle = item.getTitle();
                ViewGroup.LayoutParams layoutParams = root_relativeLayout.getLayoutParams();
                if (postTitle.length() >= 28)
                    postTitle = postTitle.substring(0, 28) + "...";
                else if (item.getImg() != null && !item.getImg().equals("") && postTitle.length() >= 19)
                    postTitle = postTitle.substring(0, 19) + "...";

                postTitle_textView.setText(postTitle);

                String postContent = item.getContent();
                if (item.getImg() != null && item.getImg().equals("") && postContent.length() >= 29)
                    postContent = postContent.substring(0, 29) + "...";
                else if(item.getImg() != null && !item.getImg().equals("") && postContent.length() >= 24)
                    postContent = postContent.substring(0, 24) + "...";
                postContent_textView.setText(postContent);

                district_textView.setText(item.getAddress());

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

                likedCnt_textView.setText("" + item.getLikes());
            }
        }
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

    // 초기 데이터 로딩 함수
    private void loadData() {
        Call<List<PostInfoItem>> call = communityAPI.getCommunityPostList(currLatitude, currLongitude);
        call.enqueue(new Callback<List<PostInfoItem>>() {
            @Override
            public void onResponse(Call<List<PostInfoItem>> call, Response<List<PostInfoItem>> response) {
                if (response.isSuccessful()) {
                    postInfoItems.clear();
                    List<PostInfoItem> reversedList = response.body();
                    Collections.reverse(reversedList);
                    postInfoItems.addAll(reversedList);
                    adapter.setPostInfoList(postInfoItems);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<PostInfoItem>> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), CommunityActivity.this);
            }
        });

//        Call<ResponseBody> call = communityAPI.getCommunityPostList(currLatitude, currLongitude);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    try {
//                        postInfoItems.clear();
//                        String responseBody = response.body().string();
//                        Type listType = new TypeToken<ArrayList<CommunityInfoResponse>>() {}.getType();
//                        ArrayList<CommunityInfoResponse> postList = new Gson().fromJson(responseBody, listType);
//
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
//                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
//
//                        // 현재 시간 UTC로 생성
//                        Date now = new Date();
//                        for (CommunityInfoResponse post : postList) {
//                            try {
//                                String createdAtString = post.getCreatedAt();
//                                Date createdAt = sdf.parse(createdAtString);
//                                long elapsedTime = (now.getTime() - createdAt.getTime()) / 1000;
//                                PostInfoItem item = new PostInfoItem(
//                                        post.getId(),
//                                        post.getImg(),
//                                        post.getCategory(),
//                                        post.getTitle(),
//                                        post.getContent(),
//                                        post.getAddress(),
//                                        elapsedTime,
//                                        post.getLikes()
//                                );
//                                postInfoItems.add(0, item);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        adapter.setPostInfoList(postInfoItems);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
//                new ToastWarning(getResources().getString(R.string.toast_server_error), CommunityActivity.this);
//            }
//        });


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
                    startActivity(new Intent(CommunityActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), CommunityActivity.this);
            }
        });
    }
    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
        loadData();
    }
}