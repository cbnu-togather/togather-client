package com.project.togather.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostInfoResponse {
    private int id;
    private String title;
    private String img;
    private String createdAt;
    private int headCount;
    private int currentCount;
    private int likes;
    private String category;
    private boolean liked;
}
