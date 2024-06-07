package com.project.togather.retrofit.interfaceAPI;

import com.project.togather.notification.NotificationInfoItem;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ChatAPI {
    @POST("raisehand/{groupBuyId}")
    Call<ResponseBody> raiseHand(@Path("groupBuyId") int postId);
    @PUT("acceptWaiting/{waitingId}")
    Call<ResponseBody> acceptWaiting(@Path("waitingId") int waitingId);

    @GET("notifications")
    Call<List<NotificationInfoItem>> getNotificationList();
}
