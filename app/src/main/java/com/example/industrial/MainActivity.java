package com.example.industrial;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.industrial.models.Area;
import com.example.industrial.models.Machine;
import com.example.industrial.models.Sector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int area_id = 1;
    private static final int sector_id = 1;

    private Area area;
    private Sector sector;

    private static final String BASE_URL = "http://192.168.1.151:5000";
    ArrayList<Machine> machines;
    String[] footerTexts = new String[2];

    TextView footer;
    TextView timestamp;



    DataService service = new DataService(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        machines = new ArrayList<>();

        footer = findViewById(R.id.footer);

        timestamp = findViewById(R.id.timestamp);
        timestamp.setText("timestamp");

        service.getArea(area_id, new DataService.VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(Object response) {
                Area area = (Area)response;
                footerTexts[0] = area.getName();

                service.getSector(sector_id, new DataService.VolleyResponseListener() {
                    @Override
                    public void onError(String message) {

                    }

                    @Override
                    public void onResponse(Object response) {
                        Sector sector = (Sector) response;
                        footerTexts[1] = sector.getName();
                        footer.setText("Area: " + footerTexts[0]+ " - Settore: " + footerTexts[1]);
                    }
                });
            }
        });

        service.getMachines(sector_id, new DataService.VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(Object response) {
                machines = (ArrayList<Machine>) response;
                Machine m = machines.get(0);

                MachineFragment fragment = MachineFragment.newInstance(m.getName(), m.getId());
                getSupportFragmentManager().beginTransaction().replace(R.id.body_layout, fragment).commit();
            }
        });

    }





}

