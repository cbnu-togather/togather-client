package com.project.togather.chat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import android.Manifest;

import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.databinding.ActivityChatDetailBinding;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.ChatAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private RecyclerViewAdapter adapter;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private ChatAPI chatAPI;
    private RetrofitService retrofitService;

    private BottomSheetBehavior moreMenuBottomSheetBehavior;
    private BottomSheetBehavior addMenuBottomSheetBehavior;

    private Dialog askLeaveChatRoom_dialog;
    private static int chatRoomId, partyMember;
    private static String chatRoomTitle;
    private static final int REFRESH_INTERVAL = 500;
    ArrayList<ChatDetailInfoItem> chatDetailInfoItems = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable refreshRunnable;
    private boolean firstLoad = true;
    private Bitmap bitmap;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        chatAPI = retrofitService.getRetrofit().create(ChatAPI.class);

        Intent intent = getIntent();
        partyMember = intent.getIntExtra("chatRoom_member", 0);
        chatRoomId = intent.getIntExtra("chatroom_id", 0);
        chatRoomTitle = intent.getStringExtra("chatRoom_title");


        /** (로그아웃 확인) 다이얼로그 변수 초기화 및 설정 */
        askLeaveChatRoom_dialog = new Dialog(ChatDetailActivity.this);  // Dialog 초기화
        askLeaveChatRoom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askLeaveChatRoom_dialog.setContentView(R.layout.dialog_ask_leave_chat_room); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askLeaveChatRoom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(view ->
                startActivity(new Intent(ChatDetailActivity.this, ChatActivity.class)));

        // Add callback listener
        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(ChatDetailActivity.this, ChatActivity.class));
            }
        });

        adapter = new RecyclerViewAdapter();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                hideKeyboard();
                addMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
            }
        });



        // initiate recyclerview
        binding.chatRoomRecyclerView.setAdapter(adapter);
        binding.chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        startRefreshing();

        adapter.setChatDetailInfoItem(chatDetailInfoItems);

        /** (채팅 입력란) 내용 입력 이벤트 설정 */
        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트 변경 전에 호출되는 메소드
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // 텍스트가 한 글자 이상 있을 경우 send_filled 이미지로 변경
                    binding.sendMessageImageView.setImageResource(R.drawable.send_filled);
                } else {
                    // 텍스트가 없을 경우 기본 이미지로 변경 (예를 들어 send_empty)
                    binding.sendMessageImageView.setImageResource(R.drawable.send_normal);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트가 변경된 후에 호출되는 메소드
            }
        });

        // 채팅방 제목이 15글자 이상일 때 ...으로 대체
        String headText = chatRoomTitle;
        if (headText != null && headText.length() > 14) {
            binding.headTextView.setText(headText.substring(0, 14) + "...");
        } else if (headText != null) {
            binding.headTextView.setText(headText);
        }
        binding.currentPartyMemberNumTextView.setText(String.valueOf(partyMember));


        // 채팅 입력 후 (전송) 버튼 클릭 시
        binding.sendMessageImageView.setOnClickListener(view -> sendMessage());

        // 채팅 내용 입력 후 엔터 입력 시 채팅 전송
        binding.messageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        // 텍스트가 비어있지 않은지 확인
                        if (!binding.messageEditText.getText().toString().trim().isEmpty()) {
                            sendMessage();
                            return true;  // 이벤트 소비하여 개행 방지
                        }
                    }
                    return true;  // 이벤트 소비하여 개행 방지
                }
                return false;  // 엔터키가 아닌 다른 키 입력은 기본 동작 수행
            }
        });

        moreMenuBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.chatRoomMoreMenuBottomSheet_layout));

        addMenuBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.chatRoomAddMenuBottomSheet_layout));

        moreMenuBottomSheetBehavior.setDraggable(false);
        addMenuBottomSheetBehavior.setDraggable(false);

        moreMenuBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        addMenuBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
                binding.backgroundDimmer.setVisibility(View.VISIBLE);
                binding.backgroundDimmer.setAlpha(slideOffset);
            }
        });

        // 더 보기 -> (채팅방 나가기) 아이콘, 버튼 클릭 이벤트 설정
        findViewById(R.id.leaveChatRoom_imageView).setOnClickListener(view -> showDialog_askLeaveChatRoom_dialog());
        findViewById(R.id.leaveChatRoom_button).setOnClickListener(view -> showDialog_askLeaveChatRoom_dialog());

        // 더 보기 -> (취소) 버튼 클릭 이벤트 설정
        findViewById(R.id.cancel_button).setOnClickListener(view -> moreMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        // 플러스 버튼 -> (갤러리) 클릭 이벤트 설정
        findViewById(R.id.gallery_relativeLayout).setOnClickListener(view -> {
            Intent intentGallery = new Intent(Intent.ACTION_PICK);
            intentGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intentGallery, REQUEST_GALLERY);
        });

        // 플러스 버튼 -> (카메라) 클릭 이벤트 설정
        findViewById(R.id.camera_relativeLayout).setOnClickListener(view -> {
            int permissionCheck = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.CAMERA);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                // 권한 없음
                ActivityCompat.requestPermissions(ChatDetailActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
            } else {
                //권한 있음
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentCamera, REQUEST_CAMERA);
            }
        });

        // 플러스 버튼 -> 위치 클릭 이벤트 설정
