package com.example.industrial;

import com.example.industrial.models.Area;
import com.example.industrial.models.Machine;
import com.example.industrial.models.Sector;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

class APIClient {
    private static final String BASE_URL = "http://192.168.1.151:5000";
    static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
    }
}

interface APIInterface {
    @GET("/position")
    Observable<DataService.Position> getCurrentPosition();

    @GET("/area/{id}")
    Observable<Area> getArea(@Path("id") String id);

    @GET("/sector/{id}")
    Observable<Sector> getSector(@Path("id") String id);

    @GET("/sector/{id}/machines")
    Observable<List<Machine>> getSectorMachines(@Path("id") String id);

    @GET("/machine/{id}/data")
    Observable<Machine> getMachineData(@Path("id") String id);

//    @GET("/machine/{id}/{control}")
//    Observable<> getMachineData(@Path("id") String id, @Path("control") String control);
}