package com.project.togather.editPost.community;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.community.CommunityPostDetailActivity;
import com.project.togather.community.CommunityPostDetailItem;
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.databinding.ActivityCommunityPostDetailBinding;
import com.project.togather.databinding.ActivityEditCommunityPostBinding;
import com.project.togather.editPost.recruitment.EditRecruitmentPostActivity;
import com.project.togather.home.RecruitmentPostDetailActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.CommunityAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCommunityPostActivity extends AppCompatActivity {

    private ActivityEditCommunityPostBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private CommunityAPI communityAPI;
    private RetrofitService retrofitService;
    private BottomSheetBehavior selectCategoryBottomSheetBehavior;

    private static final int REQUEST_GALLERY = 2;

    Uri selectedImageUri;
    private static int postId;
    private CommunityPostDetailItem communityPostDetailItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCommunityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        communityAPI = retrofitService.getRetrofit().create(CommunityAPI.class);

        Intent intentEdit = getIntent();
        postId = intentEdit.getIntExtra("post_id", 0);

        Call<ResponseBody> call = communityAPI.getCommunityPostDetail(postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        communityPostDetailItem = gson.fromJson(jsonString, CommunityPostDetailItem.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 데이터 하드 코딩
                    String response_selectedCategory = communityPostDetailItem.getCategory();
                    switch (response_selectedCategory) {
                        case "맛집":
                            findViewById(R.id.famousRestaurantCategory_button).performClick();
                            break;
                        case "병원/약국":
                            findViewById(R.id.hospitalAndPharmacyCategory_button).performClick();
                            break;
                        case "생활/편의":
                            findViewById(R.id.lifeAndConvenienceCategory_button).performClick();
                            break;
                        case "고민/사연":
                            findViewById(R.id.concernAndStoryCategory_button).performClick();
                            break;
                        case "동네친구":
                            findViewById(R.id.neighborhoodCategory_button).performClick();
                            break;
                        case "운동":
                            findViewById(R.id.exerciseCategory_button).performClick();
                            break;
                        case "반려동물":
                            findViewById(R.id.petCategory_button).performClick();
                            break;
                        case "미용":
                            findViewById(R.id.beautyCategory_button).performClick();
                            break;
                        case "이사/시공":
                            findViewById(R.id.movingAndConstructionCategory_button).performClick();
                            break;
                        case "주거/부동산":
                            findViewById(R.id.residentialAndEstateCategory_button).performClick();
                            break;
                        case "교육":
                            findViewById(R.id.educationCategory_button).performClick();
                            break;
                        case "취미":
                            findViewById(R.id.hobbyCategory_button).performClick();
                            break;
                        case "동네사건사고":
                            findViewById(R.id.neighborhoodAccidentCategory_button).performClick();
                            break;
                        case "동네풍경":
                            findViewById(R.id.neighborhoodSceneCategory_button).performClick();
                            break;
                        case "분실/실종":
                            findViewById(R.id.lostAndMissingCategory_button).performClick();
                            break;
                        case "임신/육아":
                            findViewById(R.id.pregnancyAndParentingCategory_button).performClick();
                            break;
                        case "일반":
                            findViewById(R.id.generalCategory_button).performClick();
                            break;
                        default:
                            Log.d("로그 : ", response_selectedCategory + "는 존재하지 않는 카테고리입니다.");
                    }
                    if (communityPostDetailItem.getImg() != null) {
                        selectedImageUri = Uri.parse(communityPostDetailItem.getImg());
                        Glide.with(binding.postThumbnailImageView)
                                .load(selectedImageUri) // 이미지 URL 가져오기
                                .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                                .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                                .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
                        binding.postThumbnailImageView.setVisibility(selectedImageUri.equals("") ? View.GONE : View.VISIBLE);
                    }
                    binding.postTitleEditText.setText(communityPostDetailItem.getTitle());
                    binding.contentEditText.setText(communityPostDetailItem.getContent());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostActivity.this);
            }
        });


        // X 이미지뷰 클릭 시 현재 액티비티 종료
        binding.closeActivityImageView.setOnClickListener(view -> finish());

        binding.selectCategoryRelativeLayout.setOnClickListener(view -> {
            hideKeyboard();
            selectCategoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        selectCategoryBottomSheetBehavior = BottomSheetBehavior.from(

                findViewById(R.id.createCommunityPostSelectCategoryBottomSheet_layout));

        selectCategoryBottomSheetBehavior.setDraggable(false);

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
                new ToastWarning("본문을 8자 이상 작성해 주세요", EditCommunityPostActivity.this);
                return;
            }

            updatePost();

            Intent intent = new Intent(EditCommunityPostActivity.this, CommunityPostDetailActivity.class);
            intent.putExtra("post_id", postId);
            startActivity(intent);
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
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    private void updatePost() {
        String title = binding.postTitleEditText.getText().toString();
        String content = binding.contentEditText.getText().toString();
        String category = binding.postCategoryTextView.getText().toString();

        if (!selectedImageUri.toString().isEmpty()) {
            File file = uriToFile(selectedImageUri, EditCommunityPostActivity.this);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body =  MultipartBody.Part.createFormData("img", file.getName(), requestFile);

            Call<ResponseBody> call = communityAPI.updateCommunityPost(postId, title, content, category, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("수정이 완료되었어요", EditCommunityPostActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostActivity.this);
                }
            });
        } else {
            Call<ResponseBody> call = communityAPI.updateCommunityPostWithoutImg(postId, title, content, category);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("수정이 완료되었어요", EditCommunityPostActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                    new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostActivity.this);
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
                    startActivity(new Intent(EditCommunityPostActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostActivity.this);
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