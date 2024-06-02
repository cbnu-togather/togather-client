package com.project.togather.profile.myCommunityPost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.project.togather.MainActivity;
import com.project.togather.R;
import com.project.togather.community.CommunityPostDetailActivity;
import com.project.togather.databinding.ActivityMyCommunityPostListBinding;
import com.project.togather.editPost.recruitment.EditRecruitmentPostSelectMeetingSpotActivity;
import com.project.togather.retrofit.RetrofitService;
import com.project.togather.retrofit.interfaceAPI.UserAPI;
import com.project.togather.toast.ToastWarning;
import com.project.togather.utils.TokenManager;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCommunityPostListActivity extends AppCompatActivity {

    private ActivityMyCommunityPostListBinding binding;
    private TokenManager tokenManager;
    private UserAPI userAPI;
    private RetrofitService retrofitService;

    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyCommunityPostListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        retrofitService = new RetrofitService(tokenManager);
        userAPI = retrofitService.getRetrofit().create(UserAPI.class);

        adapter = new RecyclerViewAdapter();

        ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent(MyCommunityPostListActivity.this, CommunityPostDetailActivity.class);
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                Intent intent = new Intent(MyCommunityPostListActivity.this, CommunityPostDetailActivity.class);
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.postsRecyclerView.setAdapter(adapter);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        postInfoItems.add(new PostInfoItem("https://gd.image-gmkt.com/%EC%BD%94%EC%B9%98-%EC%BD%94%EC%B9%98-%EB%B8%8C%EB%9D%BC%EC%9A%B4-%EA%B0%80%EC%A3%BD-%EB%82%A8%EB%85%80-%ED%95%99%EC%83%9D-%EC%A7%81%EC%9E%A5%EC%9D%B8-%EB%B0%B1%ED%8C%A9-%EC%B1%85%EA%B0%80%EB%B0%A9-%EC%A4%91%EA%B3%A0-%EB%AA%85%ED%92%88-%EA%B0%80%EB%B0%A9-%EC%9D%80%ED%94%BC%EC%95%84%EB%85%B8/li/600/574/2388574600.g_350-w-et-pj_g.jpg", "나눔", "가방 나눔해요~~", "새거입니다.", "개신동", 320, 1));
        postInfoItems.add(new PostInfoItem("https://mblogthumb-phinf.pstatic.net/MjAyMjA2MDhfMjMy/MDAxNjU0NjgwMDEyNzQ2.jkfgtrFgZlbkEzgAXCmgEK7lzIcgJOwiMwNfjm9dfUog.weSpjt3puKOlolJv8fZUuNys5s7Vh9RAuJkX2Ikoe0cg.JPEG.dpfls111103/20220602%EF%BC%BF144812.jpg?type=w800", "고민/사연", "실외기 비둘기가 나뭇가지를..ㅠ", "실외기에 비둘기가 나뭇가지를..어제보니 실외기 위에 나뭇가지가 수북히 쌓여있더라구요...\n집은2층 입니다 실외기 커버 씌여놓은 상태구요..ㅠ\n혹시 경험 있으신 분이나 방법 아시는 분..\n조언 좀 부탁드릴게요ㅠ", "사창동", 320, 6));
        postInfoItems.add(new PostInfoItem("", "맛집", "청주에 파스타 샐러드 / 샌드위치 맛집 있을까요?", "많은 추천 부탁드려요 ㅎㅎ", "율량동", 320, 2));

        adapter.setPostInfoList(postInfoItems);

        /** 뒤로가기 버튼 기능 */
        binding.backImageButton.setOnClickListener(view -> finish());
    }

    /**
     * 리스트뷰 어댑터
     */
    public class ListViewAdapter extends BaseAdapter {
        ArrayList<PostInfoItem> items = new ArrayList<PostInfoItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(PostInfoItem item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();
            final PostInfoItem postInfoItem = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_view_item_community, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            ImageView post_imageView = convertView.findViewById(R.id.post_imageView);

            TextView postTitle_textView = convertView.findViewById(R.id.postTitle_textView);
            TextView district_textView = convertView.findViewById(R.id.district_textView);
            TextView elapsedTime_textView = convertView.findViewById(R.id.elapsedTime_textView);


            ImageView liked_imageView = convertView.findViewById(R.id.liked_imageView);
            TextView likedCnt_textView = convertView.findViewById(R.id.likedCnt_textView);

            postTitle_textView.setText(postInfoItem.getTitle());
            district_textView.setText(postInfoItem.getDistrict());

            long elapsedTime = postInfoItem.getElapsedTime();
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

            post_imageView.setImageResource(R.drawable.community_temp_image_1);

            likedCnt_textView.setText("" + postInfoItem.getLikedCnt());

            //각 아이템 선택 event
            convertView.setOnClickListener(view ->
                    startActivity(new Intent(MyCommunityPostListActivity.this, CommunityPostDetailActivity.class)));

            return convertView;  //뷰 객체 반환
        }
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<PostInfoItem> items = new ArrayList<>();

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

        private OnLongItemClickListener onLongItemClickListener = null;

        public void setOnLongItemClickListener(OnLongItemClickListener listener) {
            this.onLongItemClickListener = listener;
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_community, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
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
            ImageView liked_imageView;

            TextView hotPostTag_textView;
            TextView categoryTag_textView;
            TextView postTitle_textView;
            TextView postContent_textView;
            TextView district_textView;
            TextView elapsedTime_textView;
            TextView likedCnt_textView;

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

                hotPostTag_textView = itemView.findViewById(R.id.hotPostTag_textView);
                categoryTag_textView = itemView.findViewById(R.id.categoryTag_textView);
                postTitle_textView = itemView.findViewById(R.id.postTitle_textView);
                postContent_textView = itemView.findViewById(R.id.postContent_textView);
                district_textView = itemView.findViewById(R.id.district_textView);
                elapsedTime_textView = itemView.findViewById(R.id.elapsedTime_textView);

                likedCnt_textView = itemView.findViewById(R.id.likedCnt_textView);
            }

            void onBind(PostInfoItem item) {
                if (item.getPostThumbnailImageUrl().equals("")) {
                    post_imageView.setVisibility(View.GONE);
                    postTitle_textView.setMaxWidth(1000);
                } else {
                    Glide.with(itemView)
                            .load(item.getPostThumbnailImageUrl()) // 이미지 URL 가져오기
                            .placeholder(R.drawable.one_person_logo) // 로딩 중에 표시할 이미지
                            .error(R.drawable.one_person_logo) // 에러 발생 시 표시할 이미지
                            .into(post_imageView); // ImageView에 이미지 설정
                }

                hotPostTag_textView.setVisibility(item.getLikedCnt() > 5 ? View.VISIBLE : View.GONE);
                categoryTag_textView.setText(item.getTag());

                String postTitle = item.getTitle();
                ViewGroup.LayoutParams layoutParams = root_relativeLayout.getLayoutParams();
                if (postTitle.length() >= 28)
                    postTitle = postTitle.substring(0, 28) + "...";
                else if (!item.getPostThumbnailImageUrl().equals("") && postTitle.length() >= 19)
                    postTitle = postTitle.substring(0, 19) + "...";

                postTitle_textView.setText(postTitle);

                String postContent = item.getContent();
                if (item.getPostThumbnailImageUrl().equals("") && postContent.length() >= 29)
                    postContent = postContent.substring(0, 29) + "...";
                else if(!item.getPostThumbnailImageUrl().equals("") && postContent.length() >= 24)
                    postContent = postContent.substring(0, 24) + "...";

                postContent_textView.setText(postContent);

                district_textView.setText(item.getDistrict());

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

                likedCnt_textView.setText("" + item.getLikedCnt());
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
                    startActivity(new Intent(MyCommunityPostListActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new ToastWarning(getResources().getString(R.string.toast_server_error), MyCommunityPostListActivity.this);
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
