package com.project.togather.retrofit.interfaceAPI;

import androidx.annotation.Nullable;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CommunityAPI {

    @Multipart
    @POST("/community")
    Call<ResponseBody> createCommunityPost(
            @Query("title") String title,
            @Query("content") String content,
            @Query("latitude") float latitude,
            @Query("longitude") float longitude,
            @Query("address") String address,
            @Query("category") String category,
            @Part @Nullable MultipartBody.Part img
    );

    @POST("/community")
    Call<ResponseBody> createCommunityPostWithoutImg(
            @Query("title") String title,
            @Query("content") String content,
            @Query("latitude") float latitude,
            @Query("longitude") float longitude,
            @Query("address") String address,
            @Query("category") String category
    );
    @GET("/community/list")
    Call<ResponseBody> getCommunityPostList(@Query("Latitude") double latitude,
                                            @Query("Longitude") double longitude);
    @GET("community/{communityId}")
    Call<ResponseBody> getCommunityPostDetail(@Path("communityId") int postId);

    @POST("community/like")
    Call<ResponseBody> setCommunityPostLike(@Query("communityId") int postId);
    @DELETE("community/{communityId}")
    Call<ResponseBody> deleteCommunityPost(@Path("communityId") int postId);


}
