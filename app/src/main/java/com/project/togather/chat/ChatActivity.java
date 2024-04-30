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
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.R;
import com.project.togather.notification.NotificationActivity;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.home.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.project.togather.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private RecyclerViewAdapter adapter;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();  // 현재 액티비티와 같은 작업에 있는 모든 액티비티를 종료
            }
        });

        adapter = new RecyclerViewAdapter();

        ArrayList<ChatInfoItem> chatInfoItems = new ArrayList<>();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                startActivity(new Intent(ChatActivity.this, ChatDetailActivity.class));
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                startActivity(new Intent(ChatActivity.this, ChatDetailActivity.class));
            }
        });

        // initiate recyclerview
        binding.chatsRecyclerView.setAdapter(adapter);
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        chatInfoItems.add(new ChatInfoItem("", "https://cdn.011st.com/11dims/resize/600x600/quality/75/11src/product/5400941752/B.jpg?481000000", "https://image.newsis.com/2023/07/12/NISI20230712_0001313626_web.jpg?rnd=20230712163021", "https://cdn.dominos.co.kr/admin/upload/goods/20240214_8rBc1T61.jpg?RS=350x350&SP=1", "도미노 피자 드실분 구해요", "도착했습니다! 모여용", 300, 3, 3, 3));
        chatInfoItems.add(new ChatInfoItem("http://image.dongascience.com/Photo/2020/03/5bddba7b6574b95d37b6079c199d7101.jpg", "https://www.sisajournal.com/news/photo/first/200508/img_102658_1.jpg", "", "https://d12zq4w4guyljn.cloudfront.net/750_750_20201122041810_photo1_5831aaf849cf.jpg", "파브리카 배달 구해용", "솔못에서 모일게요", 30000, 1, 2, 3));
        chatInfoItems.add(new ChatInfoItem("", "https://img1.daumcdn.net/thumb/R1280x0.fjpg/?fname=http://t1.daumcdn.net/brunch/service/user/9mqM/image/6vuarJpov779Xfo2EdNhLhmaPgI.JPG", "", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ-1FF9Hpe-_ERtrBHcUDeeckMOeOzm6IWylD_mJJlJEQ&s", "컴포즈 배달 구해요!!!", "맛나게 드셔요~", 100000, 0, 1, 1));
        chatInfoItems.add(new ChatInfoItem("", "", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSutGBoBGvVLOofPQ8mNAAKDpgD7NiHKzAyRSAL35gRQA&s", "", "밥버거 드실분", "넹", 300000, 0, 2, 1));

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

        /** "글 쓰기" 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(view ->
                startActivity(new Intent(ChatActivity.this, CreateCommunityPostActivity.class)));

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
                if (item.getFirstChatUserProfileImageUrl().equals("")) {
                    firstUser_roundedImageView.setImageResource(R.drawable.user_default_profile);
                } else {
                    Glide.with(itemView)
                            .load(item.getFirstChatUserProfileImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                            .into(firstUser_roundedImageView); // ImageView에 이미지 설정
                }

                if (item.getSecondChatUserProfileImageUrl().equals("")) {
                    secondUser_roundedImageView.setImageResource(R.drawable.user_default_profile);
                } else {
                    Glide.with(itemView)
                            .load(item.getSecondChatUserProfileImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                            .into(secondUser_roundedImageView); // ImageView에 이미지 설정
                }

                if (item.getThirdChatUserProfileImageUrl().equals("")) {
                    thirdUser_roundedImageView.setImageResource(R.drawable.user_default_profile);
                } else {
                    Glide.with(itemView)
                            .load(item.getThirdChatUserProfileImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                            .into(thirdUser_roundedImageView); // ImageView에 이미지 설정
                }

                if (item.getPostThumbnailImageUrl().equals("")) {
                    post_imageView.setImageResource(R.drawable.one_person_logo);
                } else {
                    Glide.with(itemView)
                            .load(item.getPostThumbnailImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                String postTitle = item.getTitle();
                if (postTitle.length() >= 20)
                    postTitle = postTitle.substring(0, 20) + "...";

                postTitle_textView.setText(postTitle);

                String lastChat = item.getLastChat();
                if (lastChat.length() >= 40) lastChat = lastChat.substring(0, 40) + "...";
                lastChatContent_textView.setText(lastChat);

                chatMemberNum_textView.setText("" + (item.getCurrentPartyMemberNum() + 1));

                long lastChatElapsedTime = item.getLastChatElapsedTime(); // 이 부분은 기존 코드에서 가져왔다고 가정합니다.
                String elapsedTime_str = getFormattedElapsedTime(lastChatElapsedTime);
                lastChatElapsedTime_textView.setText(elapsedTime_str);


                int unreadMsgNum = item.getUnreadMsgNum();
                unreadMsgNum_relativeLayout.setVisibility(unreadMsgNum == 0 ? View.INVISIBLE : View.VISIBLE);
                unreadMsgNum_textView.setText("" + item.getUnreadMsgNum());
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
                SimpleDateFormat todayFormat = new SimpleDateFormat("a hh:mm", Locale.getDefault());
                SimpleDateFormat yesterdayFormat = new SimpleDateFormat("'어제' a hh:mm", Locale.getDefault());
                SimpleDateFormat sameYearFormat = new SimpleDateFormat("MM월 dd일", Locale.getDefault());
                SimpleDateFormat differentYearFormat = new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());

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
}