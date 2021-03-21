package com.example.industrial.API;

import com.example.industrial.menu.APIResult;
import com.example.industrial.models.Area;
import com.example.industrial.models.Location;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.example.industrial.models.MachineLog;
import com.example.industrial.models.Sector;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIInterface {
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

    @GET("machine/{id}/log")
    Observable<ArrayList<MachineLog>> getMachineLogs(@Path("id") int id);

    @POST("machine/{id}/danger")
    Observable<APIResult> setMachineDanger(@Path("id") int id);

    @POST("machine/{id}/command/{command}")
    Observable<APIResult> commandMachine(@Path("id") int id, @Path("command") String command);

}
