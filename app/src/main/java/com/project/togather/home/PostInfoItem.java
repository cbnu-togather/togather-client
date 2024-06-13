package com.project.togather.home;

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
public class PostInfoItem {
    private int id;
    private String img;
    private String title;
    private String category;
    private String createdAt;
    private int headCount;
    private int currentCount;
    private boolean liked;
    private int likes;
    private boolean completed;

    public long getElapsedTime() {
        long elapsedTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREAN);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            Date now = new Date();
            if (createdAt != null) {
                String createdAtString = createdAt.split("\\.")[0];
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