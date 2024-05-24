package com.project.togather.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.chat.ChatDetailActivity;
import com.project.togather.databinding.ActivityNotificationBinding;
import com.project.togather.editPost.recruitment.EditRecruitmentPostSelectMeetingSpotActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastSuccess;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RetrofitService retrofitService;

    private RecyclerViewAdapter adapter;

    private Dialog askAcceptJoinParty_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);


        // 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        /** (파티 가입 요청 확인) 다이얼로그 변수 초기화 및 설정 */
        askAcceptJoinParty_dialog = new Dialog(NotificationActivity.this);  // Dialog 초기화
        askAcceptJoinParty_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        askAcceptJoinParty_dialog.setContentView(R.layout.dialog_ask_accept_join_party); // xml 레이아웃 파일과 연결
        // dialog 창의 root 레이아웃을 투명하게 조절 모서리(코너)를 둥글게 보이게 하기 위해
        askAcceptJoinParty_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        adapter = new RecyclerViewAdapter();

        ArrayList<NotificationInfoItem> notificationInfoItems = new ArrayList<>();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                showDialog_askUnsubscribe_dialog();
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                showDialog_askUnsubscribe_dialog();
            }
        });

        // initiate recyclerview
        binding.notificationsRecyclerView.setAdapter(adapter);
        binding.notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        notificationInfoItems.add(new NotificationInfoItem("https://cdn.011st.com/11dims/resize/600x600/quality/75/11src/product/5400941752/B.jpg?481000000", "https://cdn.dominos.co.kr/admin/upload/goods/20240214_8rBc1T61.jpg?RS=350x350&SP=1", "김하늘", "도미노 피자 드실분 구해요", 30000, 3, 2, "저 같이 주문하고 싶어요..!"));
        notificationInfoItems.add(new NotificationInfoItem("https://img1.daumcdn.net/thumb/R1280x0.fjpg/?fname=http://t1.daumcdn.net/brunch/service/user/9mqM/image/6vuarJpov779Xfo2EdNhLhmaPgI.JPG", "", "아무개", "짚신 스시 & 롤 배달 구해요", 70000, 2, 1, "저 같이 주문 가능할까용?"));
        notificationInfoItems.add(new NotificationInfoItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSutGBoBGvVLOofPQ8mNAAKDpgD7NiHKzAyRSAL35gRQA&s", "https://media-cdn.tripadvisor.com/media/photo-s/12/31/92/d9/1519804025288-largejpg.jpg", "크루키", "신전 떡볶이 구해유", 90000, 1, 0, "같이 드시져!😎😎"));

        adapter.setNotificationInfoList(notificationInfoItems);

        /** (뒤로가기 화살표 이미지) 버튼 클릭 시 */
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<NotificationInfoItem> items = new ArrayList<>();
        private int currentSelectedPosition = -1; // 현재 선택된 아이템의 위치 초기화

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(items.get(position));
        }

        public void setNotificationInfoList(ArrayList<NotificationInfoItem> list) {
            this.items = list;
            notifyDataSetChanged();
        }

        public void removeItem(int position) {
            if (position >= 0 && position < items.size()) {
                items.remove(position);
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

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout root_relativeLayout;
            RelativeLayout content_relativeLayout;

            ImageView userProfile_roundedImageView;
            ImageView post_imageView;

            TextView username_textView;
            TextView postTitle_textView;
            TextView category_textView;
            TextView elapsedTime_textView;
            TextView currentPartyMemberNum_textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(position);
                            currentSelectedPosition = position; // 현재 선택된 아이템 위치 저장
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
                });

                itemView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (onLongItemClickListener != null) {
                            onLongItemClickListener.onLongItemClick(position);
                            currentSelectedPosition = position; // 현재 선택된 아이템 위치 저장
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
                            return true;
                        }
                    }
                    return false;
                });

                root_relativeLayout = itemView.findViewById(R.id.root_relativeLayout);
                content_relativeLayout = itemView.findViewById(R.id.content_relativeLayout);

                userProfile_roundedImageView = itemView.findViewById(R.id.userProfile_roundedImageView);
                post_imageView = itemView.findViewById(R.id.post_imageView);

                username_textView = itemView.findViewById(R.id.username_textView);
                postTitle_textView = itemView.findViewById(R.id.postTitle_textView);
                category_textView = itemView.findViewById(R.id.category_textView);
                elapsedTime_textView = itemView.findViewById(R.id.elapsedTime_textView);
                currentPartyMemberNum_textView = itemView.findViewById(R.id.currentPartyMemberNum_textView);
            }

            void onBind(NotificationInfoItem item) {
                if (item.getUserProfileImageUrl().equals("")) {
                    userProfile_roundedImageView.setImageResource(R.drawable.user_default_profile);
                } else {
                    Glide.with(itemView)
                            .load(item.getUserProfileImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.user_default_profile) // 로딩 중에 표시할 이미지
                            .error(R.drawable.user_default_profile) // 에러 발생 시 표시할 이미지
                            .into(userProfile_roundedImageView); // ImageView에 이미지 설정
                }

                if (item.getPostThumbnailImageUrl().equals("")) {
                    post_imageView.setImageResource(R.drawable.post_thumbnail_background_logo);
                } else {
                    Glide.with(itemView)
                            .load(item.getPostThumbnailImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.post_thumbnail_background_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.post_thumbnail_background_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                username_textView.setText(item.getUsername());
                postTitle_textView.setText(item.getTitle());

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

                currentPartyMemberNum_textView.setText("" + item.getCurrentPartyMemberNum() + '/' + item.getMaxPartyMemberNum());
            }
        }
    }

    /**
     * (askAcceptJoinParty_dialog) 다이얼로그를 디자인하는 함수
     */
    public void showDialog_askUnsubscribe_dialog() {
        askAcceptJoinParty_dialog.show(); // 다이얼로그 띄우기
        // 다이얼로그 창이 나타나면서 외부 액티비티가 어두워지는데, 그 정도를 조절함
        askAcceptJoinParty_dialog.getWindow().setDimAmount(0.35f);

        // (아니오) 버튼
        Button noBtn = askAcceptJoinParty_dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(view -> askAcceptJoinParty_dialog.dismiss());

        // (확인) 버튼
        askAcceptJoinParty_dialog.findViewById(R.id.yesBtn).setOnClickListener(view -> {
            askAcceptJoinParty_dialog.dismiss(); // 다이얼로그 닫기
            new ToastSuccess("채팅방에 초대되었어요", NotificationActivity.this);
            if (adapter != null) {
                adapter.removeItem(adapter.currentSelectedPosition); // 선택된 아이템 삭제

                createNotification();
            }
        });
    }

    private void requestNotificationPermission() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (!notificationManager.areNotificationsEnabled()) {
            // Android 13 이상에서 알림 권한 요청
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
    }

    private void createNotification() {
        final String CHANNEL_ID = "channel1";
        final String CHANNEL_NAME = "Channel Name";
        final String CHANNEL_DESCRIPTION = "Channel Description";
        int importance = NotificationManager.IMPORTANCE_HIGH; // 중요도 설정

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O(API 레벨 26) 이상에서는 알림 채널을 생성해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            // 채널을 시스템에 등록
            notificationManager.createNotificationChannel(channel);
        }

        // GroupChatActivity 이동하는 인텐트 생성
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // NotificationCompat.Builder를 사용하여 알림 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)  // 이 부분에서 smallIcon 설정
                .setColor(getResources().getColor(R.color.theme_color))
                .setContentTitle("❤️파티가 생성됐어요❤️") // 알림 제목 설정
                .setContentText("\"도미노 피자 드실분 구해요\" 채팅방에서 지금 바로 이야기 나눠보세요!") // 알림 내용 설정
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 설정 (필수는 아님)
                .setContentIntent(pendingIntent)  // PendingIntent 추가
                .setAutoCancel(true);  // 사용자가 탭할 때 알림 자동 제거

        // 알림 표시 (알림은 신청자에게 가야 함: 예시 화면)
        notificationManager.notify(1, builder.build());
    }

    private void removeNotification() {
        // Notification 제거
        NotificationManagerCompat.from(this).cancel(1);
    }
    // 유저 정보 조회 메서드
    private void getUserInfo() {
        Call<ResponseBody> call = userAPI.getUserInfo();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 403) {
                    startActivity(new Intent(NotificationActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), NotificationActivity.this);
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