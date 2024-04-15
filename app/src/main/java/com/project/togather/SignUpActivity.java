package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.project.togather.databinding.ActivityLoginBinding;
import com.project.togather.databinding.ActivityMainBinding;
import com.project.togather.databinding.ActivitySignUpBinding;
import com.project.togather.home.HomeActivity;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private boolean isAgreeOurPolicies = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        // 로그인 액티비티에서 인증 완료된 전화번호를 phoneNumberEditText 위젯 텍스트로 설정
        binding.phoneNumberEditText.setText(phoneNumber);

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });

        /** (닉네임 입력란) 포커스 시 */
        binding.usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.usernameEditText.setBackground(getResources().getDrawable(hasFocus ? R.drawable.black_border : R.drawable.light_gray_border));
            }
        });

        /** (닉네임 입력란) 내용 입력 시 */
        binding.usernameEditText.addTextChangedListener(new TextWatcher() {
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
                String usernameText = editable.toString();

                binding.signUpButton.setEnabled(!binding.usernameEditText.getText().toString().equals("") && isAgreeOurPolicies);
                binding.signUpButton.setTextColor(getResources().getColor(!binding.usernameEditText.getText().toString().equals("") && isAgreeOurPolicies ? R.color.white : R.color.disabled_widget_text_deep_gray_color));
                binding.signUpButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(!binding.usernameEditText.getText().toString().equals("") && isAgreeOurPolicies ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
            }
        });

        /** (개인정보 수집 및 이용 체크박스 이미지) 클릭 시 */
        binding.agreeOurPoliciesCheckboxRoundedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.agreeOurPoliciesCheckboxRoundedImageView.setImageResource(isAgreeOurPolicies ? R.drawable.check_circle_gray : R.drawable.check_circle_green);
                isAgreeOurPolicies = !isAgreeOurPolicies;

                binding.signUpButton.setEnabled(!binding.usernameEditText.getText().toString().equals("") && isAgreeOurPolicies);
                binding.signUpButton.setTextColor(getResources().getColor(!binding.usernameEditText.getText().toString().equals("") && isAgreeOurPolicies ? R.color.white : R.color.disabled_widget_text_deep_gray_color));
                binding.signUpButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(!binding.usernameEditText.getText().toString().equals("") && isAgreeOurPolicies ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
            }
        });

        /** (개인정보 수집 및 이용 텍스트) 클릭 시 */
        binding.agreeOurPoliciesTextRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, HandleAndStoreUserInformationPoliciesActivity.class);
                startActivity(intent);
            }
        });

        /** (개인정보 수집 및 이용 텍스트 우측 화살표 이미지) 클릭 시 */
        binding.arrowRightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, HandleAndStoreUserInformationPoliciesActivity.class);
                startActivity(intent);
            }
        });

        /** (회원가입) 버튼 클릭 시 */
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = binding.usernameEditText.getText().toString();

                if (username.equals("success")) {
                    new ToastSuccess("회원가입 완료", SignUpActivity.this);
                    Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                    startActivity(intent);
                    return;
                }

                new ToastWarning("서버 에러가 발생했어요", SignUpActivity.this);
            }
        });
    }
}