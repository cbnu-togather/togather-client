package com.project.togather.createPost.community;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.GetMyLocation;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.chat.ChatDetailInfoItem;
import com.project.togather.community.CommunityPostDetailActivity;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.createPost.recruitment.SelectMeetingSpotActivity;
import com.project.togather.databinding.ActivityCreateCommunityPostBinding;
import com.project.togather.model.CoordinateToAddress;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.RetrofitServiceForKakao;
import com.project.togather.retrofit.interfaceAPI.CommunityAPI;
import com.project.togather.retrofit.interfaceAPI.KakaoAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateCommunityPostActivity extends AppCompatActivity {

    private ActivityCreateCommunityPostBinding binding;
    private LocationManager locationManager;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private CommunityAPI communityAPI;
    private RetrofitService retrofitService;
    private BottomSheetBehavior selectCategoryBottomSheetBehavior;

    private static final int REQUEST_GALLERY = 2;

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

    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateCommunityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        communityAPI = retrofitService.getRetrofit().create(CommunityAPI.class);

        RetrofitServiceForKakao retrofitServiceForKakao = new RetrofitServiceForKakao();
        KakaoAPI kakaoInterface = retrofitServiceForKakao.getRetrofit().create(KakaoAPI.class);

        selectedImageUri = Uri.parse("");

        // 전역 데이터 초기화
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedAddress", "");
        editor.putString("extractedDong", "");
        editor.apply();

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

//        Call<CoordinateToAddress> call = kakaoInterface.getAddress("WGS84", currLatitude, currLongitude);
//        call.enqueue(new Callback<CoordinateToAddress>() {
//            @Override
//            public void onResponse(Call<CoordinateToAddress> call, Response<CoordinateToAddress> response) {
//                if (response.isSuccessful()) {
//                    CoordinateToAddress responseData = response.body();
//                    List<CoordinateToAddress.Document> documents = responseData.getDocuments();
//                    if (!documents.isEmpty()) {
//                        CoordinateToAddress.Document document = documents.get(0);
//                        CoordinateToAddress.Address address = document.getAddress();
//                        String addressName = address.getAddressName();
//                        String extractedDong = address.extractDong(); // "동" 추출
//
//                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//
//                        // 조회된 주소에 '동' 정보가 없을 때 일반 주소를 저장
//                        if (extractedDong == null) {
//                            editor.putString("selectedAddress", addressName);
//                            editor.apply();
//                            finish();
//                            return;
//                        }
//
//                        editor.putString("extractedDong", extractedDong);
//                        editor.apply();
//                        finish();
//                    } else {
//                        System.out.println(response.code());
//                        new ToastWarning("서비스 불가 지역이에요", CreateCommunityPostActivity.this);
//                    }
//                } else {
//                    new ToastWarning("잘못된 요청이에요", CreateCommunityPostActivity.this);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<CoordinateToAddress> call, Throwable t) {
//                // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
//                new ToastWarning(getResources().getString(R.string.toast_server_error), CreateCommunityPostActivity.this);
//            }
//        });


        // X 이미지뷰 클릭 시 현재 액티비티 종료
        binding.closeActivityImageView.setOnClickListener(view -> finish());

        binding.selectCategoryRelativeLayout.setOnClickListener(view -> {
            hideKeyboard();
            selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        selectCategoryBottomSheetBehavior = BottomSheetBehavior.from(

                findViewById(R.id.createCommunityPostSelectCategoryBottomSheet_layout));

        selectCategoryBottomSheetBehavior.setDraggable(false);

        binding.rootRelativeLayout.post(new Runnable() {
            @Override
            public void run() {
                selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        selectCategoryBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
        binding.backgroundDimmer.setOnClickListener(view ->

        {
            if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // 게시글 카테고리 선택
        findViewById(R.id.famousRestaurantCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("맛집");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.famousRestaurantCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.famousRestaurantCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.hospitalAndPharmacyCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("병원/약국");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.hospitalAndPharmacyCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.hospitalAndPharmacyCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.lifeAndConvenienceCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("생활/편의");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.lifeAndConvenienceCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.lifeAndConvenienceCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.concernAndStoryCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("고민/사연");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.concernAndStoryCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.concernAndStoryCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.neighborhoodCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("동네친구");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.neighborhoodCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.neighborhoodCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.exerciseCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("운동");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.exerciseCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.exerciseCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.petCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("반려동물");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.petCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.petCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.beautyCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("미용");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.beautyCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.beautyCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.movingAndConstructionCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("이사/시공");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.movingAndConstructionCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.movingAndConstructionCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.residentialAndEstateCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("주거/부동산");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.residentialAndEstateCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.residentialAndEstateCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.educationCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("교육");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.educationCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.educationCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.hobbyCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("취미");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.hobbyCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.hobbyCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.neighborhoodAccidentCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("동네사건사고");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.neighborhoodAccidentCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.neighborhoodAccidentCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.neighborhoodSceneCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("동네풍경");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.neighborhoodSceneCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.neighborhoodSceneCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.lostAndMissingCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("분실/실종");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.lostAndMissingCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.lostAndMissingCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.pregnancyAndParentingCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("임신/육아");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.pregnancyAndParentingCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.pregnancyAndParentingCategorycheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.generalCategory_button).

                setOnClickListener(view ->

                {
                    if (selectCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("일반");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.generalCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.generalCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        /** (완료) 버튼 클릭 이벤트 설정 */
        binding.createCommunityPostButton.setOnClickListener(view ->

        {
            hideKeyboard();

            if (binding.postCategoryTextView.getText().toString().equals("게시글의 주제를 선택해 주세요")) {
                selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return;
            }

            if (binding.contentEditText.getText().toString().length() < 8) {
                new ToastWarning("본문을 8자 이상 작성해 주세요", CreateCommunityPostActivity.this);
                return;
            }

            performPost();

            startActivity(new Intent(CreateCommunityPostActivity.this, CommunityPostDetailActivity.class));
            finish();
        });

        /** (제목) 입력란 텍스트 입력 시 */
        binding.postTitleEditText.addTextChangedListener(new

                                                                 TextWatcher() {
                                                                     @Override
                                                                     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                                     }

                                                                     @Override
                                                                     public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                                     }

                                                                     @Override
                                                                     public void afterTextChanged(Editable s) {
                                                                         if (binding.postTitleEditText.getText().toString().length() > 0 && binding.contentEditText.getText().toString().length() > 0) {
                                                                             binding.createCommunityPostButton.setTextColor(getResources().getColor(R.color.text_color));
                                                                             binding.createCommunityPostButton.setEnabled(true);
                                                                             return;
                                                                         }

                                                                         binding.createCommunityPostButton.setTextColor(getResources().getColor(R.color.gray_text));
                                                                         binding.createCommunityPostButton.setEnabled(false);
                                                                     }
                                                                 });

        /** (본문) 입력란 텍스트 입력 시 */
        binding.contentEditText.addTextChangedListener(new

                                                               TextWatcher() {
                                                                   @Override
                                                                   public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                                   }

                                                                   @Override
                                                                   public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                                   }

                                                                   @SuppressLint("ResourceAsColor")
                                                                   @Override
                                                                   public void afterTextChanged(Editable s) {
                                                                       if (binding.postTitleEditText.getText().toString().length() > 0 && binding.contentEditText.getText().toString().length() > 0) {
                                                                           binding.createCommunityPostButton.setTextColor(getResources().getColor(R.color.text_color));
                                                                           binding.createCommunityPostButton.setEnabled(true);
                                                                           return;
                                                                       }

                                                                       binding.createCommunityPostButton.setTextColor(getResources().getColor(R.color.gray_text));
                                                                       binding.createCommunityPostButton.setEnabled(false);
                                                                   }
                                                               });

        // (갤러리) 이미지뷰 클릭 이벤트 설정
        findViewById(R.id.gallery_imageView).

                setOnClickListener(view ->

                {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, REQUEST_GALLERY);
                });

        // (키보드) 이미지뷰 클릭 이벤트 설정
        findViewById(R.id.hideKeyboard_imageView).

                setOnClickListener(view ->

                {
                    hideKeyboard();
                });

        // 게시글 섬네일 이미지 우측 상단의 X 버튼 클릭 이벤트 설정
        binding.deleteImageCardView.setOnClickListener(view ->

        {
            // 이미지 초기화
            binding.postThumbnailImageView.setImageResource(0);
            binding.postImageRelativeLayout.setVisibility(View.INVISIBLE);
            selectedImageUri = Uri.parse("");
        });
    }

    void clearCategoryStyle() {
        ((Button) findViewById(R.id.famousRestaurantCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.famousRestaurantCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.hospitalAndPharmacyCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.hospitalAndPharmacyCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.lifeAndConvenienceCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.lifeAndConvenienceCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.concernAndStoryCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.concernAndStoryCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.neighborhoodCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.neighborhoodCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.exerciseCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.exerciseCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.petCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.petCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.beautyCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.beautyCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.movingAndConstructionCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.movingAndConstructionCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.residentialAndEstateCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.residentialAndEstateCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.educationCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.educationCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.hobbyCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.hobbyCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.hobbyCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.hobbyCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.neighborhoodAccidentCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.neighborhoodAccidentCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.neighborhoodSceneCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.neighborhoodSceneCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.lostAndMissingCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.lostAndMissingCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.pregnancyAndParentingCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.pregnancyAndParentingCategorycheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.generalCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.generalCategoryCheck_imageView)).setVisibility(View.INVISIBLE);
    }


    /**
     * 키보드를 숨기는 함수
     */
    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void updateImage(Uri imageUri) {
        selectedImageUri = imageUri;
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Glide.with(binding.postThumbnailImageView)
                    .load(imageUri) // 이미지 URL 가져오기
                    .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                    .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                    .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
            binding.postImageRelativeLayout.setVisibility(View.VISIBLE);
        } catch (
                Exception e) {
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY:
                    // 갤러리에서 이미지를 선택했을 때의 처리
                    Uri newSelectedImageUri = data.getData();
                    updateImage(newSelectedImageUri);
                    break;
            }
        }
    }

    // Uri를 실제 파일로 변환하는 메서드
    private File uriToFile(Uri uri, Context context) {
        String uniqueFileName = "upload_" + UUID.randomUUID().toString() + ".jpg";
        File file = new File(context.getCacheDir(), uniqueFileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.e("File Conversion", "Error converting Uri to File", e);
        }
        return file;
    }

    private void performPost() {
        String title = binding.postTitleEditText.getText().toString();
        String content = binding.contentEditText.getText().toString();
        float latitude = (float)currLatitude;
        float longitude = (float)currLongitude;
//        String address = (sp_extractedDong.isEmpty() ? sp_selectedAddress : sp_extractedDong);
        String address = "개신동";
        String category = binding.postCategoryTextView.getText().toString();

        if (!selectedImageUri.toString().isEmpty()) {
            File file = uriToFile(selectedImageUri, CreateCommunityPostActivity.this);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body =  MultipartBody.Part.createFormData("img", file.getName(), requestFile);

            Call<ResponseBody> call = communityAPI.createCommunityPost(title, content, latitude,
                    longitude, address, category, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("작성이 완료되었어요", CreateCommunityPostActivity.this);

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                    new ToastWarning(getResources().getString(R.string.toast_server_error), CreateCommunityPostActivity.this);
                }
            });
        } else {
            Call<ResponseBody> call = communityAPI.createCommunityPostWithoutImg(title, content,
                    latitude, longitude, address, category);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("작성이 완료되었어요", CreateCommunityPostActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    new ToastWarning(getResources().getString(R.string.toast_server_error), CreateCommunityPostActivity.this);
                }
            });
        }
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
                    startActivity(new Intent(CreateCommunityPostActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), CreateCommunityPostActivity.this);
            }
        });
    }

    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        sp_extractedDong = sharedPreferences.getString("extractedDong", "");
        sp_selectedAddress = sharedPreferences.getString("selectedAddress", "");

    }

    public void resolveLocationSettings(Exception exception) {
        ResolvableApiException resolvable = (ResolvableApiException) exception;
        try {
            resolvable.startResolutionForResult(this, REQUEST_CODE_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 마커 등록
     */
    public void addMakerToMap(double latitude, double longitude) {
    }


}