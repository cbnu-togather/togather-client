package com.project.togather.retrofit.interfaceAPI;

import androidx.annotation.Nullable;

import com.project.togather.home.PostInfoItem;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @GET("groupbuy/list")
    Call<List<PostInfoItem>> getRecruitmentPostList(
            @Query("Latitude") double latitude,
            @Query("Longitude") double longitude,
            @Query("distance") int distance
    );

    @GET("groupbuy/{groupBuyId}")
    Call<ResponseBody> getRecruitmentPostDetail(@Path("groupBuyId") int postId);

    @POST("groupbuy/like")
    Call<ResponseBody> setRecruitmentPostLike(@Query("groupBuyId") int postId);
    @DELETE("groupbuy/{groupBuyId}")
    Call<ResponseBody> deleteRecruitmentPost(@Path("groupBuyId") int postId);
    @Multipart
    @PUT("/groupbuy/{groupBuyId}")
    Call<ResponseBody> updateRecruitmentPost(
            @Path("groupBuyId") int postId,
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


    @PUT("/groupbuy/{groupBuyId}")
    Call<ResponseBody> updateRecruitmentPostWithoutImg(
            @Path("groupBuyId") int postId,
            @Query("title") String title,
            @Query("content") String content,
            @Query("latitude") float latitude,
            @Query("longitude") float longitude,
            @Query("headCount") int headCount,
            @Query("address") String address,
            @Query("spotName") String spotName,
            @Query("category") String category

    );

    @PUT("groupbuy/{groupBuyId}/close")
    Call<ResponseBody> closeRecruitment(@Path("groupBuyId") int postId);

}
