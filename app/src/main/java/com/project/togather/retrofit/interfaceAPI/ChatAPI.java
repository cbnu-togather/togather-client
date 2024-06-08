package com.project.togather.retrofit.interfaceAPI;

import com.project.togather.chat.ChatDetailInfoItem;
import com.project.togather.chat.ChatInfoItem;
import com.project.togather.chat.ChatMessageRequest;
import com.project.togather.notification.NotificationInfoItem;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ChatAPI {
    @POST("raisehand/{groupBuyId}")
    Call<ResponseBody> raiseHand(@Path("groupBuyId") int postId);
    @PUT("acceptWaiting/{waitingId}")
    Call<ResponseBody> acceptWaiting(@Path("waitingId") int waitingId);
    @DELETE("declineWaiting/{waitingId}")
    Call<ResponseBody> deleteNotification(@Path("waitingId") int waitingId);

    @GET("notifications")
    Call<List<NotificationInfoItem>> getNotificationList();
    @GET("chatrooms")
    Call<List<ChatInfoItem>> getChatRoomList();
    @GET("chatrooms/{chatRoomId}/records")
    Call<List<ChatDetailInfoItem>> getChatRoomDetails(@Path("chatRoomId") int chatRoomId);
    @PUT("chatrooms/{chatRoomId}/messages")
    Call<ResponseBody> sendMessage(@Path("chatRoomId") int chatRoomId, @Body ChatMessageRequest chatMessageRequest);
    @DELETE("chatrooms/{chatRoomId}/leave")
    Call<ResponseBody> leaveChatRoom(@Path("chatRoomId") int chatRoomId);

}
