package com.project.togather.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatInfoItem {
    private String firstChatUserProfileImageUrl;
    private String secondChatUserProfileImageUrl;
    private String thirdChatUserProfileImageUrl;
    private String postThumbnailImageUrl;
    private String title;
    private String lastChat;
    private long lastChatElapsedTime;
    private int unreadMsgNum;
    private int maxPartyMemberNum;
    private int currentPartyMemberNum;
}

