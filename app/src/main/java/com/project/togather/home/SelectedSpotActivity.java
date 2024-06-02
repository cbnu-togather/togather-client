package com.project.togather.home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;

import com.project.togather.GetMyLocation;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.databinding.ActivitySelectedSpotBinding;
import com.project.togather.editPost.recruitment.EditRecruitmentPostSelectMeetingSpotActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectedSpotActivity extends AppCompatActivity implements net.daum.mf.map.api.MapView.CurrentLocationEventListener, net.daum.mf.map.api.MapView.MapViewEventListener, net.daum.mf.map.api.MapView.POIItemEventListener {

    private ActivitySelectedSpotBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RetrofitService retrofitService;

    private Dialog askJoinParty_dialog;

    private static MapView mapView;
    private static ViewGroup mapViewContainer;
    private MapPoint currPoint, selectedPoint;
    private MapPOIItem marker;

    /**
     * 위치 설정에 대한 객체 변수
     */
    private LocationManager locationManager;
    private static double currLatitude, currLongitude, selectedLatitude, selectedLongitude;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectedSpotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        /** (손 들기 확인) 다이얼로그 변수 초기화 및 설정 */
        askJoinParty_dialog = new Dialog(SelectedSpotActivity.this);  // Dialog 초기화
        askJoinParty_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askJoinParty_dialog.setContentView(R.layout.dialog_ask_join_party); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askJoinParty_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Add callback listener
        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                binding.mapRelativeLayout.removeView(mapView);
                finish();
            }
        });

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(view -> {
            binding.mapRelativeLayout.removeView(mapView);
            finish();
        });

        /** 다음 카카오맵 지도를 띄우는 코드 */
        mapView = new MapView(this);
        mapView.setZoomLevel(2, true);

        mapViewContainer = binding.mapRelativeLayout;
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);

        /** 현재 나의 위치에 점을 갱신하며 찍어줌 */
        mapView.setCurrentLocationTrackingMode(net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /** 사용자의 현재 위치 */
        GetMyLocation getMyLocation = new GetMyLocation(this, this);
        Location userLocation = getMyLocation.getMyLocation();
        if (userLocation != null) {
            // currLatitude = userLocation.getLatitude();
            // currLongitude = userLocation.getLongitude();
            currLatitude = 36.62565323814696; // 소프트웨어학부 건물 위도, 경도
            currLongitude = 127.45428323069932;
            selectedLatitude = 36.625264039836026; // 학역산 건물 출입문 앞 위도, 경도
            selectedLongitude = 127.45708706510892;
            currPoint = MapPoint.mapPointWithGeoCoord(currLatitude, currLongitude);
            selectedPoint = MapPoint.mapPointWithGeoCoord(selectedLatitude, selectedLongitude);

            /** 중심점 변경 */
            mapView.setMapCenterPoint(selectedPoint, true);
        }

        // (현재 위치로 이동) 카드뷰 레이아웃 클릭 이벤트 설정
        binding.moveCurrentLocationCardView.setOnClickListener(view -> {
            binding.markerTagCardView.setVisibility(View.GONE);
            mapView.setMapCenterPoint(currPoint, true);
        });

        addMakerToMap(selectedLatitude, selectedLongitude, "학연산 출입문 앞");

        binding.joinPartyButton.setOnClickListener(view ->
                showDialog_askJoinParty_dialog());
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
            new ToastSuccess("요청이 전송되었어요", SelectedSpotActivity.this);
            binding.mapRelativeLayout.removeView(mapView);
            finish();
        });
    }

    /**
     * 마커 등록
     */
    public void addMakerToMap(double selectedLatitude, double selectedLongitude, String spotName) {
        marker = new MapPOIItem();
        marker.setItemName(spotName);
        marker.setTag(0);
        // 원하는 위치에 마커 추가
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(selectedLatitude, selectedLongitude); // 마커의 위도, 경도 설정
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setCustomImageResourceId(R.drawable.marker);

        mapView.addPOIItem(marker);
    }

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

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        // 지도를 드래그 하면 장소명 말풍선을 사라지게 함
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

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(SelectedSpotActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), SelectedSpotActivity.this);
            }
        });
    }
    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
    }
}