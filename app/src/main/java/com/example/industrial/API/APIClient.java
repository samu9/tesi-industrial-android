package com.example.industrial.API;

import com.example.industrial.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.industrial.BuildConfig.API_KEY;
import static com.example.industrial.BuildConfig.BASE_URL;

public class APIClient {
    private final static String AUTHORIZATION_HEADER = "Authorization";
    private static Retrofit retrofitInstance = null;

    public static String getBaseUrl(){
        return BASE_URL;
    }

    public static Retrofit getInstance() {
        if (retrofitInstance == null) {

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder()
                            .addHeader(AUTHORIZATION_HEADER, "Basic " + API_KEY).build();
                    return chain.proceed(request);
                }
            });
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // serve a gestire la risposta di retrofit
                    .addConverterFactory(GsonConverterFactory.create()) // serializza e deserializza l'oggetto response
                    .client(httpClient.build())
                    .build();
        }
        return retrofitInstance;
    }
}

