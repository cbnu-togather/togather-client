package com.project.togather.retrofit.interfaceAPI;

import androidx.annotation.Nullable;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UserAPI {
    @GET("/user/phone")
    Call<ResponseBody> checkPhoneNumber(@Query("phone") String phone);
    @GET("/user/doublecheck")
    Call<Boolean> doubleCheckUserName(@Query("name") String name);
    @POST("/user")
    Call<ResponseBody> signUp(
            @Query("phone") String phone,
            @Query("name") String name
    );
    @POST("/login")
    Call<ResponseBody> login(@Query("phone") String phone);
    @DELETE("/user")
    @Headers("accept: application/json")
    Call<Void> deleteUser();
    @GET("/user")
    @Headers("accept: application/json")
    Call<ResponseBody> getUserInfo();
    @Multipart
    @POST("/user/update")
    Call<ResponseBody> updateUserProfile(
            @Query("name") String name,
            @Part @Nullable MultipartBody.Part img
            );
    @POST("/user/update")
    Call<ResponseBody> updateUserProfileWithoutImg(@Query("name") String name);

}
