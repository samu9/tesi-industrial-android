package com.example.industrial;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.industrial.adapters.MachineDataAdapter;
import com.example.industrial.menu.MenuActivity;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MachineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MachineFragment extends BaseFragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String MACHINE = "machine";
    protected static final String MENU_KEY = "menu_key";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private Machine machine;

    ArrayList<MachineData> machineData;

    TextView statusView, idView, nameView, value3, dangerText;

    LineChart chart2;
    BarChart chart1;

    DataService service = new DataService(getContext());

    APIInterface apiService;

    List<BarEntry> entries1 = new ArrayList<>();
    List<Entry> entries2 = new ArrayList<>();

    int dataCounter = 0;

    boolean inDanger = false;

    public MachineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param machine Parameter 1.
     * @return A new instance of fragment MachineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MachineFragment newInstance(Serializable machine, int menu) {
        MachineFragment fragment = new MachineFragment();
        Bundle args = new Bundle();
        args.putSerializable(MACHINE, machine);
        args.putInt(MENU_KEY, menu);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            machine = (Machine) getArguments().getSerializable(MACHINE);

        }

        machineData = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_machine, container, false);

        apiService = APIClient.getInstance().create(APIInterface.class);

        nameView = v.findViewById(R.id.machine_name);
        idView = v.findViewById(R.id.machine_id);
        statusView = v.findViewById(R.id.machine_status);

        chart1 = v.findViewById(R.id.value1_chart);
        chart2 = v.findViewById(R.id.value2_chart);
        chartsSetUp();

        value3 = v.findViewById(R.id.value3_number);

        dangerText = v.findViewById(R.id.danger_text);

        nameView.setText(machine.getName());
        idView.setText(Integer.toString(machine.getId()));
        statusView.setText(machine.getStatus());

//        apiService.getMachineData(machine.getId())
////                .repeatWhen(completed -> completed.delay(APIInterface.UPDATE_DELAY, TimeUnit.SECONDS))
//                .subscribe(machineDataResponse -> {
////                    dataCounter = machineDataResponse.size();
////                    machineData.addAll((ArrayList<MachineData>) machineDataResponse);
////
////                for(int i = 0; i < machineData.size(); i++){
////                    entries1.add(new BarEntry(i, machineData.get(i).getValues()[0]));
////                    entries2.add(new Entry(i, machineData.get(i).getValues()[1]));
////                }
//
//                chartsUpdate();
//
//                });

        startGetData();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

