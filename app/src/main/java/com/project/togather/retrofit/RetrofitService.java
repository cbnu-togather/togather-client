package com.project.togather.retrofit;

import com.google.gson.Gson;
import com.project.togather.utils.AuthInterceptor;
import com.project.togather.utils.TokenManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private Retrofit retrofit;
    private AuthInterceptor authInterceptor;

    public RetrofitService(TokenManager tokenManager) {
        authInterceptor = new AuthInterceptor(tokenManager);
        initializeRetrofit();
    }

    private void initializeRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}