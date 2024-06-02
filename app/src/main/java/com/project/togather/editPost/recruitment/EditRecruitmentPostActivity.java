package com.project.togather.editPost.recruitment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.databinding.ActivityEditRecruitmentPostBinding;
import com.project.togather.editPost.community.EditCommunityPostCommentActivity;
import com.project.togather.home.PostDetailsItem;
import com.project.togather.home.RecruitmentPostDetailActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.RecruitmentAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRecruitmentPostActivity extends AppCompatActivity {

    private ActivityEditRecruitmentPostBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RecruitmentAPI recruitmentAPI;
    private RetrofitService retrofitService;
    private BottomSheetBehavior selectFoodCategoryBottomSheetBehavior;

    private static final int REQUEST_GALLERY = 2;

    String sp_extractedDong, sp_selectedAddress, sp_addSpotName;

    float sp_selectedLatitude, sp_selectedLongitude;
    Uri selectedImageUri;
    private static int postId;
    final int[] recruitmentNum = {1};

    // 데이터 하드 코딩
    private String response_selectedCategory;
    private PostDetailsItem postDetailsItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditRecruitmentPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        recruitmentAPI = retrofitService.getRetrofit().create(RecruitmentAPI.class);

        Intent intentEdit = getIntent();
        postId = intentEdit.getIntExtra("post_id", 0);

        selectedImageUri = Uri.parse("");


        // 전역 데이터 초기화
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedAddress", "");
        editor.putString("extractedDong", "");
        editor.putString("addSpotName", "");
        editor.putFloat("selectedLatitude", 0);
        editor.putFloat("selectedLongitude", 0);
        editor.apply();


        Call<ResponseBody> call = recruitmentAPI.getRecruitmentPostDetail(postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonString = response.body().string();
                        Log.d("jsonString", "onResponse: " + jsonString);
                        Gson gson = new Gson();
                        postDetailsItem = gson.fromJson(jsonString, PostDetailsItem.class);

                        response_selectedCategory = postDetailsItem.getCategory();
                        switch (response_selectedCategory) {
                            case "치킨":
                                findViewById(R.id.chickenCategory_button).performClick();
                                break;
                            case "피자":
                                findViewById(R.id.pizzaCategory_button).performClick();
                                break;
                            case "햄버거":
                                findViewById(R.id.hamburgerCategory_button).performClick();
                                break;
                            case "한식":
                                findViewById(R.id.koreanFoodCategory_button).performClick();
                                break;
                            case "일식":
                                findViewById(R.id.japaneseFoodCategory_button).performClick();
                                break;
                            case "중식":
                                findViewById(R.id.chineseFoodCategory_button).performClick();
                                break;
                            case "양식":
                                findViewById(R.id.westernFoodCategory_button).performClick();
                                break;
                            case "간식":
                                findViewById(R.id.snackCategory_button).performClick();
                                break;
                            case "카페·디저트":
                                findViewById(R.id.cafeAndDessertCategory_button).performClick();
                                break;
                            case "일반":
                                findViewById(R.id.generalCategory_button).performClick();
                                break;
                            default:
                                Log.d("로그 : ", response_selectedCategory + "는 존재하지 않는 카테고리입니다.");
                        }

                        if (postDetailsItem.getImg() != null) {
                            selectedImageUri = Uri.parse(postDetailsItem.getImg());
                            Glide.with(binding.postThumbnailImageView)
                                    .load(selectedImageUri) // 이미지 URL 가져오기
                                    .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                                    .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                                    .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
                            binding.postThumbnailRelativeLayout.setVisibility(selectedImageUri.equals("") ? View.GONE : View.VISIBLE);
                        }

                        binding.postTitleEditText.setText(postDetailsItem.getTitle());
                        binding.contentEditText.setText(postDetailsItem.getContent());
                        binding.selectDeliverySpotTextView.setText(postDetailsItem.getSpotName());

                        recruitmentNum[0] = postDetailsItem.getHeadCount();

                        binding.recruitmentNumTextView.setText(recruitmentNum[0] + "명");

                        binding.decreaseRecruitmentNumImageView.setImageResource(recruitmentNum[0] == 1 ? R.drawable.minus_light_gray : R.drawable.minus);
                        binding.decreaseRecruitmentNumImageView.setEnabled(recruitmentNum[0] == 1 ? false : true);
                        binding.increaseRecruitmentNumImageView.setImageResource(recruitmentNum[0] == 3 ? R.drawable.plus_300_light_gray : R.drawable.plus_300);
                        binding.increaseRecruitmentNumImageView.setEnabled(recruitmentNum[0] == 3 ? false : true);

                        // 모집 인원 (-) 버튼 클릭 이벤트 설정
                        binding.decreaseRecruitmentNumImageView.setOnClickListener(view -> {
                            if (recruitmentNum[0] > 0) {
                                binding.recruitmentNumTextView.setText(--recruitmentNum[0] + "명");
                                binding.increaseRecruitmentNumImageView.setImageResource(recruitmentNum[0] == 3 ? R.drawable.plus_300_light_gray : R.drawable.plus_300);
                                binding.increaseRecruitmentNumImageView.setEnabled(recruitmentNum[0] == 3 ? false : true);

                                binding.decreaseRecruitmentNumImageView.setImageResource(recruitmentNum[0] == 1 ? R.drawable.minus_light_gray : R.drawable.minus);
                                binding.decreaseRecruitmentNumImageView.setEnabled(recruitmentNum[0] == 1 ? false : true);
                            }
                        });

                        // 모집 인원 (+) 버튼 클릭 이벤트 설정
                        binding.increaseRecruitmentNumImageView.setOnClickListener(view -> {
                            if (recruitmentNum[0] < 3) {
                                binding.recruitmentNumTextView.setText(++recruitmentNum[0] + "명");
                                binding.decreaseRecruitmentNumImageView.setImageResource(recruitmentNum[0] == 1 ? R.drawable.minus_light_gray : R.drawable.minus);
                                binding.decreaseRecruitmentNumImageView.setEnabled(recruitmentNum[0] == 1 ? false : true);

                                binding.increaseRecruitmentNumImageView.setImageResource(recruitmentNum[0] == 3 ? R.drawable.plus_300_light_gray : R.drawable.plus_300);
                                binding.increaseRecruitmentNumImageView.setEnabled(recruitmentNum[0] == 3 ? false : true);
                            }
                        });

                        // 전역 데이터 설정
                        editor.putString("selectedAddress", postDetailsItem.getAddress());
                        editor.putString("extractedDong", postDetailsItem.getAddress());
                        editor.putString("addSpotName", postDetailsItem.getSpotName());
                        editor.putFloat("selectedLatitude", (float)postDetailsItem.getLatitude());
                        editor.putFloat("selectedLongitude", (float)postDetailsItem.getLongitude());
                        editor.apply();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), EditRecruitmentPostActivity.this);
            }
        });

        // X 이미지뷰 클릭 시 현재 액티비티 종료
        binding.closeActivityImageView.setOnClickListener(view -> finish());

        binding.selectCategoryRelativeLayout.setOnClickListener(view -> {
            hideKeyboard();
            selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        selectFoodCategoryBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.createRecruitmentPostSelectFoodCategoryBottomSheet_layout));

        selectFoodCategoryBottomSheetBehavior.setDraggable(false);

        selectFoodCategoryBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
            if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // 음식 종류 선택
        findViewById(R.id.chickenCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("치킨");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.chickenCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.chickenCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.pizzaCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("피자");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.pizzaCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.pizzaCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.hamburgerCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("햄버거");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.hamburgerCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.hamburgerCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.koreanFoodCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("한식");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.koreanFoodCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.koreanFoodCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.japaneseFoodCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("일식");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.japaneseFoodCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.japaneseFoodCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.chineseFoodCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("중식");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.chineseFoodCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.chineseFoodCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.westernFoodCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("양식");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.westernFoodCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.westernFoodCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.snackCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("간식");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.snackCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.snackCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.cafeAndDessertCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("카페·디저트");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.cafeAndDessertCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.cafeAndDessertCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        findViewById(R.id.generalCategory_button).

                setOnClickListener(view ->

                {
                    if (selectFoodCategoryBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        binding.postCategoryTextView.setText("일반");
                        clearCategoryStyle();
                        ((Button) findViewById(R.id.generalCategory_button)).setTextColor(getResources().getColor(R.color.theme_color));
                        ((ImageView) findViewById(R.id.generalCategoryCheck_imageView)).setVisibility(View.VISIBLE);
                    }
                });

        binding.galleryContentRelativeLayout.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, REQUEST_GALLERY);
        });

        // 게시글 섬네일 이미지 우측 상단의 X 버튼 클릭 이벤트 설정
        binding.deleteImageCardView.setOnClickListener(view -> {
            // 이미지 초기화
            binding.postThumbnailImageView.setImageResource(0);
            binding.postThumbnailRelativeLayout.setVisibility(View.INVISIBLE);
            selectedImageUri = Uri.parse("");
        });

        /** (입력란) 포커스 스타일 일괄 설정 */
        binding.postTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.postTitleEditText.setBackground(getResources().getDrawable(hasFocus ? R.drawable.black_border : R.drawable.light_gray_border));
            }
        });

        binding.postTitleEditText.addTextChangedListener(new TextWatcher() {

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
                // (작성 완료) 버튼 활성화/비활성화 상태 제어
                binding.createRecruitmentPostButton.setEnabled(editable.toString().length() > 0 && binding.contentEditText.getText().toString().length() > 0 ? true : false);
                binding.createRecruitmentPostButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(editable.toString().length() > 0 && binding.contentEditText.getText().toString().length() > 0 ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
            }
        });

        binding.contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.contentEditText.setBackground(getResources().getDrawable(hasFocus ? R.drawable.black_border : R.drawable.light_gray_border));
            }
        });

        binding.contentEditText.addTextChangedListener(new TextWatcher() {

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
                // (작성 완료) 버튼 활성화/비활성화 상태 제어
                binding.createRecruitmentPostButton.setEnabled(editable.toString().length() > 0 && binding.postTitleEditText.getText().toString().length() > 0);
                binding.createRecruitmentPostButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(editable.toString().length() > 0 && binding.postTitleEditText.getText().toString().length() > 0 ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
            }
        });

        binding.selectDeliverySpotRelativeLayout.setOnClickListener(view ->
                startActivity(new Intent(EditRecruitmentPostActivity.this, EditRecruitmentPostSelectMeetingSpotActivity.class)));

        binding.createRecruitmentPostButton.setOnClickListener(view -> {
            hideKeyboard();

            if (binding.postCategoryTextView.getText().toString().equals("음식의 종류를 선택해 주세요")) {
                selectFoodCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return;
            }

            if (binding.contentEditText.getText().toString().length() < 8) {
                new ToastWarning("본문을 8자 이상 작성해 주세요", EditRecruitmentPostActivity.this);
                return;
            }

            updatePost();

            Intent intent = new Intent(EditRecruitmentPostActivity.this, RecruitmentPostDetailActivity.class);
            intent.putExtra("post_id", postId);
            startActivity(intent);
            finish();
        });

    }

    void clearCategoryStyle() {
        ((Button) findViewById(R.id.chickenCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.chickenCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.pizzaCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.pizzaCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.hamburgerCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.hamburgerCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.koreanFoodCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.koreanFoodCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.japaneseFoodCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.japaneseFoodCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.chineseFoodCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.chineseFoodCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.westernFoodCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.westernFoodCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.snackCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.snackCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

        ((Button) findViewById(R.id.cafeAndDessertCategory_button)).setTextColor(getResources().getColor(R.color.text_color));
        ((ImageView) findViewById(R.id.cafeAndDessertCategoryCheck_imageView)).setVisibility(View.INVISIBLE);

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
            Glide.with(binding.postThumbnailImageView)
                    .load(imageUri) // 이미지 URL 가져오기
                    .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                    .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                    .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
            binding.postThumbnailRelativeLayout.setVisibility(View.VISIBLE);
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
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    private void updatePost() {
        String title = binding.postTitleEditText.getText().toString();
        String content = binding.contentEditText.getText().toString();
        float latitude = sp_selectedLatitude;
        float longitude = sp_selectedLongitude;
        int headCount = recruitmentNum[0];
        String address = (sp_extractedDong.isEmpty() ? sp_selectedAddress : sp_extractedDong);
        String spotName = sp_addSpotName;
        String category = binding.postCategoryTextView.getText().toString();

        Log.d("Debug", "Title: " + title);
        Log.d("Debug", "Content: " + content);
        Log.d("Debug", "Latitude: " + latitude);
        Log.d("Debug", "Longitude: " + longitude);
        Log.d("Debug", "Head Count: " + headCount);
        Log.d("Debug", "Address: " + address);
        Log.d("Debug", "Spot Name: " + spotName);
        Log.d("Debug", "Category: " + category);

        if (!selectedImageUri.toString().isEmpty()) {
            File file = uriToFile(selectedImageUri, EditRecruitmentPostActivity.this);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body =  MultipartBody.Part.createFormData("img", file.getName(), requestFile);

            Call<ResponseBody> call = recruitmentAPI.updateRecruitmentPost(postId, title, content, latitude,
                    longitude, headCount, address, spotName, category, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                    new ToastWarning(getResources().getString(R.string.toast_server_error), EditRecruitmentPostActivity.this);
                }
            });

        } else {
            Call<ResponseBody> call = recruitmentAPI.updateRecruitmentPostWithoutImg(postId, title, content, latitude,
                    longitude, headCount, address, spotName, category);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("수정이 완료되었어요", EditRecruitmentPostActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                    new ToastWarning(getResources().getString(R.string.toast_server_error), EditRecruitmentPostActivity.this);
                }
            });
        }

    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(EditRecruitmentPostActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), EditRecruitmentPostActivity.this);
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
        sp_addSpotName = sharedPreferences.getString("addSpotName", "");
        sp_selectedLatitude = sharedPreferences.getFloat("selectedLatitude", 0);
        sp_selectedLongitude = sharedPreferences.getFloat("selectedLongitude", 0);

        if (binding.postTitleEditText.getText().toString().length() > 0 && binding.contentEditText.getText().toString().length() > 0 && !sp_addSpotName.equals("")) {
            binding.createRecruitmentPostButton.setEnabled(true);
            binding.createRecruitmentPostButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.theme_color)));
        }

        binding.selectDeliverySpotTextView.setTextColor(getResources().getColor(R.color.text_color));
        binding.selectDeliverySpotTextView.setText(sp_addSpotName);
    }
}