//        startGetData();
    }


    @Override
    public void onSingleTapUp() {
        if (getArguments() != null) {
            int menu = getArguments().getInt(MENU_KEY, MENU_DEFAULT_VALUE);
            Log.i("MachineFragment", "TAP - menu id: " + menu);
            if (menu != MENU_DEFAULT_VALUE) {
                Intent intent = new Intent(getActivity(), MenuActivity.class);
                intent.putExtra(MENU_KEY, menu);
                intent.putExtra(MACHINE, machine.getId());
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("BaseFragment", "ActivityResult: " + requestCode + " - " + resultCode);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            final int id = data.getIntExtra(MenuActivity.EXTRA_MENU_ITEM_ID_KEY,
                    MenuActivity.EXTRA_MENU_ITEM_DEFAULT_VALUE);
            String selectedOption = "";
            switch (id) {
                case R.id.start:
                    selectedOption = getString(R.string.start);
                    updateMachineStatus(Machine.START);
                    if(!started){
                        started = true;
                        startGetData();
                    }
                    dangerText.setVisibility(View.GONE);
                    break;
                case R.id.pause:
                    updateMachineStatus(Machine.PAUSE);
                    started = false;
                    selectedOption = getString(R.string.pause);
                    break;
                case R.id.stop:
                    selectedOption = getString(R.string.stop);
                    updateMachineStatus(Machine.STOP);
                    dangerText.setVisibility(View.VISIBLE);
                    break;

            }
            Toast.makeText(getActivity(), selectedOption + " option selected.", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    private void updateMachineStatus(String status){
        machine.setStatus(status);
        statusView.setText(status);
    }

    private void startGetData(){
                apiService.getMachineDataUpdate(machine.getId())
//                .delay(APIInterface.UPDATE_DELAY, TimeUnit.SECONDS)
                .repeatWhen(completed ->  completed.delay(1000, TimeUnit.MILLISECONDS))
                .subscribe(machineDataUpdate -> {
                    Log.i("data", Integer.toString(machine.getId()));
                    if(!machine.checkSpeed(machineDataUpdate.getSpeed())){
                        Log.i("DANGER", "speed: " + machineDataUpdate.getSpeed());
//                        goToDangerMode();

                    }

                    if(!machine.checkEfficiency(machineDataUpdate.getEfficiency())){
                        Log.i("DANGER", "efficiency: " + machineDataUpdate.getEfficiency());
//                        goToDangerMode();
                    }

                    if(!machine.checkTemp(machineDataUpdate.getTemp())){
                        Log.i("DANGER", "temp: " + machineDataUpdate.getTemp());
                        value3.setTextColor(Color.RED);
                        inDanger = true;
//                        goToDangerMode();
                    }

                    if(dataCounter > 10){
                        entries1.remove(0);
                        entries2.remove(0);
                        machineData.remove(0);
                    }

                    entries1.add(new BarEntry(dataCounter, machineDataUpdate.getValues()[0]));
                    entries2.add(new Entry(dataCounter, machineDataUpdate.getValues()[1]));
                    machineData.add(machineDataUpdate);

                    dataCounter++;
                    chartsUpdate();

                    value3.setText(Integer.toString(machineDataUpdate.getValues()[2]));
                });
    }

    private void startMachine(){
        service.controlMachine(machine.getId(), DataService.Control.START, new DataService.VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(Object response) {
                machine.setStatus("RUN");
                statusView.setText(machine.getStatus());
            }
        });
    }

    private void chartsUpdate(){
        BarDataSet dataSet1 = new BarDataSet(entries1, "data");
        dataSet1.setColor(Color.WHITE);
        dataSet1.setDrawValues(false);

        BarData barData1 = new BarData(dataSet1);

        LineDataSet dataSet2 = new LineDataSet(entries2, "data");
        dataSet2.setColor(Color.WHITE);
        dataSet2.setDrawCircles(false);
        dataSet2.setDrawValues(false);

        LineData lineData2 = new LineData(dataSet2);

        chart1.setData(barData1);
        chart1.invalidate();

        chart2.setData(lineData2);
        chart2.invalidate();
    }

    private void chartsSetUp(){
        XAxis xAxis;
        YAxis axisRight, axisLeft;

        // chart 1 - BAR CHART
        chart1.setTouchEnabled(false);
        chart1.setDrawBorders(false);
        chart1.setDrawMarkers(false);
        chart1.setDescription(null);

        xAxis = chart1.getXAxis();
        axisRight = chart1.getAxisRight();
        axisLeft = chart1.getAxisLeft();
        xAxis.setDrawLabels(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        axisRight.setDrawAxisLine(false);
        axisRight.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setDrawGridLines(true);
        axisLeft.setDrawLabels(true);
        axisLeft.setTextColor(Color.WHITE);
        axisLeft.setAxisMaximum(5000);
        axisLeft.setAxisMinimum(0);


        // chart 2 - LINE CHART
        chart2.setTouchEnabled(false);
        chart2.setDrawBorders(false);
        chart2.setDrawMarkers(false);
        chart2.setDescription(null);

        xAxis = chart2.getXAxis();
        axisRight = chart2.getAxisRight();
        axisLeft = chart2.getAxisLeft();
        xAxis.setDrawLabels(false);

        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);


        axisRight.setDrawLabels(true);
        axisRight.setDrawAxisLine(false);
        axisRight.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setDrawGridLines(true);
        axisLeft.setDrawLabels(true);
        axisLeft.setTextColor(Color.WHITE);

        axisLeft.setAxisMaximum(100);
        axisLeft.setAxisMinimum(0);

    }

    private void goToDangerMode(){
        Intent intent = new Intent(getActivity(), DangerActivity.class);
        getActivity().finish();
        startActivity(intent);
    }

}