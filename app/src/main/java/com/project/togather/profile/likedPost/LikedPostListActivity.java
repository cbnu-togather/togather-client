package com.project.togather.profile.likedPost;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.databinding.ActivityLikedPostListBinding;
import com.project.togather.editPost.recruitment.EditRecruitmentPostSelectMeetingSpotActivity;
import com.project.togather.home.PostInfoResponse;
import com.project.togather.home.RecruitmentPostDetailActivity;
import com.project.togather.profile.myRecruitmentPartyPost.MyRecruitmentPartyPostListActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikedPostListActivity extends AppCompatActivity {

    private ActivityLikedPostListBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RetrofitService retrofitService;

    private RecyclerViewAdapter adapter;
    private ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLikedPostListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);


        adapter = new RecyclerViewAdapter();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                PostInfoItem selectedItem = postInfoItems.get(pos);
                Intent intent = new Intent(LikedPostListActivity.this, RecruitmentPostDetailActivity.class);
                intent.putExtra("post_id", selectedItem.getId());
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                PostInfoItem selectedItem = postInfoItems.get(pos);
                Intent intent = new Intent(LikedPostListActivity.this, RecruitmentPostDetailActivity.class);
                intent.putExtra("post_id", selectedItem.getId());
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.postsRecyclerView.setAdapter(adapter);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_home, parent, false);
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

        @Override
        public int getItemViewType(int position) {
            return position;
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
                if (item.getPostThumbnailImageUrl() != null && item.getPostThumbnailImageUrl().equals("")) {
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

                liked_imageView.setImageResource(item.isLikedState() ? R.drawable.like_filled : R.drawable.like_normal);
                likedCnt_textView.setText("" + item.getLikedCnt());
            }
        }

    }

    // 데이터 로딩 함수
    private void loadData() {
        Call<ResponseBody> call = userAPI.getLikedPosts();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        postInfoItems.clear();
                        String responseBody = response.body().string();
                        Type listType = new TypeToken<ArrayList<PostInfoResponse>>() {}.getType();
                        ArrayList<PostInfoResponse> postList = new Gson().fromJson(responseBody, listType);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                        // 현재 시간 UTC로 생성
                        Date now = new Date();

                        for (PostInfoResponse post : postList) {
                            try {
                                String createdAtString = post.getCreatedAt().split("\\.")[0];
                                Date createdAt = sdf.parse(createdAtString);
                                long elapsedTime = (now.getTime() - createdAt.getTime()) / 1000;
                                PostInfoItem item = new PostInfoItem(
                                        post.getId(),
                                        post.getImg(),
                                        post.getTitle(),
                                        post.getCategory(),
                                        elapsedTime,
                                        post.getHeadCount(),
                                        post.getCurrentCount(),
                                        post.isLiked(),
                                        post.getLikes()
                                );

                                postInfoItems.add(0, item);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.setPostInfoList(postInfoItems);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), LikedPostListActivity.this);
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
                    startActivity(new Intent(LikedPostListActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), LikedPostListActivity.this);
            }
        });
    }
    // 이 액티비티로 다시 돌아왔을 때 실행되는 메소드
    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
        loadData();
    }
}