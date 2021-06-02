package com.finaltest.Remote;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import com.finaltest.Model.MyResponse;
import com.finaltest.Model.Sender;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2woeJpo:APA91bGJfS4-zoENuASVuLcEXFMR-XPBeNVVhHWx5DQZN0bkHp85sFY6qFibkOdgHwjLh4i2UnfewEPP3DKBxxOXq2lY-VEYAi-BaxRQKSNeQBzk4uMhPaNldH8xLfFvbDSHMCAxkuqu"
            }

    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
