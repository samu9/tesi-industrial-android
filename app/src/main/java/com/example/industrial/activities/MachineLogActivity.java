package com.example.industrial.activities;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.industrial.API.APIClient;
import com.example.industrial.API.APIInterface;
import com.example.industrial.R;
import com.example.industrial.adapters.MachineLogAdapter;
import com.example.industrial.models.MachineLog;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MachineLogActivity extends BaseActivity {
    public static final String MACHINE_ID_EXTRA = "machine id";

    private int machineId;

    private MachineLogAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<MachineLog> logs;

    private APIInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_log);

        machineId = getIntent().getIntExtra(MACHINE_ID_EXTRA, -1);

        logs = new ArrayList<MachineLog>();

        recyclerView = findViewById(R.id.log_recyclerView);
        adapter = new MachineLogAdapter(logs);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = APIClient.getInstance().create(APIInterface.class);


        apiService.getMachineLogs(machineId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineLogs -> {

                    adapter.notifyDataSetChanged();

                    Log.d(getClass().getName() + " " + machineId,"logs: " + logs.size());
            logs.addAll(machineLogs);

        });

    }
}