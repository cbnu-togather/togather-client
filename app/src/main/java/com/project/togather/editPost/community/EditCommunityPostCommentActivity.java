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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.community.CommentInfoItem;
import com.project.togather.community.CommentInfoResponse;
import com.project.togather.community.CommunityPostDetailItem;
import com.project.togather.databinding.ActivityEditCommunityPostCommentBinding;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCommunityPostCommentActivity extends AppCompatActivity {

    private ActivityEditCommunityPostCommentBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private CommunityAPI communityAPI;
    private RetrofitService retrofitService;


    private static Dialog askCancelWriteComment_dialog;

    private static final int REQUEST_GALLERY = 2;
    private CommunityPostDetailItem communityPostDetailItem;
    Uri selectedImageUri;
    private static int commentId, postId;
    private ArrayList<CommentInfoItem> commentInfoItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCommunityPostCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        communityAPI = retrofitService.getRetrofit().create(CommunityAPI.class);

        Intent intentEditComment = getIntent();
        commentId = intentEditComment.getIntExtra("comment_id", 0);
        postId = intentEditComment.getIntExtra("post_id", 0);

        Log.d("id", "onCreate: " + commentId + " " + postId);

        selectedImageUri = Uri.parse("");
        getPostDetailInfo(postId);
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
            updateComment(commentId);
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
                    .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                    .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                    .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
            binding.postImageRelativeLayout.setVisibility(View.VISIBLE);
        } catch (
                Exception e) {
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
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
            Log.e("File Conversion", "Error converting Uri to File", e);
        }
        return file;
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
    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(EditCommunityPostCommentActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostCommentActivity.this);
            }
        });
    }

    private void getPostDetailInfo(int postId) {
        Call<ResponseBody> call = communityAPI.getCommunityPostDetail(postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonString = response.body().string();
                        Log.d("response", "response: " + jsonString);
                        Gson gson = new Gson();
                        communityPostDetailItem = gson.fromJson(jsonString, CommunityPostDetailItem.class);

                        CommentInfoResponse[] commentArray = communityPostDetailItem.getComments();
                        ArrayList<CommentInfoResponse> commentList = new ArrayList<>(Arrays.asList(commentArray));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                        Log.d("commentId", "commentId: " + commentId);
                        // 현재 시간 UTC로 생성
                        Date now = new Date();
                        CommentInfoResponse selectedComment = null;
                        for (CommentInfoResponse comment : commentList) {
                            if (comment.getId() == commentId) {
                                selectedComment = comment;
                                break;
                            }
                        }
                        Log.d("sel", "selectedId: " + selectedComment.getId());
                        if (selectedComment != null) {
                            binding.contentEditText.setText(selectedComment.getContent());

                            if (selectedComment.getImg() != null && !selectedComment.getImg().isEmpty()) {
                                selectedImageUri = Uri.parse(selectedComment.getImg());
                                Glide.with(binding.postThumbnailImageView)
                                        .load(selectedImageUri) // 이미지 URL 가져오기
                                        .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                                        .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                                        .into(binding.postThumbnailImageView); // ImageView에 이미지 설정
                                binding.postImageRelativeLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {

            }
        });
    }

    private void updateComment(int commentId) {
        String content = binding.contentEditText.getText().toString();
        if (!selectedImageUri.toString().isEmpty()) {
            File file = uriToFile(selectedImageUri, EditCommunityPostCommentActivity.this);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body =  MultipartBody.Part.createFormData("img", file.getName(), requestFile);

            Call<ResponseBody> call = communityAPI.updateComment(commentId, content, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("수정이 완료되었어요", EditCommunityPostCommentActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostCommentActivity.this);
                }
            });

        } else {
            Call<ResponseBody> call = communityAPI.updateCommentWithoutImg(commentId, content);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastSuccess("수정이 완료되었어요", EditCommunityPostCommentActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    // 서버 코드 및 네트워크 오류 등의 이유로 요청 실패
                    new ToastWarning(getResources().getString(R.string.toast_server_error), EditCommunityPostCommentActivity.this);
                }
            });
        }
    }
    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();

    }
}