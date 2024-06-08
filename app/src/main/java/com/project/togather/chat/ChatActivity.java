package com.project.togather.chat;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.MainActivity;
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.R;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.createPost.recruitment.SelectMeetingSpotActivity;
import com.project.togather.notification.NotificationActivity;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.home.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.project.togather.databinding.ActivityChatBinding;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.ChatAPI;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private RecyclerViewAdapter adapter;
    private UserAPI userAPI;
    private ChatAPI chatAPI;
    private TokenManager tokenManager;
    private RetrofitService retrofitService;
    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
    ArrayList<ChatInfoItem> chatInfoItems = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 500;
    private BottomSheetBehavior selectCreatePostTypeBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);
        chatAPI = retrofitService.getRetrofit().create(ChatAPI.class);


        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();  // 현재 액티비티와 같은 작업에 있는 모든 액티비티를 종료
            }
        });

        adapter = new RecyclerViewAdapter();



        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
                intent.putExtra("chatroom_id",chatInfoItems.get(pos).getId());
                intent.putExtra("chatRoom_member", chatInfoItems.get(pos).getParticipantCount());
                intent.putExtra("chatRoom_title", chatInfoItems.get(pos).getChatRoomTitle());
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
                intent.putExtra("chatroom_id",chatInfoItems.get(pos).getId());
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.chatsRecyclerView.setAdapter(adapter);
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        startRefreshing();



        adapter.setChatInfoList(chatInfoItems);

        /** "알림" 버튼 클릭 시 */
        binding.notificationImageButton.setOnClickListener(view ->
                startActivity(new Intent(ChatActivity.this, NotificationActivity.class)));

        /** "신규 알림" 버튼 클릭 시 */
        binding.notificationNewImageButton.setOnClickListener(view ->
                startActivity(new Intent(ChatActivity.this, NotificationActivity.class)));

        /** "홈" 레이아웃 클릭 시 */
        binding.homeActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(ChatActivity.this, HomeActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        /** "동네생활" 레이아웃 클릭 시 */
        binding.communityActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(ChatActivity.this, CommunityActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        // 어두운 배경 클릭 이벤트 설정
        binding.backgroundDimmer.setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        selectCreatePostTypeBottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.selectCreatePostTypeBottomSheet_layout));

        selectCreatePostTypeBottomSheetBehavior.setDraggable(false);

        selectCreatePostTypeBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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


        /** "글 쓰기" 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(view -> selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        // 작성할 게시글 유형 선택
        findViewById(R.id.createRecruitmentPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(ChatActivity.this, CreateRecruitmentPostActivity.class));
            }
        });

        findViewById(R.id.createCommunityPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(ChatActivity.this, CreateCommunityPostActivity.class));
            }
        });

        /** "내 정보" 레이아웃 클릭 시 */
        binding.profileActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(ChatActivity.this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<ChatInfoItem> items = new ArrayList<>();

        public interface OnItemClickListener {
            void onItemClick(int pos);
        }

        private RecyclerViewAdapter.OnItemClickListener onItemClickListener = null;

        public void setOnItemClickListener(RecyclerViewAdapter.OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }


        public interface OnLongItemClickListener {
            void onLongItemClick(int pos);
        }

        private RecyclerViewAdapter.OnLongItemClickListener onLongItemClickListener = null;

        public void setOnLongItemClickListener(RecyclerViewAdapter.OnLongItemClickListener listener) {
            this.onLongItemClickListener = listener;
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_chat, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(items.get(position));
        }

        public void setChatInfoList(ArrayList<ChatInfoItem> list) {
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

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout root_relativeLayout;
            RelativeLayout content_relativeLayout;
            RelativeLayout unreadMsgNum_relativeLayout;

            ImageView firstUser_roundedImageView;
            ImageView secondUser_roundedImageView;
            ImageView thirdUser_roundedImageView;
            ImageView post_imageView;

            TextView postTitle_textView;
            TextView lastChatContent_textView;
            TextView chatMemberNum_textView;
            TextView lastChatElapsedTime_textView;
            TextView unreadMsgNum_textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (onItemClickListener != null) {
                                onItemClickListener.onItemClick(position);

                                // 클릭된 배경으로 변경
                                root_relativeLayout.setBackgroundColor(itemView.getResources().getColor(R.color.post_clicked_gray_color));
                                content_relativeLayout.setBackgroundColor(itemView.getResources().getColor(R.color.post_clicked_gray_color));

                                // 500 밀리초(0.5초) 후에 이전 배경색으로 변경
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 이전 배경색으로 변경
                                        root_relativeLayout.setBackground(itemView.getResources().getDrawable(R.drawable.list_item_view_border_bottom_white_background));
                                        content_relativeLayout.setBackground(itemView.getResources().getDrawable(R.drawable.list_item_view_border_bottom_white_background));
                                    }
                                }, 500); // 0.5초 지연
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
                content_relativeLayout = itemView.findViewById(R.id.content_relativeLayout);
                unreadMsgNum_relativeLayout = itemView.findViewById(R.id.unreadMsgNum_relativeLayout);

                firstUser_roundedImageView = itemView.findViewById(R.id.firstUser_roundedImageView);
                secondUser_roundedImageView = itemView.findViewById(R.id.secondUser_roundedImageView);
                thirdUser_roundedImageView = itemView.findViewById(R.id.thirdUser_roundedImageView);
                post_imageView = itemView.findViewById(R.id.post_imageView);

                postTitle_textView = itemView.findViewById(R.id.postTitle_textView);
                lastChatContent_textView = itemView.findViewById(R.id.lastChatContent_textView);
                chatMemberNum_textView = itemView.findViewById(R.id.chatMemberNum_textView);
                lastChatElapsedTime_textView = itemView.findViewById(R.id.lastChatElapsedTime_textView);
                unreadMsgNum_textView = itemView.findViewById(R.id.unreadMsgNum_textView);
            }

            void onBind(ChatInfoItem item) {

                ImageView[] userImageViews = {
                        firstUser_roundedImageView,
                        secondUser_roundedImageView,
                        thirdUser_roundedImageView
                };

                for (int i=0; i < item.getParticipantCount(); i++) {
                    if (item.getUserProfileImgUrls()[i] != null && item.getUserProfileImgUrls()[i].equals("")) {
                        userImageViews[i].setImageResource(R.drawable.user_default_profile);
                    } else {
                        Glide.with(itemView)
                                .load(item.getUserProfileImgUrls()[i]) // 이미지 URL 가져오기
                                .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                                .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                                .into(userImageViews[i]); // ImageView에 이미지 설정
                    }
                }

//                if (item.getUserProfileImgUrls()[1] != null && item.getUserProfileImgUrls()[1].equals("")) {
//                    secondUser_roundedImageView.setImageResource(R.drawable.user_default_profile);
//                } else {
//                    Glide.with(itemView)
//                            .load(item.getUserProfileImgUrls()[1]) // 이미지 URL 가져오기
//                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
//                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
//                            .into(secondUser_roundedImageView); // ImageView에 이미지 설정
//                }
//
//                if (item.getUserProfileImgUrls()[2] != null && item.getUserProfileImgUrls()[2].equals("")) {
//                    thirdUser_roundedImageView.setImageResource(R.drawable.user_default_profile);
//                } else {
//                    Glide.with(itemView)
//                            .load(item.getUserProfileImgUrls()[2]) // 이미지 URL 가져오기
//                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
//                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
//                            .into(thirdUser_roundedImageView); // ImageView에 이미지 설정
//                }
//
                if (item.getGroupBuyThumbnailUrl() != null && item.getGroupBuyThumbnailUrl().equals("")) {
                    post_imageView.setImageResource(R.drawable.one_person_logo);
                } else {
                    Glide.with(itemView)
                            .load(item.getGroupBuyThumbnailUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                String postTitle = item.getChatRoomTitle();
                if (postTitle.length() >= 20)
                    postTitle = postTitle.substring(0, 20) + "...";

                postTitle_textView.setText(postTitle);

                String lastChat = item.getLastMessage();
                if (lastChat.length() >= 40) lastChat = lastChat.substring(0, 40) + "...";
                if (lastChat != null) {
                    lastChatContent_textView.setText(lastChat);
                } else {
                    lastChatContent_textView.setText("");
                }


                chatMemberNum_textView.setText("" + (item.getParticipantCount()));

                long lastChatElapsedTime = item.getElapsedTime(); // 이 부분은 기존 코드에서 가져왔다고 가정합니다.

                if (lastChatElapsedTime < 0) {
                    lastChatElapsedTime_textView.setText("");
                } else {
                    String elapsedTime_str = getFormattedElapsedTime(lastChatElapsedTime);
                    lastChatElapsedTime_textView.setText(elapsedTime_str);
                }



                int unreadMsgNum = item.getUnreadMessageCount();
                unreadMsgNum_relativeLayout.setVisibility(unreadMsgNum == 0 ? View.INVISIBLE : View.VISIBLE);
                unreadMsgNum_textView.setText("" + item.getUnreadMessageCount());
            }

            private String getFormattedElapsedTime(long elapsedTimeSeconds) {
                // 현재 시간
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTimeInMillis(System.currentTimeMillis());

                // 경과 시간을 더한 시간
                Calendar elapsedTimeCalendar = Calendar.getInstance();
                elapsedTimeCalendar.setTimeInMillis(System.currentTimeMillis());
                elapsedTimeCalendar.add(Calendar.SECOND, (int) -elapsedTimeSeconds); // 시간을 빼서 과거의 시간을 나타냄

                // 날짜 포맷을 정의
                SimpleDateFormat todayFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
                SimpleDateFormat yesterdayFormat = new SimpleDateFormat("'어제' a hh:mm", Locale.KOREA);
                SimpleDateFormat sameYearFormat = new SimpleDateFormat("MM월 dd일", Locale.KOREA);
                SimpleDateFormat differentYearFormat = new SimpleDateFormat("yyyy. MM. dd.", Locale.KOREA);

                // 현재 시간과 경과 시간의 년도 비교
                int currentYear = currentCalendar.get(Calendar.YEAR);
                int elapsedTimeYear = elapsedTimeCalendar.get(Calendar.YEAR);

                // 경과 시간이 현재 시간과 같은 년도인 경우
                if (currentYear == elapsedTimeYear) {
                    // 경과 시간이 오늘인 경우
                    if (isSameDay(currentCalendar, elapsedTimeCalendar)) {
                        return todayFormat.format(new Date(elapsedTimeCalendar.getTimeInMillis()));
                    }
                    // 경과 시간이 어제인 경우
                    else if (isYesterday(currentCalendar, elapsedTimeCalendar)) {
                        return "어제";
                    }
                    // 그 외의 경우 (2일 이상 경과)
                    else {
                        return sameYearFormat.format(new Date(elapsedTimeCalendar.getTimeInMillis()));
                    }
                }
                // 경과 시간이 다른 년도인 경우
                else {
                    return differentYearFormat.format(new Date(elapsedTimeCalendar.getTimeInMillis()));
                }
            }

            // 두 날짜가 같은 날인지 확인하는 메서드
            private boolean isSameDay(Calendar cal1, Calendar cal2) {
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                        cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
            }

            // 두 날짜가 어제인지 확인하는 메서드
            private boolean isYesterday(Calendar cal1, Calendar cal2) {
                cal1.add(Calendar.DAY_OF_MONTH, -1);
                return isSameDay(cal1, cal2);
            }
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

    private void refreshChatDetails() {
        Call<List<ChatInfoItem>> call = chatAPI.getChatRoomList();
        call.enqueue(new Callback<List<ChatInfoItem>>() {
            @Override
            public void onResponse(Call<List<ChatInfoItem>> call, Response<List<ChatInfoItem>> response) {
                if (response.isSuccessful()) {
                    chatInfoItems.clear();
                    List<ChatInfoItem> reversedList = response.body();
                    Collections.reverse(reversedList); // 리스트를 역순으로 변경
                    chatInfoItems.addAll(reversedList);
                    adapter.setChatInfoList(chatInfoItems);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ChatInfoItem>> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ChatActivity.this);
            }
        });
    }

    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(ChatActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), ChatActivity.this);
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