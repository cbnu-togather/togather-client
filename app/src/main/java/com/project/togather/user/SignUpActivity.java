package com.project.togather.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.content.res.ColorStateList;

import com.project.togather.R;
import com.project.togather.databinding.ActivitySignUpBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private boolean isAgreeOurPolicies = false;

    private UserAPI userAPI;
    private TokenManager tokenManager;
    private RetrofitService retrofitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneNumberEditText.setText(phoneNumber);

        binding.backImageButton.setOnClickListener(view -> finish());

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

        binding.agreeOurPoliciesCheckboxRoundedImageView.setOnClickListener(view -> {
            isAgreeOurPolicies = !isAgreeOurPolicies;
            binding.agreeOurPoliciesCheckboxRoundedImageView.setImageResource(isAgreeOurPolicies ? R.drawable.check_circle_green : R.drawable.check_circle_gray);
            validateUsername(binding.usernameEditText.getText().toString());
        });

        binding.agreeOurPoliciesTextRelativeLayout.setOnClickListener(this::openPolicyActivity);
        binding.arrowRightImageButton.setOnClickListener(this::openPolicyActivity);

        binding.signUpButton.setOnClickListener(view -> performSignUp());
    }

    private void validateUsername(String username) {
        boolean isValidUsername = username.matches("[a-zA-Z0-9가-힣]+") && username.length() >= 2 && !username.contains(" ");
        updateUsernameHelperText(isValidUsername, username);

        // 버튼 활성화는 닉네임 유효성과 동의란 체크를 모두 고려
        updateSignUpButtonState(isValidUsername && isAgreeOurPolicies);
    }

    private void updateUsernameHelperText(boolean isValidUsername, String username) {
        if (isValidUsername) {
            binding.usernameEditTextHelperTextView.setVisibility(View.GONE);
        } else {
            binding.usernameEditTextHelperTextView.setVisibility(View.VISIBLE);
            binding.usernameEditTextHelperTextView.setText(username.length() < 2 ? "닉네임은 2자 이상 입력해 주세요." : "닉네임은 띄어쓰기 없이 한글, 영문, 숫자만 가능해요.");
        }
    }

    private void updateSignUpButtonState(boolean enable) {
        binding.signUpButton.setEnabled(enable);
        binding.signUpButton.setTextColor(getResources().getColor(enable ? R.color.white : R.color.disabled_widget_text_deep_gray_color));
        binding.signUpButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(enable ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
    }

    private void setupListeners() {
        binding.backImageButton.setOnClickListener(view -> finish());

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

        binding.agreeOurPoliciesCheckboxRoundedImageView.setOnClickListener(view -> {
            isAgreeOurPolicies = !isAgreeOurPolicies;
            binding.agreeOurPoliciesCheckboxRoundedImageView.setImageResource(isAgreeOurPolicies ? R.drawable.check_circle_green : R.drawable.check_circle_gray);
            // 버튼 활성화 상태 업데이트
            updateSignUpButtonState(binding.usernameEditText.getText().toString().matches("[a-zA-Z0-9가-힣]+") && binding.usernameEditText.getText().length() >= 2 && !binding.usernameEditText.getText().toString().contains(" ") && isAgreeOurPolicies);
        });

        binding.agreeOurPoliciesTextRelativeLayout.setOnClickListener(this::openPolicyActivity);
        binding.arrowRightImageButton.setOnClickListener(this::openPolicyActivity);

        binding.signUpButton.setOnClickListener(view -> performSignUp());
    }

    private void performSignUp() {
        String username = binding.usernameEditText.getText().toString().trim();
        String phoneNumber = binding.phoneNumberEditText.getText().toString().replaceAll("\\s", "");

        if (username.equals("exist")) {
            new ToastWarning("이미 존재하는 유저 이름입니다.", this);
            return;
        }

        Call<ResponseBody> call = userAPI.signUp(phoneNumber, username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    new ToastSuccess("회원가입 완료", SignUpActivity.this);
                    startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), SignUpActivity.this);
            }
        });
    }

    private void openPolicyActivity(View view) {
        startActivity(new Intent(this, HandleAndStoreUserInformationPoliciesActivity.class));
    }
}
