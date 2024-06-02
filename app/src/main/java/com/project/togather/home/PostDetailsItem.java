package com.project.togather.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostDetailsItem {
    private String writerName;
    private String writerImg;
    private String address;
    private String spotName;
    private String title;
    private String img;
    private String content;
    private String createdAt;
    private int headCount;
    private int currentCount;
    private double latitude;
    private double longitude;
    private int view;
    private int likes;
    private boolean writer;
    private String category;
}
