package com.example.industrial;

import com.example.industrial.models.Area;
import com.example.industrial.models.Areas;
import com.example.industrial.models.Location;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.example.industrial.models.Sector;
import java.util.List;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

class APIClient {

//    private static final String BASE_URL = "http://192.168.1.151:5000/";
//    private static String BASE_URL = "http://192.168.1.7:5000";
    private static String BASE_URL = "http://10.0.2.2:5000";


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

interface APIInterface {
    int UPDATE_DELAY = 1;

    @GET("position")
    Observable<Location> getCurrentPosition();

    @GET("area/{id}")
    Observable<Area> getArea(@Path("id") int id);

    @GET("sector/{id}")
    Observable<Sector> getSector(@Path("id") int id);

    @GET("sector/{id}/machines")
    Observable<List<Machine>> getSectorMachines(@Path("id") int id);

    @GET("machine/{id}/data")
    Observable<List<MachineData>> getMachineData(@Path("id") int id);

    @GET("machine/{id}/data/update")
    Observable<MachineData> getMachineDataUpdate(@Path("id") int id);

    @POST("machine/{id}/{command}")
    void commandMachine(@Path("id") int id, @Path("command") String command);

}