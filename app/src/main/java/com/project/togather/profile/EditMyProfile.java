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
import android.view.View;
import android.widget.Toast;

import com.project.togather.R;
import com.project.togather.databinding.ActivityEditMyProfileBinding;
import com.project.togather.toast.ToastSuccess;

import java.io.InputStream;

public class EditMyProfile extends AppCompatActivity {

    private ActivityEditMyProfileBinding binding;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        binding.usernameEditText.setText("김감자");
    }

    private void setupListeners() {
        binding.closeActivityImageView.setOnClickListener(view -> finish());

        binding.editMyProfileCompleteButton.setOnClickListener(view -> {
            new ToastSuccess("정보가 변경되었어요", EditMyProfile.this);
            finish();
        });

        binding.userProfileImageGroupRelativeLayout.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            activityResultLauncher.launch(intent);
        });

        binding.usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
                    Uri selectedImageUri = result.getData().getData();
                    updateProfileImage(selectedImageUri);
                    // 이미지 변경 후 사용자 이름 유효성을 다시 확인
                    validateUsername(binding.usernameEditText.getText().toString());
                }
            }
    );

    private void updateProfileImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            bitmap = BitmapFactory.decodeStream(inputStream);
            binding.userProfileImageRoundedImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateUsername(String input) {
        boolean isValid = input.matches("[a-zA-Z0-9가-힣]+") && !input.contains(" ") && input.length() >= 2;
        binding.editMyProfileCompleteButton.setEnabled(isValid);
        binding.editMyProfileCompleteButton.setTextColor(ContextCompat.getColor(this, isValid ? R.color.text_color : R.color.gray_text));
        binding.usernameEditTextHelperTextView.setVisibility(isValid ? View.GONE : View.VISIBLE);
        binding.usernameEditTextHelperTextView.setText(!isValid && input.length() < 2 ? "닉네임은 2자 이상 입력해 주세요." : "닉네임은 띄어쓰기 없이 한글, 영문, 숫자만 가능해요.");
    }
}