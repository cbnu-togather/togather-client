package com.project.togather.retrofit.interfaceAPI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatAPI {
    @POST("raisehand/{groupBuyId}")
    Call<ResponseBody> raiseHand(@Path("groupBuyId") int postId);
}
