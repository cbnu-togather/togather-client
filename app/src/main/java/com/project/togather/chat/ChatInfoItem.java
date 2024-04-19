package com.project.togather.chat;

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

    public ChatInfoItem(String firstChatUserProfileImageUrl, String secondChatUserProfileImageUrl, String thirdChatUserProfileImageUrl, String postThumbnailImageUrl, String title, String lastChat, long lastChatElapsedTime, int unreadMsgNum, int maxPartyMemberNum, int currentPartyMemberNum) {
        this.firstChatUserProfileImageUrl = firstChatUserProfileImageUrl;
        this.secondChatUserProfileImageUrl = secondChatUserProfileImageUrl;
        this.thirdChatUserProfileImageUrl = thirdChatUserProfileImageUrl;
        this.postThumbnailImageUrl = postThumbnailImageUrl;
        this.title = title;
        this.lastChat = lastChat;
        this.lastChatElapsedTime = lastChatElapsedTime;
        this.unreadMsgNum = unreadMsgNum;
        this.maxPartyMemberNum = maxPartyMemberNum;
        this.currentPartyMemberNum = currentPartyMemberNum;
    }

    public String getFirstChatUserProfileImageUrl() {
        return firstChatUserProfileImageUrl;
    }

    public void setFirstChatUserProfileImageUrl(String firstChatUserProfileImageUrl) {
        this.firstChatUserProfileImageUrl = firstChatUserProfileImageUrl;
    }

    public String getSecondChatUserProfileImageUrl() {
        return secondChatUserProfileImageUrl;
    }

    public void setSecondChatUserProfileImageUrl(String secondChatUserProfileImageUrl) {
        this.secondChatUserProfileImageUrl = secondChatUserProfileImageUrl;
    }

    public String getThirdChatUserProfileImageUrl() {
        return thirdChatUserProfileImageUrl;
    }

    public void setThirdChatUserProfileImageUrl(String thirdChatUserProfileImageUrl) {
        this.thirdChatUserProfileImageUrl = thirdChatUserProfileImageUrl;
    }

    public String getPostThumbnailImageUrl() {
        return postThumbnailImageUrl;
    }

    public void setPostThumbnailImageUrl(String postThumbnailImageUrl) {
        this.postThumbnailImageUrl = postThumbnailImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastChat() {
        return lastChat;
    }

    public void setLastChat(String lastChat) {
        this.lastChat = lastChat;
    }

    public long getLastChatElapsedTime() {
        return lastChatElapsedTime;
    }

    public void setLastChatElapsedTime(long lastChatElapsedTime) {
        this.lastChatElapsedTime = lastChatElapsedTime;
    }

    public int getUnreadMsgNum() {
        return unreadMsgNum;
    }

    public void setUnreadMsgNum(int unreadMsgNum) {
        this.unreadMsgNum = unreadMsgNum;
    }

    public int getMaxPartyMemberNum() {
        return maxPartyMemberNum;
    }

    public void setMaxPartyMemberNum(int maxPartyMemberNum) {
        this.maxPartyMemberNum = maxPartyMemberNum;
    }

    public int getCurrentPartyMemberNum() {
        return currentPartyMemberNum;
    }

    public void setCurrentPartyMemberNum(int currentPartyMemberNum) {
        this.currentPartyMemberNum = currentPartyMemberNum;
    }
}

