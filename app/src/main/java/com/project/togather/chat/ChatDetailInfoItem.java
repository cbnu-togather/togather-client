package com.project.togather.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatDetailInfoItem {
    private String userProfileImageUrl;
    private String username;
    private String message;
    private String ImageUrl;
    private long timestamp;
    private boolean isMyMessage;
    private boolean isContinuousMessage;
}

