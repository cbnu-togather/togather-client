package com.project.togather.notification;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationInfoItem {
    private int id;
    @SerializedName("userProfileImgUrl")
    private String userProfileImgUrl;
    @SerializedName("groupBuyThumbnailUrl")
    private String groupBuyThumbnailUrl;
    @SerializedName("userName")
    private String userName;
    @SerializedName("groupBuyTitle")
    private String groupBuyTitle;
    private String notificationTime;
    private int headCount;
    private int currentCount;

    public long getElapsedTime() {
        long elapsedTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            Date now = new Date();

            String createdAtString = notificationTime.split("\\.")[0];
            Date createdAt = sdf.parse(createdAtString);
            elapsedTime = (now.getTime() - createdAt.getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return elapsedTime;
    }
}

