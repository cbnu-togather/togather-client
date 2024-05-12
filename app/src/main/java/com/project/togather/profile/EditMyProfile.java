package com.project.togather.profile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.databinding.ActivityEditMyProfileBinding;
import com.project.togather.databinding.ActivityProfileBinding;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class EditMyProfile extends AppCompatActivity {

    private ActivityEditMyProfileBinding binding;

    private Uri selectedImageUri;
    private Bitmap bitmap;
    private UserAPI userAPI;
    private TokenManager tokenManager;
    private RetrofitService retrofitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        getUserInfo();

        setupListeners();
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

                        String userName = jsonObject.getString("name");
                        String photo = jsonObject.getString("photo");

                        // 내 유저 이름을 표시
                        binding.usernameEditText.setText(userName);

                        // 내 프로필 사진을 표시
                        Glide.with(EditMyProfile.this)
                                .load(photo)
                                .placeholder(R.drawable.one_person_logo)
                                .error(R.drawable.one_person_logo)
                                .into(binding.userProfileImageRoundedImageView);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), EditMyProfile.this);
            }
        });
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

    private void setupListeners() {
        // X 이미지뷰 클릭 시 현재 액티비티 종료
        binding.closeActivityImageView.setOnClickListener(view -> finish());

        // 프로필 수정
        binding.editMyProfileCompleteButton.setOnClickListener(view -> {
            // 유저 이름 가져오기
            String userName = binding.usernameEditText.getText().toString();

            // 이미지를 선택하지 않는 경우 (프로필 사진은 변경하지 않는 경우)
            if (selectedImageUri != null) {
                File file = uriToFile(selectedImageUri, EditMyProfile.this);

                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("img", file.getName(), requestFile);

                Call<ResponseBody> call = userAPI.updateUserProfile(userName, body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            new ToastSuccess("정보가 수정되었어요", EditMyProfile.this);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                        new ToastWarning(getResources().getString(R.string.toast_server_error), EditMyProfile.this);
                    }
                });

            } else {
                // 유저 이름만 변경하는 경우
                Call<ResponseBody> call = userAPI.updateUserProfile(userName, null);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            new ToastSuccess("회원정보 수정 완료", EditMyProfile.this);
                            if (tokenManager.getToken() != null) {
                                getUserInfo();
                            }

                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                        new ToastWarning(getResources().getString(R.string.toast_server_error), EditMyProfile.this);
                    }
                });
            }

        });

        binding.userProfileImageGroupRelativeLayout.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            activityResultLauncher.launch(intent);
        });

        /** (닉네임) 입력란 포커스 시 */
        binding.usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.usernameEditText.setBackground(getResources().getDrawable(hasFocus ? R.drawable.black_border : R.drawable.light_gray_border));
            }
        });

        /** (닉네임) 입력란 텍스트 입력 시 */
        binding.usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateUsername(s.toString());
            }
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri newSelectedImageUri = result.getData().getData();
                    updateImage(newSelectedImageUri);
                    // 이미지 변경 후 사용자 이름 유효성을 다시 확인
                    validateUsername(binding.usernameEditText.getText().toString());
                }
            }
    );

    private void updateImage(Uri imageUri) {
        selectedImageUri = imageUri;
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.one_person_logo)  // 로딩 중 표시할 이미지
                .error(R.drawable.one_person_logo)  // 에러 발생 시 표시할 이미지
                .into(binding.userProfileImageRoundedImageView);  // ImageView에 이미지 설정
    }

    private void validateUsername(String input) {
        boolean isValid = input.matches("[a-zA-Z0-9가-힣]+") && !input.contains(" ") && input.length() >= 2;
        binding.editMyProfileCompleteButton.setEnabled(isValid);
        binding.editMyProfileCompleteButton.setTextColor(ContextCompat.getColor(this, isValid ? R.color.text_color : R.color.gray_text));
        binding.usernameEditTextHelperTextView.setVisibility(isValid ? View.GONE : View.VISIBLE);
        binding.usernameEditTextHelperTextView.setText(!isValid && input.length() < 2 ? "닉네임은 2자 이상 입력해 주세요." : "닉네임은 띄어쓰기 없이 한글, 영문, 숫자만 가능해요.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 토큰 값이 없다면 메인 액티비티로 이동
        if (tokenManager.getToken() == null) {
            startActivity(new Intent(EditMyProfile.this, MainActivity.class));
            finish();
        }
    }
}