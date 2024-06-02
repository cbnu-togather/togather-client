package com.project.togather.community;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommunityInfoResponse {
    private int id;
    private String title;
    private String img;
    private String category;
    private String createdAt;
    private String address;
    private String content;
    private int likes;

}
