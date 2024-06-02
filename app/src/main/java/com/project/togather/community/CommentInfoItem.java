package com.project.togather.community;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentInfoItem {
    private int id;
    private String userProfileImageUrl;
    private String username;
    private String comment;
    private String ImageUrl;
    private long elapsedTime;
    private String who;
}

