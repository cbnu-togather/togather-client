package com.project.togather.community;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentInfoResponse {
    private int id;
    private String content;
    private String writerName;
    private String writerImg;
    private String createdAt;
    private String who;
    private String img;
}
