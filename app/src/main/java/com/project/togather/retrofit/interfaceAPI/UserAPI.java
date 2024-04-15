package com.project.togather.retrofit.interfaceAPI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserAPI {
    @GET("/user/phone")
    Call<ResponseBody> checkPhoneNumber(@Query("phone") String phoneNumber);
}