package com.project.togather.retrofit.interfaceAPI;

import androidx.annotation.Nullable;

import com.project.togather.community.PostInfoItem;

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
    Call<List<PostInfoItem>> getCommunityPostList(@Query("Latitude") double latitude,
                                                  @Query("Longitude") double longitude);
    @GET("community/{communityId}")
    Call<ResponseBody> getCommunityPostDetail(@Path("communityId") int postId);

    @POST("community/like")
    Call<ResponseBody> setCommunityPostLike(@Query("communityId") int postId);
    @DELETE("community/{communityId}")
    Call<ResponseBody> deleteCommunityPost(@Path("communityId") int postId);

    @Multipart
    @PUT("community/{communityId}")
    Call<ResponseBody> updateCommunityPost(
            @Path("communityId") int postId,
            @Query("title") String title,
            @Query("content") String content,
            @Query("category") String category,
            @Part @Nullable MultipartBody.Part img
    );

    @PUT("community/{communityId}")
    Call<ResponseBody> updateCommunityPostWithoutImg(
            @Path("communityId") int postId,
            @Query("title") String title,
            @Query("content") String content,
            @Query("category") String category
    );
    @Multipart
    @POST("comment/{communityId}")
    Call<ResponseBody> postComment(
            @Path("communityId") int postId,
            @Query("content") String content,
            @Part @Nullable MultipartBody.Part img
    );
    @POST("comment/{communityId}")
    Call<ResponseBody> postCommentWithoutImg(
            @Path("communityId") int postId,
            @Query("content") String content
    );
    @DELETE("comment/{commentId}")
    Call<ResponseBody> deleteComment(
            @Path("commentId") int commentId
    );

    @Multipart
    @PUT("comment/{commentId}")
    Call<ResponseBody> updateComment(
            @Path("commentId") int commentId,
            @Query("content") String content,
            @Part @Nullable MultipartBody.Part img
    );

    @PUT("comment/{commentId}")
    Call<ResponseBody> updateCommentWithoutImg(
            @Path("commentId") int commentId,
            @Query("content") String content
    );




}
