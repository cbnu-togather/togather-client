package com.project.togather.community;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommunityPostDetailItem {
    private String writerName;
    private String writerImg;
    private String address;
    private String title;
    private String img;
    private String content;
    private String createdAt;
    private int view;
    private int likes;
    private CommentInfoItem[] comments;
    private String category;
    private boolean writer;
}
