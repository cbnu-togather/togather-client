package com.project.togather.home;

public class PostInfoItem {
    private String title;
    private String category;
    private long elapsedTime;
    private int currentPartyMemberNum;
    private boolean likedState;
    private int likedCnt;

    public PostInfoItem(String title, String category, long elapsedTime, int currentPartyMemberNum, boolean likedState, int likedCnt) {
        this.title = title;
        this.category = category;
        this.elapsedTime = elapsedTime;
        this.currentPartyMemberNum = currentPartyMemberNum;
        this.likedState = likedState;
        this.likedCnt = likedCnt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getCurrentPartyMemberNum() {
        return currentPartyMemberNum;
    }

    public void setCurrentPartyMemberNum(int currentPartyMemberNum) {
        this.currentPartyMemberNum = currentPartyMemberNum;
    }

    public boolean getLikedState() {
        return likedState;
    }

    public void setLikedState(boolean likedState) {
        this.likedState = likedState;
    }

    public int getLikedCnt() {
        return likedCnt;
    }

    public void setLikedCnt(int likedCnt) {
        this.likedCnt = likedCnt;
    }
}