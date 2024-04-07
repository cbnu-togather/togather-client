package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.project.togather.databinding.ActivityLoginBinding;
import com.project.togather.toast.ToastWarning;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    // 일일 문자 인증 가능 횟수
    private int limitNum = 5;

    // 타이머 변수
    private CountDownTimer countDownTimer;

    // 타이머 시작 메서드
    private void startTimer() {
        // 기존 타이머가 있을 경우 취소
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // 5분(300000밀리초) 타이머
        countDownTimer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 남은 시간을 분과 초로 변환하여 텍스트 설정
                String timeText = String.format(Locale.getDefault(), "%02d분 %02d초",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                binding.receiveAuthCodeButton.setText("인증문자 받기" + " (" + timeText + ")");
            }

            @Override
            public void onFinish() {
                // 타이머가 종료되면 버튼 텍스트를 초기 상태로 복원
                binding.receiveAuthCodeButton.setText("인증문자 받기");
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });

        /** (휴대폰 번호 입력란) 포커스 시 */
        binding.phoneNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.phoneNumberEditText.setBackground(getResources().getDrawable(hasFocus ? R.drawable.black_border : R.drawable.light_gray_border));
            }
        });

        /** (휴대폰 번호 입력란) 내용 입력 시 */
        binding.phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            boolean isFormatting; // 현재 포맷팅이 진행 중인지 여부

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
                // 입력이 완료된 후 호출됩니다.
                if (isFormatting) {
                    return; // 포맷팅 중에는 무시
                }

                // 입력된 문자열에서 숫자만 추출하여 전화번호 문자열 생성
                String digitsOnly = editable.toString().replaceAll("\\D", "");

                StringBuilder formattedPhoneNum = new StringBuilder();
                int totalDigits = digitsOnly.length();

                // 입력된 전화번호를 3자리마다 공백을 추가하여 형식을 변환합니다.
                for (int i = 0; i < totalDigits; i++) {
                    formattedPhoneNum.append(digitsOnly.charAt(i));
                    if ((i == 2 || i == 6) && i != totalDigits - 1)
                        formattedPhoneNum.append(" ");
                }

                // 현재 포맷팅 중임을 표시하여 재귀적 호출을 방지
                isFormatting = true;
                binding.phoneNumberEditText.setText(formattedPhoneNum.toString());
                binding.phoneNumberEditText.setSelection(formattedPhoneNum.length());
                isFormatting = false;

                // (인증문자 받기) 버튼 활성화/비활성화 상태 제어
                binding.receiveAuthCodeButton.setEnabled(totalDigits >= 8);
                binding.receiveAuthCodeButton.setTextColor(getResources().getColor(totalDigits >= 8 ? R.color.text_color : R.color.disabled_widget_text_light_gray_color));
            }
        });

        /** (인증문자 받기) 버튼 클릭 시 */
        binding.receiveAuthCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.confirmAuthCodeRelativeLayout.setVisibility(View.VISIBLE);

                if (limitNum == -1) {
                    new ToastWarning("일일 문자 인증 가능 건수 초과", LoginActivity.this);
                    return;
                }

                // 5분 타이머 시작
                startTimer();
                new ToastWarning(getResources().getString(R.string.toast_can_auth_code_confirm_daily_limit_warning) + " " + limitNum-- + "건 남음", LoginActivity.this);
            }
        });

        binding.authCodeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.authCodeEditText.setBackground(getResources().getDrawable(R.drawable.black_border));
                    return;
                }

                binding.authCodeEditText.setBackground(getResources().getDrawable(R.drawable.light_gray_border));
            }
        });

        /** (인증문자 입력란) 내용 입력 시 */
        binding.authCodeEditText.addTextChangedListener(new TextWatcher() {
            boolean isFormatting; // 현재 포맷팅이 진행 중인지 여부

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
                String authCodeText = editable.toString();
                int totalDigits = authCodeText.length();

                binding.authCodeWarningTextView.setText(getResources().getString(R.string.auth_code_warning));
                binding.authCodeWarningTextView.setTextColor(getResources().getColor(R.color.gray));

                // (인증문자 받기) 버튼 활성화/비활성화 상태 제어
                binding.confirmAuthCodeButton.setEnabled(totalDigits >= 1);
                binding.confirmAuthCodeButton.setTextColor(getResources().getColor(totalDigits >= 1 ? R.color.white : R.color.disabled_widget_text_deep_gray_color));
                binding.confirmAuthCodeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(totalDigits >= 1 ? R.color.theme_color : R.color.disabled_widget_background_light_gray_color)));
            }
        });

        /** (인증문자 확인) 버튼 클릭 시 */
        binding.confirmAuthCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usersAuthCodeText = binding.authCodeEditText.getText().toString();
                String systemAuthCodeText = "909409";

                if (usersAuthCodeText.equals(systemAuthCodeText)) {
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    intent.putExtra("phoneNumber", binding.phoneNumberEditText.getText().toString());
                    startActivity(intent);
                    return;
                }

                binding.authCodeEditText.setBackground(getResources().getDrawable(R.drawable.red_border));
                binding.authCodeWarningTextView.setText(getResources().getString(R.string.fail_auth_code));
                binding.authCodeWarningTextView.setTextColor(getResources().getColor(R.color.red));
            }
        });
    }
}