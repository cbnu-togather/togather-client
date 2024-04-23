package com.project.togather.profile.myRecruitmentPartyPost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.project.togather.R;
import com.project.togather.databinding.ActivityMyRecruitmentPartyListBinding;
import com.project.togather.home.HomePostDetailActivity;

import java.util.ArrayList;

public class MyRecruitmentPartyPostListActivity extends AppCompatActivity {

    private ActivityMyRecruitmentPartyListBinding binding;

    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyRecruitmentPartyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new RecyclerViewAdapter();

        ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                startActivity(new Intent(MyRecruitmentPartyPostListActivity.this, HomePostDetailActivity.class));
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                startActivity(new Intent(MyRecruitmentPartyPostListActivity.this, HomePostDetailActivity.class));
            }
        });

        // initiate recyclerview
        binding.postsRecyclerView.setAdapter(adapter);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        postInfoItems.add(new PostInfoItem("https://cdn.mkhealth.co.kr/news/photo/202306/64253_68458_1153.png", "개신동 교촌치킨 파티 구함", "chicken", 320, 3, 2, false, 1));
        postInfoItems.add(new PostInfoItem("https://cdn.dominos.co.kr/admin/upload/goods/20240214_8rBc1T61.jpg?RS=350x350&SP=1", "도미노 피자 드실분 구해요", "pizza", 160, 3, 3, false, 0));
        postInfoItems.add(new PostInfoItem("https://mblogthumb-phinf.pstatic.net/MjAyMjA3MjhfMTY5/MDAxNjU4OTkyODg0NTA3.z8WzaZAOKBvo4JkSm9lTMOTiNsKEUNHZJYRB-DPZCdEg.0WdqohiJPsSM5pXWYl-HvTE3JUVlUPe7LT-U6wvjUQwg.JPEG.duwlsrjdwb/KakaoTalk_20220728_151114228_10.jpg?type=w800", "사창동 우리집 닭강정 파티!!", " chicken ", 500, 1, 0, false, 0));

        adapter.setPostInfoList(postInfoItems);

        /** 뒤로가기 버튼 기능 */
        binding.backImageButton.setOnClickListener(view -> finish());
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<PostInfoItem> items = new ArrayList<>();

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_view_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(items.get(position));
        }

        public void setPostInfoList(ArrayList<PostInfoItem> list) {
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

            ImageView post_imageView;
            ImageView currentPartyMemberNumFirstState_imageView;
            ImageView currentPartyMemberSecondState_imageView;
            ImageView currentPartyMemberNumThirdState_imageView;
            ImageView liked_imageView;

            TextView postTitle_textView;
            TextView category_textView;
            TextView elapsedTime_textView;
            TextView recruitmentComplete_textView;
            TextView likedCnt_textView;

            CardView currentPartyMemberNumFirstState_cardView;
            CardView currentPartyMemberNumSecondState_cardView;
            CardView currentPartyMemberNumThirdState_cardView;

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

                post_imageView = itemView.findViewById(R.id.post_imageView);

                postTitle_textView = itemView.findViewById(R.id.postTitle_textView);
                category_textView = itemView.findViewById(R.id.category_textView);
                elapsedTime_textView = itemView.findViewById(R.id.elapsedTime_textView);
                recruitmentComplete_textView = itemView.findViewById(R.id.recruitmentCompleteTag_textView);

                currentPartyMemberNumFirstState_cardView = itemView.findViewById(R.id.currentPartyMemberNumFirstState_cardView);
                currentPartyMemberNumSecondState_cardView = itemView.findViewById(R.id.currentPartyMemberNumSecondState_cardView);
                currentPartyMemberNumThirdState_cardView = itemView.findViewById(R.id.currentPartyMemberNumThirdState_cardView);

                currentPartyMemberNumFirstState_imageView = itemView.findViewById(R.id.currentPartyMemberNumFirstState_imageView);
                currentPartyMemberSecondState_imageView = itemView.findViewById(R.id.currentPartyMemberSecondState_imageView);
                currentPartyMemberNumThirdState_imageView = itemView.findViewById(R.id.currentPartyMemberNumThirdState_imageView);

                liked_imageView = itemView.findViewById(R.id.liked_imageView);

                likedCnt_textView = itemView.findViewById(R.id.likedCnt_textView);
            }

            void onBind(PostInfoItem item) {
                if (item.getPostThumbnailImageUrl().equals("")) {
                    post_imageView.setImageResource(R.drawable.post_thumbnail_background_logo);
                } else {
                    Glide.with(itemView)
                            .load(item.getPostThumbnailImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                postTitle_textView.setText(item.getTitle());
                switch (item.getCategory()) {
                    case "chicken":
                        category_textView.setText("치킨");
                        break;
                    case "pizza":
                        category_textView.setText("피자");
                        break;
                    case "hamburger":
                        category_textView.setText("햄버거");
                        break;
                    case "korean_food":
                        category_textView.setText("한식");
                        break;
                    case "japanese_food":
                        category_textView.setText("일식");
                        break;
                    case "chinese_food":
                        category_textView.setText("중식");
                        break;
                    case "western_food":
                        category_textView.setText("양식");
                        break;
                    case "snack":
                        category_textView.setText("분식");
                        break;
                    case "cafe_and_dessert":
                        category_textView.setText("카페·디저트");
                        break;
                    default:
                        Log.d("로그: ", item.getCategory() + "는 존재하지 않는 카테고리입니다.");
                }

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

                if (item.getMaxPartyMemberNum() == item.getCurrentPartyMemberNum()) {
                    recruitmentComplete_textView.setVisibility(View.VISIBLE);
                } else {
                    currentPartyMemberNumFirstState_cardView.setVisibility(item.getMaxPartyMemberNum() >= 1 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberNumFirstState_imageView.setImageResource(item.getCurrentPartyMemberNum() >= 1 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

                    currentPartyMemberNumSecondState_cardView.setVisibility(item.getMaxPartyMemberNum() >= 2 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberSecondState_imageView.setImageResource(item.getCurrentPartyMemberNum() >= 2 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

                    currentPartyMemberNumThirdState_cardView.setVisibility(item.getMaxPartyMemberNum() >= 3 ? View.VISIBLE : View.INVISIBLE);
                    currentPartyMemberNumThirdState_imageView.setImageResource(item.getCurrentPartyMemberNum() >= 3 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
                }

                liked_imageView.setImageResource(item.getLikedState() ? R.drawable.like_filled : R.drawable.like_normal);
                likedCnt_textView.setText("" + item.getLikedCnt());
            }
        }

    }
}