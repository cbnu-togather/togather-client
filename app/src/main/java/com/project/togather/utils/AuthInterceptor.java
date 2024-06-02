package com.project.togather.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();

        if (!"no-auth".equals(request.header("Authorization-Type"))) {
            String token = tokenManager.getToken();
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        requestBuilder.removeHeader("Authorization-Type");

        return chain.proceed(requestBuilder.build());
    }
}
