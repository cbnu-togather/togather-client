package com.project.togather.profile.likedPost;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostInfoItem {
    private String postThumbnailImageUrl;
    private String title;
    private String category;
    private long elapsedTime;
    private int maxPartyMemberNum;
    private int currentPartyMemberNum;
    private boolean likedState;
    private int likedCnt;
}