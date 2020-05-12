package umn.ac.id.chatapp.Fragments;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import umn.ac.id.chatapp.Notification.MyResponse;
import umn.ac.id.chatapp.Notification.Sender;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAY4EGWus:APA91bGNwquCsBNTCdw_rkaqzPfBNoxwRlu9bxpYP46np_zGFQ9WulcSIPGlGz-Varqtmey-68ym3YUI9qadavk8dXjfKAl2eWIC63TYJTvYr7iaakTXHDPUDMvzlJV4qPDmKi2zsVIy"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
