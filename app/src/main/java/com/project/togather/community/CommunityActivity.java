package com.project.togather.community;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.togather.MainActivity;
import com.project.togather.chat.ChatActivity;
import com.project.togather.createPost.community.CreateCommunityPostActivity;
import com.project.togather.createPost.recruitment.CreateRecruitmentPostActivity;
import com.project.togather.databinding.ActivityCommunityBinding;
import com.project.togather.notification.NotificationActivity;
import com.project.togather.profile.ProfileActivity;
import com.project.togather.R;
import com.project.togather.home.HomeActivity;
import com.project.togather.utils.TokenManager;

import java.util.ArrayList;

public class CommunityActivity extends AppCompatActivity {

    private ActivityCommunityBinding binding;

    private RecyclerViewAdapter adapter;
    private TokenManager tokenManager;

    ArrayList<PostInfoItem> postInfoItems = new ArrayList<>();

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private BottomSheetBehavior selectCreatePostTypeBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // 토큰 값이 없다면 메인 액티비티로 이동
        if (tokenManager.getToken() == null) {
            startActivity(new Intent(CommunityActivity.this, MainActivity.class));
            finish();
        }

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
                Intent intent = new Intent(CommunityActivity.this, CommunityPostDetailActivity.class);
                startActivity(intent);
            }
        });

        adapter.setOnLongItemClickListener(new RecyclerViewAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                Intent intent = new Intent(CommunityActivity.this, CommunityPostDetailActivity.class);
                startActivity(intent);
            }
        });

        // initiate recyclerview
        binding.postsRecyclerView.setAdapter(adapter);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(); // 데이터 새로고침 메소드 호출
            }
        });

        // 초기 데이터 로드
        loadData();

        /** "알림" 버튼 클릭 시 */
        binding.notificationImageButton.setOnClickListener(view ->
                startActivity(new Intent(CommunityActivity.this, NotificationActivity.class)));

        /** "신규 알림" 버튼 클릭 시 */
        binding.notificationNewImageButton.setOnClickListener(view ->
                startActivity(new Intent(CommunityActivity.this, NotificationActivity.class)));

        /** "홈" 레이아웃 클릭 시 */
        binding.homeActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(CommunityActivity.this, HomeActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        // 어두운 배경 클릭 이벤트 설정
        binding.backgroundDimmer.setOnClickListener(view -> {
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
                startActivity(new Intent(CommunityActivity.this, CreateRecruitmentPostActivity.class));
            }
        });

        findViewById(R.id.createCommunityPost_button).setOnClickListener(view -> {
            if (selectCreatePostTypeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                selectCreatePostTypeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                startActivity(new Intent(CommunityActivity.this, CreateCommunityPostActivity.class));
            }
        });

        /** "채팅" 레이아웃 클릭 시 */
        binding.chatActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(CommunityActivity.this, ChatActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        /** "내 정보" 레이아웃 클릭 시 */
        binding.profileActivityRelativeLayout.setOnClickListener(view -> {
            startActivity(new Intent(CommunityActivity.this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
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
                    startActivity(new Intent(CommunityActivity.this, CommunityPostDetailActivity.class)));

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

    // 데이터 새로고침 함수
    private void refreshData() {
        // 기존 데이터를 비우는 로직 추가
        postInfoItems.clear();

        // 새 데이터 추가 (하드 코딩) : 새로고침 했더니 게시글이 두 개만 남았다는 가정
        postInfoItems.add(new PostInfoItem("https://gd.image-gmkt.com/%EC%BD%94%EC%B9%98-%EC%BD%94%EC%B9%98-%EB%B8%8C%EB%9D%BC%EC%9A%B4-%EA%B0%80%EC%A3%BD-%EB%82%A8%EB%85%80-%ED%95%99%EC%83%9D-%EC%A7%81%EC%9E%A5%EC%9D%B8-%EB%B0%B1%ED%8C%A9-%EC%B1%85%EA%B0%80%EB%B0%A9-%EC%A4%91%EA%B3%A0-%EB%AA%85%ED%92%88-%EA%B0%80%EB%B0%A9-%EC%9D%80%ED%94%BC%EC%95%84%EB%85%B8/li/600/574/2388574600.g_350-w-et-pj_g.jpg", "나눔", "가방 나눔해요~~", "새거입니다.", "개신동", 320, 1));
        postInfoItems.add(new PostInfoItem("https://mblogthumb-phinf.pstatic.net/MjAyMjA2MDhfMjMy/MDAxNjU0NjgwMDEyNzQ2.jkfgtrFgZlbkEzgAXCmgEK7lzIcgJOwiMwNfjm9dfUog.weSpjt3puKOlolJv8fZUuNys5s7Vh9RAuJkX2Ikoe0cg.JPEG.dpfls111103/20220602%EF%BC%BF144812.jpg?type=w800", "고민/사연", "실외기 비둘기가 나뭇가지를..ㅠ", "실외기에 비둘기가 나뭇가지를..어제보니 실외기 위에 나뭇가지가 수북히 쌓여있더라구요...\n집은2층 입니다 실외기 커버 씌여놓은 상태구요..ㅠ\n혹시 경험 있으신 분이나 방법 아시는 분..\n조언 좀 부탁드릴게요ㅠ", "사창동", 320, 6));

        // 어댑터에 변경된 데이터 리스트를 설정
        adapter.setPostInfoList(postInfoItems);

        // RecyclerView의 레이아웃 매니저와 어댑터를 다시 설정하여 UI를 갱신
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.postsRecyclerView.setAdapter(adapter);

        // 새로고침 아이콘을 숨김 (새로고침이 끝났음을 의미)
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    // 초기 데이터 로딩 함수
    private void loadData() {
        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        postInfoItems.add(new PostInfoItem("https://gd.image-gmkt.com/%EC%BD%94%EC%B9%98-%EC%BD%94%EC%B9%98-%EB%B8%8C%EB%9D%BC%EC%9A%B4-%EA%B0%80%EC%A3%BD-%EB%82%A8%EB%85%80-%ED%95%99%EC%83%9D-%EC%A7%81%EC%9E%A5%EC%9D%B8-%EB%B0%B1%ED%8C%A9-%EC%B1%85%EA%B0%80%EB%B0%A9-%EC%A4%91%EA%B3%A0-%EB%AA%85%ED%92%88-%EA%B0%80%EB%B0%A9-%EC%9D%80%ED%94%BC%EC%95%84%EB%85%B8/li/600/574/2388574600.g_350-w-et-pj_g.jpg", "나눔", "가방 나눔해요~~", "새거입니다.", "개신동", 320, 1));
        postInfoItems.add(new PostInfoItem("https://mblogthumb-phinf.pstatic.net/MjAyMjA2MDhfMjMy/MDAxNjU0NjgwMDEyNzQ2.jkfgtrFgZlbkEzgAXCmgEK7lzIcgJOwiMwNfjm9dfUog.weSpjt3puKOlolJv8fZUuNys5s7Vh9RAuJkX2Ikoe0cg.JPEG.dpfls111103/20220602%EF%BC%BF144812.jpg?type=w800", "고민/사연", "실외기 비둘기가 나뭇가지를..ㅠ", "실외기에 비둘기가 나뭇가지를..어제보니 실외기 위에 나뭇가지가 수북히 쌓여있더라구요...\n집은2층 입니다 실외기 커버 씌여놓은 상태구요..ㅠ\n혹시 경험 있으신 분이나 방법 아시는 분..\n조언 좀 부탁드릴게요ㅠ", "사창동", 320, 6));
        postInfoItems.add(new PostInfoItem("", "맛집", "청주에 파스타 샐러드 / 샌드위치 맛집 있을까요?", "많은 추천 부탁드려요 ㅎㅎ", "율량동", 320, 2));
        postInfoItems.add(new PostInfoItem("", "이사/시공", "폐목재", "리모델링하면서 나온 다루끼 폐목재들을 어떻게 버려야 할까요..?", "개신동", 320, 0));
        postInfoItems.add(new PostInfoItem("", "부동산/주거", "전세계약 어떻게 하셨나요", "전세로 계약 시 계약서는 1대1로 쓰시나요?\n혹시 계약서만 부동산에서 작성할 수 있는 건가요?", "개신동", 320, 1));
        postInfoItems.add(new PostInfoItem("", "모임", "보드게임 모임사람 구합니다", "보드게임 모임 사람 모집합니다! 모임은 보통 주말에 충대 보드카페나 게임들고 일반카페도 가끔 갑니당☺️☺️ 게임 아는 거 없어도 가능해요! 루미큐브 할리갈리만 해 분신 분 가능! 티츄 좋아하거나 전략 게임 좋아하시는 분! 다양한 게임 배우고 싶으신 분! 환영합니다! 와서 같이 놀아요! 신천지 여미새 남미새 사절😒", "개신동", 320, 2));
        postInfoItems.add(new PostInfoItem("https://i.balaan.io/images/87/879c94/879c94f2397057afb0b9f72673c33ab1dc599ff66b09049433eab961a198f357.jpg", "분실/실종", "카드 케이스를 잃어버렸어요ㅠㅠ", "하복대에서 분실했습니다..\n보신 분 있으면 연락 주세요 사례 해 드릴게요...ㅠㅠ", "개신동", 320, 0));
        postInfoItems.add(new PostInfoItem("", "운동", "강서동 헬스장 회원권 양도합니다 싸게 해드릴께요 ㅠ", "연락 주세용~", "개신동", 320, 1));
        postInfoItems.add(new PostInfoItem("", "생활/편의", "가경동이나 근처에 믿을만한 차량 정비소 있을까요?", "예전에는 집 뒤에 있는 카센터에 가서 믿고 맡기는 편이었습니다. 사장님이 친절하시고 기술료도 적정선에서 받으셔서 부담은 없었어요. 그런데 그 사장님이 바뀐 뒤로 불친절하고 그래서 다른 곳을 찾고 있는데, 마땅한 곳이 없습니다.\n\n봉명동 공임나라랑 가경동 블루핸즈는 정말 괜찮은 편이긴 하지만 봉명동은 거리가 조금 멀고 블루핸즈는 무조건 모비스 정품 수리라 비싸다는 단점이 있더라고요.\n\n공임나라랑 비교해서 조금 비싸더라도 그냥 믿고 맡겼을 때 작업 완성도가 좋은 정비소를 찾고 있습니다.\n혹시 정비를 받으시고 좋았던 곳이 있을까요? 추천 부탁드립니다..!", "개신동", 320, 2));
        postInfoItems.add(new PostInfoItem("", "풍경", "청주에서 모래사장 있는 놀이터 아시면 알려주시면 감사하겠습니다 ㅠㅠ", "콘텐츠 잠깐 찍을려고 하는데 요즘에 공원에 모래가 있는 곳이 안 보이네요 ㅠㅠ", "개신동", 320, 0));

        adapter.setPostInfoList(postInfoItems);
    }
}