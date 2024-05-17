package com.project.togather.retrofit.interfaceAPI;

import androidx.annotation.Nullable;

import com.project.togather.createPost.recruitment.RecruitmentPostItem;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RecruitmentAPI {
    @Multipart
    @POST("/groupbuy")
    Call<ResponseBody> createRecruitmentPost(
            @Query("title") String title,
            @Query("content") String content,
            @Query("latitude") float latitude,
            @Query("longitude") float longitude,
            @Query("headCount") int headCount,
            @Query("address") String address,
            @Query("spotName") String spotName,
            @Query("category") String category,
            @Part @Nullable MultipartBody.Part img
    );
    @POST("/groupbuy")
    Call<ResponseBody> createRecruitmentPostWithoutImg(
            @Query("title") String title,
            @Query("content") String content,
            @Query("latitude") float latitude,
            @Query("longitude") float longitude,
            @Query("headCount") int headCount,
            @Query("address") String address,
            @Query("spotName") String spotName,
            @Query("category") String category
    );

}
