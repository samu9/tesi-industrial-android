package com.example.industrial;

import com.example.industrial.models.Area;
import com.example.industrial.models.Areas;
import com.example.industrial.models.Machine;
import com.example.industrial.models.Sector;
import java.util.List;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

class APIClient {

    // TODO Barros 1 - utilizzata questa BASE_URL_FAKE per creare un servizio online che mi restituisca una Position e una lista di area, perché non avevo il tuo backend

    private static final String BASE_URL_FAKE = "https://run.mocky.io/v3/";
//    private static final String BASE_URL = "http://192.168.1.151:5000/";
    private static String BASE_URL = "http://192.168.1.7:5000";


    private static Retrofit retrofitInstance = null;

    public static Retrofit getInstance() {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder()
                    // TODO Barros 2 - dopo che hai visto come funziona, sostituisci con BASE_URL
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // serve a gestire la risposta di retrofit
                    .addConverterFactory(GsonConverterFactory.create()) // serializza e deserializza l'oggetto response
                    .build();
        }
        return retrofitInstance;
    }
}

interface APIInterface {

    // TODO Barros 3 -Chiamate fake che mi sono creato

    // Request https://run.mocky.io/v3/b310dee7-aa9d-4eae-8219-2142c2b7f6c8
    // Response
    //      {
    //          "area_id":4,
    //          "sector_id":2
    //      }
    @GET("b310dee7-aa9d-4eae-8219-2142c2b7f6c8")
    Observable<DataService.Position> getFakeCurrentPosition();

    // TODO Barros 4 - Per comodità ho creato una funziona che restituisca una lista di area, mentre la tua gli passa l'id per ottenere un'area specifica

    // Request https://run.mocky.io/v3/68f76f55-75b8-4605-bb8e-30a7ffec15d3
    // Response
    //      { "areas": [
    //          { "id":"4", "name":"area 4" },
    //          { "id":"5", "name":"area 5" }
    //      ]}
    @GET("68f76f55-75b8-4605-bb8e-30a7ffec15d3")
    Observable<Areas> getFakeAreas();


    // TODO Barros 5 - Le tue chiamate

    @GET("position")
    Observable<DataService.Position> getCurrentPosition();

    @GET("area/{id}")
    Observable<Area> getArea(@Path("id") int id);

    @GET("sector/{id}")
    Observable<Sector> getSector(@Path("id") int id);

    @GET("sector/{id}/machines")
    Observable<List<Machine>> getSectorMachines(@Path("id") int id);

    @GET("machine/{id}/data")
    Observable<Machine> getMachineData(@Path("id") String id);

//    @GET("/machine/{id}/{control}")
//    Observable<> getMachineData(@Path("id") String id, @Path("control") String control);
}