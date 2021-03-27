package com.example.industrial.API;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

//    private static String BASE_URL = "http://10.0.2.2:5000";
    private static String BASE_URL = "http://207.154.240.44:5000";

    private static Retrofit retrofitInstance = null;

    public static String getBaseUrl(){
        return BASE_URL;
    }

    public static Retrofit getInstance() {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // serve a gestire la risposta di retrofit
                    .addConverterFactory(GsonConverterFactory.create()) // serializza e deserializza l'oggetto response
                    .build();
        }
        return retrofitInstance;
    }
}

