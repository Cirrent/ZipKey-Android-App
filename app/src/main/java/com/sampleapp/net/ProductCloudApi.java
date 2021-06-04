package com.sampleapp.net;

import com.sampleapp.net.model.LoginResponse;
import com.sampleapp.net.model.Token;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProductCloudApi {
    @POST("api/login")
    Call<LoginResponse> login(@Query("username") String username, @Query("password") String password);

    @GET("cloud/token/analytics")
    Call<Token> getAnalyticsToken(@Header("Authorization") String authorization);
}
