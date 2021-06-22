package com.example.foodapporderserver.Service;

import com.example.foodapporderserver.Model.MyRespone;
import com.example.foodapporderserver.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2woeJpo:APA91bGJfS4-zoENuASVuLcEXFMR-XPBeNVVhHWx5DQZN0bkHp85sFY6qFibkOdgHwjLh4i2UnfewEPP3DKBxxOXq2lY-VEYAi-BaxRQKSNeQBzk4uMhPaNldH8xLfFvbDSHMCAxkuqu"
            }

    )
    @POST("fcm/send")
    Call<MyRespone> sendNotification(@Body Sender body);
}
