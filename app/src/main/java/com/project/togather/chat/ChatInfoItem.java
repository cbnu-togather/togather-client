package com.project.togather.chat;

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
public class ChatInfoItem {
    @SerializedName("id")
    private int id;
    @SerializedName("userProfileImgUrls")
    private String userProfileImgUrls[];
    @SerializedName("groupBuyThumbnailUrl")
    private String groupBuyThumbnailUrl;
    @SerializedName("chatRoomTitle")
    private String chatRoomTitle;
    @SerializedName("participantCount")
    private int participantCount;
    @SerializedName("lastMessage")
    private String lastMessage;
    @SerializedName("lastMessageTime")
    private String lastMessageTime;
    @SerializedName("unreadMessageCount")
    private int unreadMessageCount;

    public long getElapsedTime() {
        long elapsedTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            Date now = new Date();
            if (lastMessageTime != null) {
                String createdAtString = lastMessageTime.split("\\.")[0];
                Date createdAt = sdf.parse(createdAtString);
                elapsedTime = (now.getTime() - createdAt.getTime()) / 1000;
            } else {
                return -1;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return elapsedTime;
    }
}

