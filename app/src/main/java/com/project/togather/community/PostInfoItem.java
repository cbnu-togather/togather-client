package com.project.togather.community;

public class PostInfoItem {
    private String postThumbnailImageUrl;
    private String tag;
    private String title;
    private String content;
    private String district;
    private long elapsedTime;
    private int likedCnt;

    public PostInfoItem(String postThumbnailImageUrl, String tag, String title, String content, String district, long elapsedTime, int likedCnt) {
        this.postThumbnailImageUrl = postThumbnailImageUrl;
        this.tag = tag;
        this.title = title;
        this.content = content;
        this.district = district;
        this.elapsedTime = elapsedTime;
        this.likedCnt = likedCnt;
    }

    public String getPostThumbnailImageUrl() {
        return postThumbnailImageUrl;
    }

    public void setPostThumbnailImageUrl(String postThumbnailImageUrl) {
        this.postThumbnailImageUrl = postThumbnailImageUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getLikedCnt() {
        return likedCnt;
    }

    public void setLikedCnt(int likedCnt) {
        this.likedCnt = likedCnt;
    }
}