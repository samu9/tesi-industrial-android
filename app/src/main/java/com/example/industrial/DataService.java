package com.example.industrial;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.industrial.models.Area;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.example.industrial.models.Sector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataService {

    public interface Control{
        String START = "START";
        String STOP = "STOP";
        String HALT = "HALT";
        String PAUSE = "PAUSE";

    }

    public class Position{
        int area_id;
        int sector_id;

        public Position(int area_id, int sector_id) {
            this.area_id = area_id;
            this.sector_id = sector_id;
        }
    }
//    private static String BASE_URL = "http://192.168.1.151:5000";
    private static String BASE_URL = "http://192.168.1.7:5000";
    private Context context;

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(Object response);
    }

    public DataService(Context context) {
        this.context = context;
    }

    public void getCurrentPosition(VolleyResponseListener responseListener){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                BASE_URL + "/position", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseListener.onResponse(new Position(response.getInt("area_id"), response.getInt("sector_id")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void getArea(int area_id, VolleyResponseListener responseListener){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                BASE_URL + "/area/" + area_id,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Area area = new Area(response.getInt("id"),
                            response.getString("name"), response.getInt("sectors"));
                    responseListener.onResponse(area);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


    public void getSector(int sector_id, VolleyResponseListener responseListener){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                BASE_URL + "/sector/" + sector_id,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Sector sector = new Sector(response.getInt("id"),
                            response.getString("name"), response.getInt("machines"));

                    responseListener.onResponse(sector);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.onError(error.getMessage());
            }
        });
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void getSectorMachines(int sector_id, VolleyResponseListener responseListener){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                BASE_URL + "/sector/" + sector_id + "/machines",
                null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        List<Machine> machines = new ArrayList<>();

                        for(int i = 0; i < response.length(); i++){

                            try {
                                JSONObject machineObject = response.getJSONObject(i);

                                Machine machine = new Machine(machineObject.getString("name"),
                                        machineObject.getInt("id"), 1, machineObject.getString("status"));
                                machines.add(machine);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        responseListener.onResponse(machines);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        VolleySingleton.getInstance(context).addToRequestQueue(jsonArrayRequest);
    }

    public void getMachineData(int machine_id, VolleyResponseListener responseListener){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                BASE_URL + "/machine/" + machine_id + "/data", null,
                response -> {
                    List<MachineData> data = new ArrayList<>();
                    for(int i = 0; i < response.length(); i++){
                        try {
                            JSONObject object = response.getJSONObject(i);

                            JSONArray objectValues = object.getJSONArray("values");
                            int[] values = new int[objectValues.length()];
                            for (int j = 0; j < objectValues.length(); j++){
                                values[j] = (int)objectValues.get(j);
                            }

                            data.add(new MachineData(object.getInt("machine_id"), values,
                            object.getString("timestamp")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    int[] temp = new int[3];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
//                    data.add(new MachineData(0, temp, ""));
                    responseListener.onResponse(data);
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.onError(error.getMessage());
            }
        });

        VolleySingleton.getInstance(context).addToRequestQueue(jsonArrayRequest);
    }

    public void controlMachine(int machine_id, String control, VolleyResponseListener responseListener){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                BASE_URL + "/machine/" + machine_id + "/" + control, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
}