//        findViewById(R.id.gallery_relativeLayout).setOnClickListener(view -> ));

        // more_imageView 클릭 이벤트 처리
        binding.moreMenuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                addMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                moreMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        binding.addMenuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // 어두운 배경 클릭 이벤트 설정
        binding.backgroundDimmer.setOnClickListener(view -> {
            if (moreMenuBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                moreMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (addMenuBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                addMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        binding.activityHeaderRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<ChatDetailInfoItem> items = new ArrayList<>();

        public interface OnItemClickListener {
            void onItemClick(int pos);
        }

        private OnItemClickListener onItemClickListener = null;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }


        public interface OnLongItemClickListener {
            void onLongItemClick(int pos);
        }

        private OnLongItemClickListener onLongItemClickListener = null;

        public void setOnLongItemClickListener(OnLongItemClickListener listener) {
            this.onLongItemClickListener = listener;
        }


        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_chat_detail, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(items.get(position));
        }

        public void setChatDetailInfoItem(ArrayList<ChatDetailInfoItem> list) {
            this.items = list;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public ArrayList<ChatDetailInfoItem> getChatDetailInfoItems() {
            return items;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout root_relativeLayout;
            RelativeLayout myMessage_relativeLayout;
            RelativeLayout otherUserMessage_relativeLayout;

            ImageView myImage_imageView;
            ImageView otherUserProfileImage_roundedImageView;
            ImageView otherUserImage_imageView;

            TextView myTimestamp_textView;
            TextView myMessage_textView;
            TextView otherUsername_textView;
            TextView otherUserMessage_textView;
            TextView otherUserTimestamp_textView;
            TextView chatRoom_textView;
            TextView partyMember_textView;

            CardView myImage_cardView;
            CardView otherUserImage_cardView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (onItemClickListener != null) {
                                onItemClickListener.onItemClick(position);
                            }
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (onLongItemClickListener != null) {
                                onLongItemClickListener.onLongItemClick(position);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                root_relativeLayout = itemView.findViewById(R.id.root_relativeLayout);
                myMessage_relativeLayout = itemView.findViewById(R.id.myMessage_relativeLayout);
                otherUserMessage_relativeLayout = itemView.findViewById(R.id.otherUserMessage_relativeLayout);

                myImage_imageView = itemView.findViewById(R.id.myImage_imageView);
                otherUserProfileImage_roundedImageView = itemView.findViewById(R.id.otherUserProfileImage_roundedImageView);
                otherUserImage_imageView = itemView.findViewById(R.id.otherUserImage_imageView);

                myTimestamp_textView = itemView.findViewById(R.id.myTimestamp_textView);
                myMessage_textView = itemView.findViewById(R.id.myMessage_textView);
                otherUsername_textView = itemView.findViewById(R.id.otherUsername_textView);
                otherUserMessage_textView = itemView.findViewById(R.id.otherUserMessage_textView);
                otherUserTimestamp_textView = itemView.findViewById(R.id.otherUserTimestamp_textView);

                myImage_cardView = itemView.findViewById(R.id.myImage_cardView);
                otherUserImage_cardView = itemView.findViewById(R.id.otherUserImage_cardView);

                chatRoom_textView = itemView.findViewById(R.id.head_textView);
                partyMember_textView = itemView.findViewById(R.id.currentPartyMemberNum_textView);
            }

            void onBind(ChatDetailInfoItem item) {
                if (item.isMyMessage() && item.isContinuousMessage()) {
                    root_relativeLayout.setPadding(root_relativeLayout.getPaddingLeft(), 15, root_relativeLayout.getPaddingRight(), 15);
                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    int marginTopDp = -3; // -3dp
                    int marginTopPx = (int) (marginTopDp * density); // dp를 px로 변환

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                    params.topMargin = marginTopPx;
                    itemView.setLayoutParams(params);

                } else if (item.isContinuousMessage()) {
                    root_relativeLayout.setPadding(root_relativeLayout.getPaddingLeft(), 15, root_relativeLayout.getPaddingRight(), 15);
                    otherUserProfileImage_roundedImageView.setVisibility(View.INVISIBLE);
                    otherUsername_textView.setVisibility(View.GONE);

                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    int marginTopDp = -7; // -7dp
                    int marginTopPx = (int) (marginTopDp * density); // dp를 px로 변환

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                    params.topMargin = marginTopPx;
                    itemView.setLayoutParams(params);
                }


                myMessage_relativeLayout.setVisibility(item.isMyMessage() ? View.VISIBLE : View.GONE);
                otherUserMessage_relativeLayout.setVisibility(item.isMyMessage() ? View.GONE : View.VISIBLE);

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.KOREA);
                SimpleDateFormat outputFormat = new SimpleDateFormat("a h:mm", Locale.KOREA); // "a"는 AM/PM을 나타내고, "h:mm"은 시간을 나타냅니다.

                String formattedDate = "";
                try {
                    Date date = inputFormat.parse(item.getCreatedAt());
                    formattedDate = outputFormat.format(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }



                if (item.isMyMessage()) {

                    myTimestamp_textView.setText("" + item.getCreatedAt());
//                    if (!item.get.equals("")) {
//                        Glide.with(itemView)
//                                .load(item.getImageUrl()) // 이미지 URL 가져오기
//                                .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
//                                .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
//                                .into(myImage_imageView); // ImageView에 이미지 설정
//                        myImage_cardView.setVisibility(View.VISIBLE);
//                        myMessage_textView.setVisibility(View.GONE);
//                    }
                    myMessage_textView.setText(item.getContent());
                    myTimestamp_textView.setText(formattedDate);

                    return;
                }

                if (item.getUserProfileImgUrl() != null && item.getUserProfileImgUrl().equals("")) {
                    otherUserProfileImage_roundedImageView.setImageResource(R.drawable.user_default_profile);
                } else {
                    Glide.with(itemView)
                            .load(item.getUserProfileImgUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                            .into(otherUserProfileImage_roundedImageView); // ImageView에 이미지 설정
                }

//                if (!item.getImageUrl().equals("")) {
//                    Glide.with(itemView)
//                            .load(item.getImageUrl()) // 이미지 URL 가져오기
//                            .placeholder(R.drawable.post_thumbnail_background_logo) // 로딩 중에 표시할 이미지
//                            .error(R.drawable.post_thumbnail_background_logo) // 에러 발생 시 표시할 이미지
//                            .into(otherUserImage_imageView); // ImageView에 이미지 설정
//                    otherUserImage_imageView.setVisibility(View.VISIBLE);
//                    otherUserMessage_textView.setVisibility(View.GONE);
//                }

                otherUsername_textView.setText(item.getUserName());
                otherUserMessage_textView.setText(item.getContent());
                otherUserTimestamp_textView.setText(formattedDate);
            }
        }
    }

    private void sendMessage() {
        String messageText = binding.messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
//            long timestamp = System.currentTimeMillis();  // Get current timestamp
//            ChatDetailInfoItem newItem = new ChatDetailInfoItem(
//                    "",  // userProfileImageUrl, assuming no image for simplicity
//                    "You",  // username
//                    messageText,  // message
//                    "",  // ImageUrl, assuming no image for simplicity
//                    new SimpleDateFormat("a h:mm", Locale.KOREA).format(new Date(timestamp)),  // current timestamp
//                    true,  // isMyMessage
//                    true  // isContinuousMessage, assuming new message is not part of a continuous block
//            );
//
//            adapter.getChatDetailInfoItems().add(newItem);  // Add new message item to the list
            adapter.notifyDataSetChanged();  // Notify adapter to refresh view
            if (adapter.getItemCount() > 0) {
                binding.chatRoomRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);  // Scroll to the new message
            }
            binding.messageEditText.setText("");  // Clear the input field

            sendMessageToServer(messageText);
        }
    }

    private void sendMessageToServer(String messageText) {
        Call<ResponseBody> call = chatAPI.sendMessage(chatRoomId, new ChatMessageRequest(messageText));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ChatDetailActivity.this);
            }
        });
    }

    /**
     * (askUnsubscribe_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askLeaveChatRoom_dialog() {
        askLeaveChatRoom_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askLeaveChatRoom_dialog.getWindow().setDimAmount(0f);

        // (아니오) 버튼
        Button noBtn = askLeaveChatRoom_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askLeaveChatRoom_dialog.dismiss());

        // (확인) 버튼
        askLeaveChatRoom_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askLeaveChatRoom_dialog.dismiss(); // 다이얼로그 닫기
            Call<ResponseBody> call = chatAPI.leaveChatRoom(chatRoomId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new ToastWarning("요청이 수락되었어요", ChatDetailActivity.this);
                        startActivity(new Intent(ChatDetailActivity.this, ChatActivity.class));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    new ToastWarning(getResources().getString(R.string.toast_server_error), ChatDetailActivity.this);
                }
            });

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
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            addMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            long timestamp = System.currentTimeMillis();  // Get current timestamp
            ChatDetailInfoItem newItem = new ChatDetailInfoItem(
                    "",  // userProfileImageUrl, assuming no image for simplicity
                    "2번",  // username
                    binding.messageEditText.getText().toString(),  // message
                    imageUri.toString(),  // ImageUrl, assuming no image for simplicity
                    new SimpleDateFormat("a h:mm", Locale.KOREA).format(new Date(timestamp)),  // current timestamp
                    true,  // isMyMessage
                    true  // isContinuousMessage, assuming new message is not part of a continuous block
            );

            adapter.getChatDetailInfoItems().add(newItem);  // Add new message item to the list
            adapter.notifyDataSetChanged();  // Notify adapter to refresh view
            binding.chatRoomRecyclerView.scrollToPosition(adapter.getItemCount() - 1);  // Scroll to the new message
            binding.messageEditText.setText("");  // Clear the input field
        } catch (Exception e) {
            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRefreshing() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshChatDetails();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        handler.post(refreshRunnable);
    }

    private void stopRefreshing() {
        handler.removeCallbacks(refreshRunnable);
    }

    private void refreshChatDetails() {
        Call<List<ChatDetailInfoItem>> call = chatAPI.getChatRoomDetails(chatRoomId);
        call.enqueue(new Callback<List<ChatDetailInfoItem>>() {
            @Override
            public void onResponse(Call<List<ChatDetailInfoItem>> call, Response<List<ChatDetailInfoItem>> response) {
                if (response.isSuccessful()) {
                    chatDetailInfoItems.clear();
                    chatDetailInfoItems.addAll(response.body());
                    adapter.setChatDetailInfoItem(chatDetailInfoItems);
                    adapter.notifyDataSetChanged();
                }

                if (firstLoad) {
                    // 채팅방에 처음 들어왔을 때 가장 최근 채팅으로 스크롤
                    binding.chatRoomRecyclerView.post(() -> binding.chatRoomRecyclerView.scrollToPosition(adapter.getItemCount() - 1));
                    firstLoad = false;  // 플래그를 false로 설정하여 이후에는 스크롤 이동하지 않음
                }
            }

            @Override
            public void onFailure(Call<List<ChatDetailInfoItem>> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ChatDetailActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    // 카메라로부터 사진을 받았을 때의 처리
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    // Bitmap으로 컨버전
                    bitmap = (Bitmap) extras.get("data");

                    addMenuBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    long timestamp = System.currentTimeMillis();  // Get current timestamp
                    ChatDetailInfoItem newItem = new ChatDetailInfoItem(
                            "",  // userProfileImageUrl, assuming no image for simplicity
                            "You",  // username
                            binding.messageEditText.getText().toString(),  // message
                            "https://d2u3dcdbebyaiu.cloudfront.net/uploads/atch_img/558/5adebf4c2aa0441be0b9eecf9d7bec7c_res.jpeg",  // ImageUrl, assuming no image for simplicity
                            new SimpleDateFormat("a h:mm", Locale.KOREA).format(new Date(timestamp)),  // current timestamp
                            true,  // isMyMessage
                            true  // isContinuousMessage, assuming new message is not part of a continuous block
                    );

                    adapter.getChatDetailInfoItems().add(newItem);  // Add new message item to the list
                    adapter.notifyDataSetChanged();  // Notify adapter to refresh view
                    binding.chatRoomRecyclerView.scrollToPosition(adapter.getItemCount() - 1);  // Scroll to the new message
                    binding.messageEditText.setText("");  // Clear the input field
                    break;
                case REQUEST_GALLERY:
                    // 갤러리에서 이미지를 선택했을 때의 처리
                    Uri selectedImageUri = data.getData();
                    updateImage(selectedImageUri);
                    break;
            }
        }
    }
    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(ChatDetailActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ChatDetailActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefreshing();
    }

    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
    }
}