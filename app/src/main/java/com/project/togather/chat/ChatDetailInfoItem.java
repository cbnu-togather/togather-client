package com.project.togather.chat;

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
public class ChatDetailInfoItem {
    private String userProfileImgUrl;
    private String userName;
    private String content;
    private String createdAt;
    private String chatRoomTitle;
    private boolean continuousMessage;
    private boolean myMessage;

    public long getElapsedTime() {
        long elapsedTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            Date now = new Date();

            String createdAtString = createdAt.split("\\.")[0];
            Date createdAt = sdf.parse(createdAtString);
            elapsedTime = (now.getTime() - createdAt.getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return elapsedTime;
    }
}

