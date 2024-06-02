package com.project.togather.createPost.recruitment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.GetMyLocation;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.databinding.ActivitySelectMeetingSpotBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.model.CoordinateToAddress;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.RetrofitServiceForKakao;
import com.project.togather.retrofit.interfaceAPI.KakaoAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectMeetingSpotActivity extends AppCompatActivity implements net.daum.mf.map.api.MapView.CurrentLocationEventListener, net.daum.mf.map.api.MapView.MapViewEventListener, net.daum.mf.map.api.MapView.POIItemEventListener {

    private ActivitySelectMeetingSpotBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
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
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    private static double currLatitude, currLongitude, selectedLatitude, selectedLongitude;

    private Context context = this;
    private Activity activity = this;

    private KakaoAPI kakaoInterface;

    private BottomSheetBehavior inputMeetingSpotBottomSheetBehavior;

    String sp_extractedDong, sp_selectedAddress, sp_addSpotName;

    float sp_selectedLatitude, sp_selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectMeetingSpotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        // 전역 데이터 로드
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        sp_extractedDong = sharedPreferences.getString("extractedDong", "");
        sp_selectedAddress = sharedPreferences.getString("selectedAddress", "");
        sp_addSpotName = sharedPreferences.getString("addSpotName", "");
        sp_selectedLatitude = sharedPreferences.getFloat("selectedLatitude", 0);
        sp_selectedLongitude = sharedPreferences.getFloat("selectedLongitude", 0);

        RetrofitServiceForKakao retrofitServiceForKakao = new RetrofitServiceForKakao();
        KakaoAPI kakaoInterface = retrofitServiceForKakao.getRetrofit().create(KakaoAPI.class);

        // X 이미지뷰 클릭 시 현재 액티비티 종료
        binding.closeActivityImageView.setOnClickListener(view -> finish());

        /** 다음 카카오맵 지도를 띄우는 코드 */
        mapView = new MapView(this);
        mapView.setZoomLevel(2, true);
        mapView.setMapViewEventListener(this);

        mapViewContainer = binding.mapRelativeLayout;
        mapViewContainer.addView(mapView);
        binding.centerPointImageView.bringToFront();
        binding.markerTagCardView.bringToFront();

        /** 현재 나의 위치에 점을 갱신하며 찍어줌 */
        mapView.setCurrentLocationTrackingMode(net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);

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
            currPoint = MapPoint.mapPointWithGeoCoord(currLatitude, currLongitude);

            // 앱 초기 실행 시에만 현재 위치를 지도 중심점으로 위치시킴
            if (selectedLongitude == 0) {
                /** 중심점 변경 */
                mapView.setMapCenterPoint(currPoint, true);
            }
        }

        inputMeetingSpotBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.inputMeetingSpotBottomSheet_layout));

        inputMeetingSpotBottomSheetBehavior.setDraggable(false);

        inputMeetingSpotBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        EditText spotName_editText = findViewById(R.id.spotName_editText);
        Button addMeetingSpot_button = findViewById(R.id.addMeetingSpot_button);

        /** (장소명 입력란) 포커스 시 */
        spotName_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                spotName_editText.setBackground(getResources().getDrawable(hasFocus ? R.drawable.black_border : R.drawable.light_gray_border));
            }
        });

        /** (장소명 입력란) 내용 입력 시 */
        spotName_editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력하기 전에 호출됩니다.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력이 변경될 때 호출됩니다.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // (모임 장소 등록) 버튼 활성화/비활성화 상태 제어
                addMeetingSpot_button.setEnabled(editable.toString().length() > 0 ? true : false);
                addMeetingSpot_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(editable.toString().length() > 0 ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
            }
        });

        // (현재 위치로 이동) 카드뷰 레이아웃 클릭 이벤트 설정
        binding.moveCurrentLocationCardView.setOnClickListener(view ->
                mapView.setMapCenterPoint(currPoint, true)
        );

        binding.selectDeliverySpotButton.setOnClickListener(view -> {
            inputMeetingSpotBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });


        // (모임 장소 등록) 버튼 이벤트 설정
        addMeetingSpot_button.setOnClickListener(view -> {
            Call<CoordinateToAddress> call = kakaoInterface.getAddress("WGS84", selectedLongitude, selectedLatitude);
            call.enqueue(new Callback<CoordinateToAddress>() {
                @Override
                public void onResponse(Call<CoordinateToAddress> call, Response<CoordinateToAddress> response) {
                    if (response.isSuccessful()) {
                        CoordinateToAddress responseData = response.body();
                        List<CoordinateToAddress.Document> documents = responseData.getDocuments();
                        if (!documents.isEmpty()) {
                            CoordinateToAddress.Document document = documents.get(0);
                            CoordinateToAddress.Address address = document.getAddress();
                            String addressName = address.getAddressName();
                            String extractedDong = address.extractDong(); // "동" 추출

                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString("addSpotName", spotName_editText.getText().toString());
                            editor.putFloat("selectedLatitude", (float) selectedLatitude);
                            editor.putFloat("selectedLongitude", (float) selectedLongitude);

                            // 조회된 주소에 '동' 정보가 없을 때 일반 주소를 저장
                            if (extractedDong == null) {
                                editor.putString("selectedAddress", addressName);
                                editor.apply();
                                finish();
                                return;
                            }

                            editor.putString("extractedDong", extractedDong);
                            editor.apply();
                            finish();
                        } else {
                            new ToastWarning("서비스 불가 지역이에요", SelectMeetingSpotActivity.this);
                        }
                    } else {
                        new ToastWarning("잘못된 요청이에요", SelectMeetingSpotActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<CoordinateToAddress> call, Throwable t) {
                    // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                    new ToastWarning(getResources().getString(R.string.toast_server_error), SelectMeetingSpotActivity.this);
                }
            });
        });

        if (!sp_addSpotName.equals("")) {
            binding.markerTagTextView.setText(sp_addSpotName);
            currPoint = MapPoint.mapPointWithGeoCoord(sp_selectedLatitude, sp_selectedLongitude);
            mapView.setMapCenterPoint(currPoint, true);
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

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                /** 사용자의 현재 위치 */
                GetMyLocation getMyLocation = new GetMyLocation(context, activity);
                Location userLocation = getMyLocation.getMyLocation();
                if (userLocation != null) {
                    currLatitude = userLocation.getLatitude();
                    currLongitude = userLocation.getLongitude();
                    System.out.println("////////////현재 내 위치값 : " + currLatitude + "," + currLongitude);
                    currPoint = MapPoint.mapPointWithGeoCoord(currLatitude, currLongitude);

                    /** 중심점 변경 */
                    mapView.setMapCenterPoint(currPoint, true);
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    resolveLocationSettings(e);
                }
            }
        });
    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(SelectMeetingSpotActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), SelectMeetingSpotActivity.this);
            }
        });
    }

    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
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

    /**
     * 카카오맵 이벤트 리스너
     */
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        selectedPoint = mapPoint;
        selectedLatitude = mapPoint.getMapPointGeoCoord().latitude;
        selectedLongitude = mapPoint.getMapPointGeoCoord().longitude;
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    /**
     * 지도 한 번 클릭 시
     */
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    public void handlePOIItemSelected(MapPOIItem mapPOIItem) {
    }

    /**
     * 지도 두 번 클릭 시
     */
    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        binding.markerTagCardView.setVisibility(View.GONE);
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        handlePOIItemSelected(mapPOIItem);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem
            mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint
            mapPoint) {
    }

}