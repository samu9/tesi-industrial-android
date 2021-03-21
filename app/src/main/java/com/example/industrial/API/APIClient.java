package com.example.industrial.API;

import com.example.industrial.menu.APIResult;
import com.example.industrial.models.Area;
import com.example.industrial.models.Areas;
import com.example.industrial.models.Location;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.example.industrial.models.MachineLog;
import com.example.industrial.models.Sector;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class APIClient {

//    private static String BASE_URL = "http://10.0.2.2:5000";
    private static String BASE_URL = "http://207.154.240.44:5000";


    private static Retrofit retrofitInstance = null;

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
