package com.project.togather.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.project.togather.CreatePostActivity;
import com.project.togather.R;
import com.project.togather.community.CommunityPostDetailActivity;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.community.CommunityActivity;
import com.project.togather.databinding.ActivityChatBinding;
import com.project.togather.home.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new RecyclerViewAdapter();

        ArrayList<ChatInfoItem> chatInfoItems = new ArrayList<>();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent(ChatActivity.this, CommunityPostDetailActivity.class);
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                Intent intent = new Intent(ChatActivity.this, CommunityPostDetailActivity.class);
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.chatsRecyclerView.setAdapter(adapter);
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        chatInfoItems.add(new ChatInfoItem("", "https://mblogthumb-phinf.pstatic.net/MjAyMjAzMjlfMSAg/MDAxNjQ4NDgwNzgwMzkw.yDLPqC9ouJxYoJSgicANH0CPNvFdcixexP7hZaPlCl4g.n7yZDyGC06_gRTwEnAKIhj5bM04laVpNuKRz29dP83wg.JPEG.38qudehd/IMG_8635.JPG?type=w800", "https://image.newsis.com/2023/07/12/NISI20230712_0001313626_web.jpg?rnd=20230712163021", "https://cdn.dominos.co.kr/admin/upload/goods/20240214_8rBc1T61.jpg?RS=350x350&SP=1", "도미노 피자 드실분 구해요", "도착했습니다! 모여용", 300, 3, 3, 3));
        chatInfoItems.add(new ChatInfoItem("http://image.dongascience.com/Photo/2020/03/5bddba7b6574b95d37b6079c199d7101.jpg", "https://www.sisajournal.com/news/photo/first/200508/img_102658_1.jpg", "", "https://d12zq4w4guyljn.cloudfront.net/750_750_20201122041810_photo1_5831aaf849cf.jpg", "파브리카 배달 구해용", "솔못에서 모일게요", 30000, 1, 2, 3));
        chatInfoItems.add(new ChatInfoItem("", "https://img1.daumcdn.net/thumb/R1280x0.fjpg/?fname=http://t1.daumcdn.net/brunch/service/user/9mqM/image/6vuarJpov779Xfo2EdNhLhmaPgI.JPG", "", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ-1FF9Hpe-_ERtrBHcUDeeckMOeOzm6IWylD_mJJlJEQ&s", "컴포즈 배달 구해요!!!", "맛나게 드셔요~", 100000, 0, 1, 1));
        chatInfoItems.add(new ChatInfoItem("", "", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSutGBoBGvVLOofPQ8mNAAKDpgD7NiHKzAyRSAL35gRQA&s", "", "밥버거 드실분", "넹", 300000, 0, 2, 1));

        adapter.setChatInfoList(chatInfoItems);

        /** "홈" 레이아웃 클릭 시 */
        binding.homeActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        /** "동네생활" 레이아웃 클릭 시 */
        binding.communityActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, CommunityActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        /** "글 쓰기" 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });

        /** "내 정보" 레이아웃 클릭 시 */
        binding.profileActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_view_item, parent, false);
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

                                // 빨간색 배경으로 변경
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

                long lastChatElapsedTime = item.getLastChatElapsedTime();
                String elapsedTime_str;

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastChatElapsedTime * 1000); // 초 단위를 밀리초 단위로 변환

                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(System.currentTimeMillis()); // 현재 시간을 밀리초 단위로 가져옴

                SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());

                if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                        && calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                    // 오늘
                    elapsedTime_str = "오늘 " + sdf.format(calendar.getTime());
                } else if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                        && calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1) {
                    // 어제
                    elapsedTime_str = "어제 " + sdf.format(calendar.getTime());
                } else {
                    // 그 이전
                    SimpleDateFormat dateFormat;
                    if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                        // 올해
                        dateFormat = new SimpleDateFormat("M월 d일", Locale.getDefault());
                    } else {
                        // 작년 이전
                        dateFormat = new SimpleDateFormat("yyyy. M. d.", Locale.getDefault());
                    }
                    elapsedTime_str = dateFormat.format(calendar.getTime());
                }

                lastChatElapsedTime_textView.setText(elapsedTime_str);


                int unreadMsgNum = item.getUnreadMsgNum();
                unreadMsgNum_relativeLayout.setVisibility(unreadMsgNum == 0 ? View.INVISIBLE : View.VISIBLE);
                unreadMsgNum_textView.setText("" + item.getUnreadMsgNum());
            }
        }
    }
}