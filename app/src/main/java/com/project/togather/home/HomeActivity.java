package com.project.togather.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.togather.ChatActivity;
import com.project.togather.CommunityActivity;
import com.project.togather.CreatePostActivity;
import com.project.togather.NotificationActivity;
import com.project.togather.ProfileActivity;
import com.project.togather.R;
import com.project.togather.RecruitmentPostDetailActivity;
import com.project.togather.databinding.ActivityHomeBinding;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new ListViewAdapter();

        // Adapter 안에 아이템의 정보 담기 (하드 코딩)
        adapter.addItem(new PostInfoItem("개신동 교촌치킨 파티 구함", "chicken", 320, 2, false, 1));
        adapter.addItem(new PostInfoItem("도미노 피자 드실분 구해요", "pizza`", 160, 0, false, 0));
        adapter.addItem(new PostInfoItem("사창동 우리집 닭강정 파티!!", "chicken", 500, 1, false, 0));
        adapter.addItem(new PostInfoItem("맘스터치 배달 파티 999~~", "hamburger", 600, 3, true, 2));
        adapter.addItem(new PostInfoItem("행컵 그룹 구해용", "korean_food", 550, 0, false, 0));
        adapter.addItem(new PostInfoItem("짚신 스시&롤 배달 구해요", "japanese_food", 555, 0, true, 1));
        adapter.addItem(new PostInfoItem("대장집 파티 구", "chinese_food", 560, 0, false, 0));
        adapter.addItem(new PostInfoItem("파브리카 배달 구해용", "western_food", 700, 3, false, 1));
        adapter.addItem(new PostInfoItem("신전 떡볶이 구해유", "snack", 900, 1, false, 2));
        adapter.addItem(new PostInfoItem("메가커피 999", "cafe_and_dessert", 1000, 5, false, 2));
        adapter.addItem(new PostInfoItem("컴포즈 배달 구해요!!!", "cafe_and_dessert", 1500, 1, false, 1));

        // 리스트뷰에 Adapter 설정
        binding.postListView.setAdapter(adapter);

        /** "알림" 버튼 클릭 시 */
        binding.notificationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        /** "신규 알림" 버튼 클릭 시 */
        binding.notificationNewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        /** "전체" 탭 버튼 클릭 시 */
        binding.allFoodTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.allFoodTabButton.setTypeface(null, Typeface.BOLD);
                binding.allFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.allFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "치킨" 탭 버튼 클릭 시 */
        binding.chickenTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.chickenTabButton.setTypeface(null, Typeface.BOLD);
                binding.chickenTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.chickenTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "피자" 탭 버튼 클릭 시 */
        binding.pizzaTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.pizzaTabButton.setTypeface(null, Typeface.BOLD);
                binding.pizzaTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.pizzaTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "햄버거" 탭 버튼 클릭 시 */
        binding.hamburgerTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.hamburgerTabButton.setTypeface(null, Typeface.BOLD);
                binding.hamburgerTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.hamburgerTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "한식" 탭 버튼 클릭 시 */
        binding.koreanFoodTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.koreanFoodTabButton.setTypeface(null, Typeface.BOLD);
                binding.koreanFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.koreanFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "일식" 탭 버튼 클릭 시 */
        binding.japaneseFoodTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.japaneseFoodTabButton.setTypeface(null, Typeface.BOLD);
                binding.japaneseFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.japaneseFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "중식" 탭 버튼 클릭 시 */
        binding.chineseFoodTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.chineseFoodTabButton.setTypeface(null, Typeface.BOLD);
                binding.chineseFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.chineseFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "양식" 탭 버튼 클릭 시 */
        binding.westernFoodTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.westernFoodTabButton.setTypeface(null, Typeface.BOLD);
                binding.westernFoodTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.westernFoodTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });
        /** "분식" 탭 버튼 클릭 시 */
        binding.snackTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.snackTabButton.setTypeface(null, Typeface.BOLD);
                binding.snackTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.snackTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "카페·디저트" 탭 버튼 클릭 시 */
        binding.cafeAndDessertTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allTabStyleClear();
                binding.cafeAndDessertTabButton.setTypeface(null, Typeface.BOLD);
                binding.cafeAndDessertTabButton.setTextColor(getResources().getColor(R.color.text_color));
                binding.cafeAndDessertTabButton.setBackground(getResources().getDrawable(R.drawable.selected_category_tab_border_bottom));
            }
        });

        /** "동네생활" 레이아웃 클릭 시 */
        binding.communityActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, CommunityActivity.class);
                startActivity(intent);
            }
        });

        /** "글 쓰기" 레이아웃 클릭 시 */
        binding.createPostActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });

        /** "채팅" 레이아웃 클릭 시 */
        binding.chatActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        /** "내 정보" 레이아웃 클릭 시 */
        binding.profileActivityRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
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
                convertView = inflater.inflate(R.layout.food_list_view_item, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            TextView postTitle_textView = convertView.findViewById(R.id.postTitle_textView);
            TextView category_textView = convertView.findViewById(R.id.category_textView);
            TextView elapsedTime_textView = convertView.findViewById(R.id.elapsedTime_textView);

            ImageView foodIcon_imageView = convertView.findViewById(R.id.foodIcon_imageView);

            ImageView currentPartyMemberNumFirstState_imageView = convertView.findViewById(R.id.currentPartyMemberNumFirstState_imageView);
            ImageView currentPartyMemberNumSecondState_imageView = convertView.findViewById(R.id.currentPartyMemberSecondState_imageView);
            ImageView currentPartyMemberNumThirdState_imageView = convertView.findViewById(R.id.currentPartyMemberNumThirdState_imageView);
            ImageView currentPartyMemberNumFourthState_imageView = convertView.findViewById(R.id.currentPartyMemberNumFourthState_imageView);
            ImageView currentPartyMemberNumFifthState_imageView = convertView.findViewById(R.id.currentPartyMemberNumFifthState_imageView);

            ImageView liked_imageView = convertView.findViewById(R.id.liked_imageView);

            TextView likedCnt_textView = convertView.findViewById(R.id.likedCnt_textView);

            switch (postInfoItem.getCategory()) {
                case "chicken":
                    foodIcon_imageView.setImageResource(R.drawable.chicken);
                    category_textView.setText("치킨");
                    break;
                case "pizza":
                    foodIcon_imageView.setImageResource(R.drawable.pizza);
                    category_textView.setText("피자");
                    break;
                case "hamburger":
                    foodIcon_imageView.setImageResource(R.drawable.hamburger);
                    category_textView.setText("햄버거");
                    break;
                case "korean_food":
                    foodIcon_imageView.setImageResource(R.drawable.korean_food);
                    category_textView.setText("한식");
                    break;
                case "japanese_food":
                    foodIcon_imageView.setImageResource(R.drawable.japanese_food);
                    category_textView.setText("일식");
                    break;
                case "chinese_food":
                    foodIcon_imageView.setImageResource(R.drawable.chinese_food);
                    category_textView.setText("중식");
                    break;
                case "western_food":
                    foodIcon_imageView.setImageResource(R.drawable.western_food);
                    category_textView.setText("양식");
                    break;
                case "snack":
                    foodIcon_imageView.setImageResource(R.drawable.snack);
                    category_textView.setText("분식");
                    break;
                case "cafe_and_dessert":
                    foodIcon_imageView.setImageResource(R.drawable.cafe_and_dessert);
                    category_textView.setText("카페·디저트");
                    break;
                default:
                    Log.d("로그: ", postInfoItem.getCategory() + "는 존재하지 않는 카테고리입니다.");
            }

            postTitle_textView.setText(postInfoItem.getTitle());

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

            currentPartyMemberNumFirstState_imageView.setImageResource(postInfoItem.getCurrentPartyMemberNum() >= 1 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
            currentPartyMemberNumSecondState_imageView.setImageResource(postInfoItem.getCurrentPartyMemberNum() >= 2 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
            currentPartyMemberNumThirdState_imageView.setImageResource(postInfoItem.getCurrentPartyMemberNum() >= 3 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
            currentPartyMemberNumFourthState_imageView.setImageResource(postInfoItem.getCurrentPartyMemberNum() >= 4 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);
            currentPartyMemberNumFifthState_imageView.setImageResource(postInfoItem.getCurrentPartyMemberNum() >= 5 ? R.drawable.one_person_logo_filled : R.drawable.one_person_logo);

            liked_imageView.setImageResource(postInfoItem.getLikedState() ? R.drawable.like_filled : R.drawable.like_normal);
            likedCnt_textView.setText("" + postInfoItem.getLikedCnt());

            //각 아이템 선택 event
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomeActivity.this, RecruitmentPostDetailActivity.class);
                    startActivity(intent);
                }
            });

            return convertView;  //뷰 객체 반환
        }
    }

    // 음식 카테고리 탭에 설정된 스타일을 제거하는 함수
    void allTabStyleClear() {
        binding.allFoodTabButton.setBackground(null);
        binding.allFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.allFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.chickenTabButton.setBackground(null);
        binding.chickenTabButton.setTypeface(null, Typeface.NORMAL);
        binding.chickenTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.pizzaTabButton.setBackground(null);
        binding.pizzaTabButton.setTypeface(null, Typeface.NORMAL);
        binding.pizzaTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.hamburgerTabButton.setBackground(null);
        binding.hamburgerTabButton.setTypeface(null, Typeface.NORMAL);
        binding.hamburgerTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.koreanFoodTabButton.setBackground(null);
        binding.koreanFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.koreanFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.japaneseFoodTabButton.setBackground(null);
        binding.japaneseFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.japaneseFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.chineseFoodTabButton.setBackground(null);
        binding.chineseFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.chineseFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.westernFoodTabButton.setBackground(null);
        binding.westernFoodTabButton.setTypeface(null, Typeface.NORMAL);
        binding.westernFoodTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.snackTabButton.setBackground(null);
        binding.snackTabButton.setTypeface(null, Typeface.NORMAL);
        binding.snackTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));

        binding.cafeAndDessertTabButton.setBackground(null);
        binding.cafeAndDessertTabButton.setTypeface(null, Typeface.NORMAL);
        binding.cafeAndDessertTabButton.setTextColor(getResources().getColor(R.color.not_selected_menu_item_gray_color));
    }
}