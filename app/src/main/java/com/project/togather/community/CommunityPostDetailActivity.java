package com.project.togather.community;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.databinding.ActivityCommunityPostDetailBinding;
import com.project.togather.editPost.community.EditCommunityPostActivity;
import com.project.togather.editPost.community.EditCommunityPostCommentActivity;
import com.project.togather.editPost.recruitment.EditRecruitmentPostActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityPostDetailActivity extends AppCompatActivity {

    private static ActivityCommunityPostDetailBinding binding;

    private static RecyclerViewAdapter adapter;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RetrofitService retrofitService;

    private BottomSheetBehavior selectPostManagementBottomSheetBehavior;
    private static BottomSheetBehavior selectCommentManagementBottomSheetBehavior;

    private static final int REQUEST_GALLERY = 2;

    private Dialog askDeletePost_dialog;
    private static Dialog askDeleteComment_dialog;

    Uri selectedImageUri;

    private boolean isWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        selectedImageUri = Uri.parse("");

        /** (게시글 삭제 확인) 다이얼로그 변수 초기화 및 설정 */
        askDeletePost_dialog = new Dialog(CommunityPostDetailActivity.this);  // Dialog 초기화
        askDeletePost_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askDeletePost_dialog.setContentView(R.layout.dialog_ask_delete_post); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askDeletePost_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /**(댓글 삭제 확인) 다이얼로그 변수 초기화 및 설정 */
        askDeleteComment_dialog = new Dialog(CommunityPostDetailActivity.this);  // Dialog 초기화
        askDeleteComment_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askDeleteComment_dialog.setContentView(R.layout.dialog_ask_delete_comment); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askDeleteComment_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        adapter = new RecyclerViewAdapter();

        ArrayList<CommentInfoItem> commentInfoItems = new ArrayList<>();

        // initiate recyclerview
        binding.commentRecyclerView.setAdapter(adapter);
        binding.commentRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        commentInfoItems.add(new CommentInfoItem("http://image.dongascience.com/Photo/2020/03/5bddba7b6574b95d37b6079c199d7101.jpg", "우리집멍뭉이는세계최강", "하이(1)", "", 10000, "other"));
        commentInfoItems.add(new CommentInfoItem("https://m.stylehorn.co.kr/web/product/big/201903/ab7175bb3de5cf3b713d0b74562e26ba.jpg", "쿠크다스", "안녕하세용!", "", 5000, "other"));
        commentInfoItems.add(new CommentInfoItem("http://image.dongascience.com/Photo/2020/03/5bddba7b6574b95d37b6079c199d7101.jpg", "우리집멍뭉이는세계최강", "하이요~", "", 1000, "other"));
        commentInfoItems.add(new CommentInfoItem("", "홍길동", "멋있어요", "", 500, "other"));
        commentInfoItems.add(new CommentInfoItem("https://scontent-ssn1-1.xx.fbcdn.net/v/t1.6435-9/87385337_2603792983243744_6226034272168312832_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=5f2048&_nc_ohc=tD2DmCLPzRwQ7kNvgGTN12Q&_nc_ht=scontent-ssn1-1.xx&oh=00_AfB840U4WbepFt_thDMjs_KQZBnE8roFLdvN0sFFr4wvjA&oe=66625729", "김감자", "텍스트", "", 275, "writer"));
        commentInfoItems.add(new CommentInfoItem("https://scontent-ssn1-1.xx.fbcdn.net/v/t1.6435-9/87385337_2603792983243744_6226034272168312832_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=5f2048&_nc_ohc=tD2DmCLPzRwQ7kNvgGTN12Q&_nc_ht=scontent-ssn1-1.xx&oh=00_AfB840U4WbepFt_thDMjs_KQZBnE8roFLdvN0sFFr4wvjA&oe=66625729", "김감자", "", "https://www.visitbusan.net/uploadImgs/files/cntnts/20191227204425032_wufrotr", 275, "writer"));
        commentInfoItems.add(new CommentInfoItem("https://scontent-ssn1-1.xx.fbcdn.net/v/t1.6435-9/87385337_2603792983243744_6226034272168312832_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=5f2048&_nc_ohc=tD2DmCLPzRwQ7kNvgGTN12Q&_nc_ht=scontent-ssn1-1.xx&oh=00_AfB840U4WbepFt_thDMjs_KQZBnE8roFLdvN0sFFr4wvjA&oe=66625729", "김감자", "글과 이미지는 동시에도 가능해요", "https://pbs.twimg.com/media/EZ5OCioVAAAM-dG.jpg", 250, "writer"));
        commentInfoItems.add(new CommentInfoItem("https://m.stylehorn.co.kr/web/product/big/201903/ab7175bb3de5cf3b713d0b74562e26ba.jpg", "쿠크다스", "테스트 댓글", "https://d2u3dcdbebyaiu.cloudfront.net/uploads/atch_img/91/3c349f3bb041bcf7d75c100b928fe2ea_res.jpeg", 100, "other"));
        adapter.setCommentInfoItem(commentInfoItems);

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });

        // 전체 레이아웃 클릭 시 포커스 잃기
        binding.getRoot().setOnClickListener(view -> {
            hideKeyboard();
            binding.commentEditText.clearFocus();
        });

        // 게시글 내용 레이아웃 클릭 시 포커스 잃기
        binding.contentPointRelativeLayout.setOnClickListener(view -> {
            hideKeyboard();
            binding.commentEditText.clearFocus();
        });

        selectPostManagementBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.selectPostManagementBottomSheet_layout));

        selectPostManagementBottomSheetBehavior.setDraggable(false);
        selectPostManagementBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        binding.moreImageButton.setOnClickListener(view ->
                selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        binding.moreImageButton.setVisibility(isWriter ? View.VISIBLE : View.GONE);

        // (게시글 더 보기 버튼) -> 수정
        findViewById(R.id.editPost_button).

                setOnClickListener(view ->

                {
                    if (selectPostManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        startActivity(new Intent(CommunityPostDetailActivity.this, EditCommunityPostActivity.class));
                    }
                });

        // (게시글 더 보기 버튼) -> 삭제
        findViewById(R.id.deletePost_button).

                setOnClickListener(view ->

                {
                    if (selectPostManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        showDialog_askDeletePost_dialog();
                    }
                });

        selectCommentManagementBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.selectCommentManagementBottomSheet_layout));

        selectCommentManagementBottomSheetBehavior.setDraggable(false);
        selectCommentManagementBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
            if (selectPostManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectPostManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (selectCommentManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCommentManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // (댓글 더 보기 버튼) -> 수정
        findViewById(R.id.editComment_button).

                setOnClickListener(view ->

                {
                    if (selectCommentManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCommentManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        startActivity(new Intent(CommunityPostDetailActivity.this, EditCommunityPostCommentActivity.class));
                    }
                });

        // (댓글 더 보기 버튼) -> 삭제
        findViewById(R.id.deleteComment_button).

                setOnClickListener(view ->

                {
                    if (selectCommentManagementBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        selectCommentManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        showDialog_askDeleteComment_dialog();
                    }
                });

        // 댓글 수 설정
        binding.commentNumTextView.setText("" + adapter.getItemCount());

        /** (댓글 입력란) 포커스 스타일 일괄 설정 */
        binding.commentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.commentEditText.getLayoutParams();
                layoutParams.setMarginEnd(hasFocus || binding.commentEditText.getText().toString().length() > 0 ? 0 : 20); // 포커스를 얻으면 marginEnd를 0으로, 잃으면 다시 20으로 설정
                binding.commentEditText.setLayoutParams(layoutParams);
                binding.writeCommentImageView.setVisibility(hasFocus || binding.commentEditText.getText().toString().length() > 0 ? View.VISIBLE : View.GONE); // 포커스를 얻으면 보이게, 잃으면 숨김
            }
        });

        /** (댓글 입력란) 내용 입력 이벤트 설정 */
        binding.commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트 변경 전에 호출되는 메소드
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // 텍스트가 한 글자 이상 있을 경우 send_filled 이미지로 변경
                    binding.writeCommentImageView.setImageResource(R.drawable.send_filled);
                } else {
                    // 텍스트가 없을 경우 기본 이미지로 변경 (예를 들어 send_empty)
                    binding.writeCommentImageView.setImageResource(R.drawable.send_normal);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트가 변경된 후에 호출되는 메소드
            }
        });

        binding.galleryImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, REQUEST_GALLERY);
        });

        // 게시글 섬네일 이미지 우측 상단의 X 버튼 클릭 이벤트 설정
        binding.deleteImageCardView.setOnClickListener(view -> {
            // 이미지 초기화
            binding.postThumbnailImageView.setImageResource(0);
            binding.postThumbnailRelativeLayout.setVisibility(View.GONE);
            selectedImageUri = Uri.parse("");
        });

        // 채팅 입력 후 (전송) 버튼 클릭 시
        binding.writeCommentImageView.setOnClickListener(view -> writeComment());

        // 채팅 내용 입력 후 엔터 입력 시 채팅 전송
        binding.commentEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        // 텍스트가 비어있지 않은지 확인
                        if (!binding.commentEditText.getText().toString().trim().isEmpty()) {
                            writeComment();
                            return true;  // 이벤트 소비하여 개행 방지
                        }
                    }
                    return true;  // 이벤트 소비하여 개행 방지
                }
                return false;  // 엔터키가 아닌 다른 키 입력은 기본 동작 수행
            }
        });

        // 임시 데이터(서버에서 받아와야 하는 값임)
        final int likedCnt[] = {0};
        final boolean isLiked[] = {false};

        // 좋아요가 5개 이상 달린 동네생활 게시글의 경우 상단에 "인기글" 태그가 달림
        binding.hotCategoryCardView.setVisibility(likedCnt[0] >= 5 ? View.VISIBLE : View.GONE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.likedRelativeLayout.getLayoutParams();

        binding.likedRelativeLayout.setOnClickListener(view -> {
            isLiked[0] = !isLiked[0];
            if (isLiked[0])
                likedCnt[0]++;  // 사용자가 좋아요를 누른 경우, 좋아요 수 증가
            else
                likedCnt[0]--;  // 사용자가 좋아요를 취소한 경우, 좋아요 수 감소

            // UI 업데이트
            binding.likedRelativeLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(isLiked[0] ? R.color.theme_color : R.color.rounded_gray_border_color)));
            binding.noLikeRelativeLayout.setVisibility(likedCnt[0] > 0 ? View.GONE : View.VISIBLE);
            binding.yesLikeRelativeLayout.setVisibility(likedCnt[0] > 0 ? View.VISIBLE : View.GONE);
            binding.likedCntTextView.setTextColor(getResources().getColor(isLiked[0] ? R.color.theme_color : R.color.text_color));
            binding.likedCntTextView.setText("" + likedCnt[0]);

            if (likedCnt[0] > 0)
                params.width = 220;
            else
                params.width = 295;

            binding.likedRelativeLayout.setLayoutParams(params);
            binding.likedRelativeLayout.requestLayout();
        });

        binding.likedRelativeLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(isLiked[0] ? R.color.theme_color : R.color.rounded_gray_border_color)));
        binding.noLikeRelativeLayout.setVisibility(likedCnt[0] > 0 ? View.GONE : View.VISIBLE);
        binding.yesLikeRelativeLayout.setVisibility(likedCnt[0] > 0 ? View.VISIBLE : View.GONE);
        binding.likedCntTextView.setTextColor(getResources().getColor(isLiked[0] ? R.color.theme_color : R.color.text_color));
        binding.likedCntTextView.setText("" + likedCnt[0]);

        if (likedCnt[0] > 0)
            params.width = 220;
        else
            params.width = 295;

        binding.likedRelativeLayout.setLayoutParams(params);
        binding.likedRelativeLayout.requestLayout();
    }

    private void writeComment() {
        String commentText = binding.commentEditText.getText().toString().trim();
        if (!commentText.isEmpty()) {
            CommentInfoItem newItem = new CommentInfoItem(
                    "https://mblogthumb-phinf.pstatic.net/MjAyMjAzMjlfMSAg/MDAxNjQ4NDgwNzgwMzkw.yDLPqC9ouJxYoJSgicANH0CPNvFdcixexP7hZaPlCl4g.n7yZDyGC06_gRTwEnAKIhj5bM04laVpNuKRz29dP83wg.JPEG.38qudehd/IMG_8635.JPG?type=w800",  // userProfileImageUrl, assuming no image for simplicity
                    "김감자",  // username
                    commentText,  // comment
                    selectedImageUri.toString(),  // ImageUrl, assuming no image for simplicity
                    1, // Elapsed time
                    "writer" // 작성자 본인의 경우 writer임
            );

            adapter.getCommentInfoItems().add(newItem);  // Add new message item to the list
            adapter.notifyDataSetChanged();  // Notify adapter to refresh view

            int commentNum = adapter.getItemCount();
            binding.activityBodyNestedScrollView.smoothScrollTo(0, 1000 * commentNum + 10000);

            binding.commentEditText.setText("");  // Clear the input field
            selectedImageUri = Uri.parse("");
            binding.postThumbnailRelativeLayout.setVisibility(View.GONE);
            binding.commentNumTextView.setText("" + adapter.getItemCount());
            hideKeyboard();
            binding.commentEditText.clearFocus();
        }
    }


    /**
     * (askDeletePost_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askDeletePost_dialog() {
        askDeletePost_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askDeletePost_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askDeletePost_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askDeletePost_dialog.dismiss());

        // (삭제) 버튼
        askDeletePost_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askDeletePost_dialog.dismiss(); // 다이얼로그 닫기
            startActivity(new Intent(CommunityPostDetailActivity.this, CommunityActivity.class));
        });
    }

    /**
     * (askDeleteComment_dialog) 다이얼로그를 디자인하는 함수
     */
    public static void showDialog_askDeleteComment_dialog() {
        askDeleteComment_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askDeleteComment_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askDeleteComment_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askDeleteComment_dialog.dismiss());

        // (삭제) 버튼
        askDeleteComment_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askDeleteComment_dialog.dismiss(); // 다이얼로그 닫기
            if (adapter != null) {
                adapter.removeItem(adapter.currentSelectedPosition); // 선택된 아이템 삭제
            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<CommentInfoItem> items = new ArrayList<>();
        private int currentSelectedPosition = -1; // 현재 선택된 아이템의 위치 초기화

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view_community_comment, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(items.get(position));
        }

        public void setCommentInfoItem(ArrayList<CommentInfoItem> list) {
            this.items = list;
            notifyDataSetChanged();
        }

        public void removeItem(int position) {
            if (position >= 0 && position < items.size()) {
                items.remove(position);
                binding.commentNumTextView.setText("" + adapter.getItemCount());
                notifyItemRemoved(position);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public ArrayList<CommentInfoItem> getCommentInfoItems() {
            return items;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView userProfileImage_roundedImageView;
            ImageView imageComment_imageView;
            ImageView moreMenu_imageView;

            RelativeLayout imageComment_relativeLayout;

            TextView username_textView;
            TextView elapsedTime_textView;
            TextView comment_textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                userProfileImage_roundedImageView = itemView.findViewById(R.id.userProfileImage_roundedImageView);
                imageComment_imageView = itemView.findViewById(R.id.imageComment_imageView);
                moreMenu_imageView = itemView.findViewById(R.id.moreMenu_imageView);

                imageComment_relativeLayout = itemView.findViewById(R.id.imageComment_relativeLayout);

                username_textView = itemView.findViewById(R.id.username_textView);
                elapsedTime_textView = itemView.findViewById(R.id.elapsedTime_textView);
                comment_textView = itemView.findViewById(R.id.comment_textView);

                moreMenu_imageView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        currentSelectedPosition = position; // 현재 선택된 아이템 위치 저장
                        CommentInfoItem selectedItem = items.get(currentSelectedPosition);
                        String whoValue = selectedItem.getWho();
                        findViewById(R.id.editComment_button).setVisibility(whoValue.equals("writer") ? View.VISIBLE : View.GONE);
                        selectCommentManagementBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                });
            }

            void onBind(CommentInfoItem item) {
                if (item.getUserProfileImageUrl().equals("")) {
                    userProfileImage_roundedImageView.setImageResource(R.drawable.user_default_profile);
                } else {
                    Glide.with(itemView)
                            .load(item.getUserProfileImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                            .into(userProfileImage_roundedImageView); // ImageView에 이미지 설정
                }

                username_textView.setText(item.getUsername());

                long elapsedTime = item.getElapsedTime();
                String elapsedTime_str;
                if (elapsedTime < 60) {
                    elapsedTime_str = elapsedTime + "초 전";
                } else if (elapsedTime < 3600) {
                    elapsedTime_str = elapsedTime / 60 + "분 전";
                } else if (elapsedTime < 86400) {
                    elapsedTime_str = elapsedTime / 3600 + "시간 전";
                } else if (elapsedTime < 86400 * 365) {
                    elapsedTime_str = elapsedTime / 86400 + "일 전";
                } else {
                    elapsedTime_str = elapsedTime / 86400 * 365 + "일 전";
                }
                elapsedTime_textView.setText(elapsedTime_str);

                comment_textView.setText(item.getComment());
                comment_textView.setVisibility(item.getComment().equals("") ? View.GONE : View.VISIBLE);

                if (item.getImageUrl().equals("")) {
                    imageComment_relativeLayout.setVisibility(View.GONE);
                } else {
                    Glide.with(itemView)
                            .load(item.getImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                            .into(imageComment_imageView); // ImageView에 이미지 설정
                }

                if (!isWriter && item.getWho().equals("other")) {
                    moreMenu_imageView.setVisibility(View.GONE);
                }
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY:
                    // 갤러리에서 이미지를 선택했을 때의 처리
                    selectedImageUri = data.getData();
                    updateImage(selectedImageUri);
                    break;
            }
        }
    }

    private void updateImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            Glide.with(binding.commentImageImageView)
                    .load(imageUri) // 이미지 URL 가져오기
                    .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                    .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                    .into(binding.commentImageImageView); // ImageView에 이미지 설정
            binding.postThumbnailRelativeLayout.setVisibility(View.VISIBLE);
        } catch (
                Exception e) {
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(CommunityPostDetailActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), CommunityPostDetailActivity.this);
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