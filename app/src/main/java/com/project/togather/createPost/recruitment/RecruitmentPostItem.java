package com.project.togather.createPost.recruitment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecruitmentPostItem {
    private String title;
    private String content;
    private int latitude;
    private int longitude;
    private int headCount;
    private String address;
    private String spotName;
    private String category;
}
