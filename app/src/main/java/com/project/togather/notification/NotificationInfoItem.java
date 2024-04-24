package com.project.togather.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationInfoItem {
    private String userProfileImageUrl;
    private String postThumbnailImageUrl;
    private String title;
    private String category;
    private long elapsedTime;
    private int maxPartyMemberNum;
    private int currentPartyMemberNum;
    private String message;
}

