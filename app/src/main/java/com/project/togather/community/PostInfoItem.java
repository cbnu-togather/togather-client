package com.project.togather.community;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostInfoItem {
    private String postThumbnailImageUrl;
    private String tag;
    private String title;
    private String content;
    private String district;
    private long elapsedTime;
    private int likedCnt;
}