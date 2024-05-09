package com.project.togather.editPost.community;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.project.togather.R;
import com.project.togather.databinding.ActivityEditCommunityPostCommentBinding;
import com.project.togather.toast.ToastSuccess;

import java.io.InputStream;

public class EditCommunityPostCommentActivity extends AppCompatActivity {

    private ActivityEditCommunityPostCommentBinding binding;

    private static Dialog askCancelWriteComment_dialog;

    private static final int REQUEST_GALLERY = 2;

    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCommunityPostCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedImageUri = Uri.parse("");

        /** (댓글 수정 취소 확인) 다이얼로그 변수 초기화 및 설정 */
        askCancelWriteComment_dialog = new Dialog(EditCommunityPostCommentActivity.this);  // Dialog 초기화
        askCancelWriteComment_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askCancelWriteComment_dialog.setContentView(R.layout.dialog_ask_cancel_write_comment); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askCancelWriteComment_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // (뒤로가기) 클릭 시 현재 액티비티 종료
        binding.backImageButton.setOnClickListener(view -> showDialog_askDeletePost_dialog());

        /** (완료) 버튼 클릭 이벤트 설정 */
        binding.editCommentButton.setOnClickListener(view -> {
            hideKeyboard();
            new ToastSuccess("댓글을 수정했어요", EditCommunityPostCommentActivity.this);
            finish();
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
                                                                       if (binding.contentEditText.getText().toString().length() > 0) {
                                                                           binding.editCommentButton.setTextColor(getResources().getColor(R.color.text_color));
                                                                           binding.editCommentButton.setEnabled(true);
                                                                           return;
                                                                       }

                                                                       binding.editCommentButton.setTextColor(getResources().getColor(R.color.gray_text));
                                                                       binding.editCommentButton.setEnabled(false);
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

        // 게시글 섬네일 이미지 우측 상단의 X 버튼 클릭 이벤트 설정
        binding.deleteImageCardView.setOnClickListener(view ->

        {
            // 이미지 초기화
            binding.postThumbnailImageView.setImageResource(0);
            binding.postImageRelativeLayout.setVisibility(View.INVISIBLE);
            selectedImageUri = Uri.parse("");
        });

        // (하드 코딩) : 서버에서 값을 받아와야 함
        binding.contentEditText.setText("이미 작성되어 있던 댓글 내용");
        Glide.with(binding.postThumbnailImageView)
                .load("https://cdn.imweb.me/upload/S20210809c06cc49e8b65a/21eaaf7839ec5.jpg") // 이미지 URL 가져오기
                .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
        binding.postImageRelativeLayout.setVisibility(View.VISIBLE);
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
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
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
                    Uri selectedImageUri = data.getData();
                    updateImage(selectedImageUri);
                    break;
            }
        }
    }

    /**
     * (askCancelWriteComment_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askDeletePost_dialog() {
        askCancelWriteComment_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askCancelWriteComment_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askCancelWriteComment_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askCancelWriteComment_dialog.dismiss());

        // (삭제) 버튼
        askCancelWriteComment_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askCancelWriteComment_dialog.dismiss(); // 다이얼로그 닫기
            finish();
        });
    }
}