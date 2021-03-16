package com.example.industrial;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.industrial.menu.MenuActivity;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.example.industrial.models.MachineValue;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.schedulers.Schedulers;

import static java.lang.Thread.currentThread;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MachineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MachineFragment extends BaseFragment {

    // TODO: Rename parameter arguments, choose names that match
    protected static final String MACHINE = "machine";
    protected static final String MACHINE_DATA = "machine data";
    protected static final String MENU_KEY = "menu_key";
    protected static final String IN_DANGER_KEY = "in danger";

    private static final int MAX_DATA_BARS = 10;
    private static final int DATA_UPDATE_DELAY = 2000;

    private static boolean menuOpened = false;

 
    private Machine machine;

    ArrayList<MachineData> machineData;

    TextView statusView, idView, nameView, tempValue, dangerText;

    LineChart efficiencyChart;
    BarChart rpmChart;

    APIInterface apiService;

    List<BarEntry> rpmEntries = new ArrayList<>();
    List<Entry> efficiencyEntries = new ArrayList<>();


    int dataCounter = 0;

    boolean started = false;
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
    public static MachineFragment newInstance(Serializable machine, @Nullable int menu, @Nullable Serializable machineData, boolean inDanger) {
        MachineFragment fragment = new MachineFragment();
        Bundle args = new Bundle();
        args.putSerializable(MACHINE, machine);
        args.putInt(MENU_KEY, menu);
        args.putSerializable(MACHINE_DATA, machineData);
        args.putBoolean(IN_DANGER_KEY, inDanger);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = APIClient.getInstance().create(APIInterface.class);

        if (getArguments() != null) {
            machine = (Machine) getArguments().getSerializable(MACHINE);
            inDanger = getArguments().getBoolean(IN_DANGER_KEY);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_machine, container, false);

        View headerSeparator = v.findViewById(R.id.header_separator);
//        headerSeparator.setBackgroundColor(inDanger? getResources().getColor(R.color.holo_red) : Color.WHITE);

        nameView = v.findViewById(R.id.machine_name);
        idView = v.findViewById(R.id.machine_id);
        statusView = v.findViewById(R.id.machine_status);

        rpmChart = v.findViewById(R.id.value1_chart);
        efficiencyChart = v.findViewById(R.id.value2_chart);
        tempValue = v.findViewById(R.id.value3_number);

        chartsSetUp();

        nameView.setText(machine.getName());
        idView.setText(Integer.toString(machine.getId()));
        statusView.setText(machine.getStatus());

        dangerText = v.findViewById(R.id.danger_text);
        if(inDanger){
            dangerText.setVisibility(View.VISIBLE);
        }

        machineData = new ArrayList<>();
        ArrayList<MachineData> extraMachineData = (ArrayList<MachineData>) getArguments().getSerializable(MACHINE_DATA);
        if(extraMachineData != null){
            Log.i(getClass().getName(), "extraMachineData");
            for(MachineData data: extraMachineData){
                addData(data);
            }
        }

        if(getMachineStatus().equals(Machine.START)){
            Log.i(getClass().getName(),"machine " + machine.getId() + " status: " + Machine.START + " - starting");
            started = true;
            startGetData();
        }

        return v;
    }


    @Override
    public void onSingleTapUp() {
        if (getArguments() != null) {
            int menu = getArguments().getInt(MENU_KEY, MENU_DEFAULT_VALUE);
            Log.i(getClass().getName(), "TAP - menu id: " + menu);
            if (menu != MENU_DEFAULT_VALUE) {
                menuOpened = true;
                Intent intent = new Intent(getActivity(), MenuActivity.class);
                intent.putExtra(MENU_KEY, menu);
                intent.putExtra(MACHINE, machine.getId());
                intent.putExtra(MenuActivity.EXTRA_MACHINE_STATUS_KEY, getMachineStatus());
                startActivityForResult(intent, machine.getId());
            }
        }
    }

    @Override
    public void onPause() {
        if(!menuOpened && inDanger){
            pauseData();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(getClass().getName() + " " + getMachineId(), "onResume");
        if(!menuOpened && machine.getStatus() == Machine.START && !started){
            resumeData();
        }
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getClass().getName() + " " + machine.getId(), "ActivityResult: " + requestCode + " - " + resultCode);

        if (requestCode == machine.getId() && resultCode == MenuActivity.RESULT_MENU && data != null) {
            final int id = data.getIntExtra(MenuActivity.EXTRA_MENU_ITEM_ID_KEY,
                    MenuActivity.EXTRA_MENU_ITEM_DEFAULT_VALUE);

            menuOpened = false;
            String message = null;

            switch (id) {
                case R.id.start:
                    message = "Starting " + machine.getName();
                    updateMachineStatus(Machine.START);
                    sendMachineCommand(Machine.START);
                    break;
                case R.id.pause:
                    message = "Pausing " + machine.getName();
                    updateMachineStatus(Machine.PAUSE);
                    pauseData();

                    break;
                case R.id.resume:
                    message = "Resuming " + machine.getName();
                    updateMachineStatus(Machine.START);
                    resumeData();

                    break;
                case R.id.stop:
                    message = "Stopping " + machine.getName();
                    updateMachineStatus(Machine.STOP);
                    sendMachineCommand(Machine.STOP);

                    break;
                case R.id.go_to_danger:
                    Log.i(getClass().getName(),"Go to danger");
//                    goToDangerMode();
                    sendDangerMode();
                    break;
                case R.id.halt:
                    resolveDanger();
                    break;
            }
            if(message != null){
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == machine.getId() && resultCode == DangerActivity.RESULT_DANGER){
            inDanger = false;
        }
    }

    private void sendMachineCommand(String command){
        apiService.commandMachine(machine.getId(), command)
                .subscribe(apiResult -> {
                    if(apiResult.getResult()){
                        switch (command) {
                            case Machine.START:
                                if (!started) {
                                    started = true;
                                    startGetData();
                                }
                                break;
                            case Machine.PAUSE:
                                pauseData();
                                break;
                            case Machine.STOP:
                                stopData();
                                break;
                            default:
                                break;
                        }
                    }
                    Log.i(getClass().getName(), "sendMachineCommand [" + command + "]: " + apiResult.getResult());
                }, throwable -> {
                    Log.e(getClass().getName(),Log.getStackTraceString(throwable));
                });
    }

    public String getMachineStatus(){
        return machine.getStatus();
    }

    public int getMachineId() { return machine.getId(); }

    private void updateMachineStatus(String status){
        machine.setStatus(status);
        statusView.setText(status);
    }

    private void checkData(MachineData data){
        boolean danger = false;
        if(!machine.checkSpeed(data.getSpeed())){
            Log.i("DANGER", "speed: " + data.getSpeed());
            danger = true;

        }

        if(!machine.checkEfficiency(data.getEfficiency())){
            Log.i("DANGER", "efficiency: " + data.getEfficiency());
            danger = true;
        }

        if(!machine.checkTemp(data.getTemp())){
            Log.i("DANGER", "temp: " + data.getTemp());
            danger = true;
        }

        if(danger){
            goToDangerMode();
        }
    }

    private void addData(MachineData data){
        if(dataCounter + 1 > MAX_DATA_BARS){
            rpmEntries.remove(0);
            efficiencyEntries.remove(0);
            machineData.remove(0);
        }

        rpmEntries.add(new BarEntry(dataCounter, data.getValues()[0]));
        efficiencyEntries.add(new Entry(dataCounter, data.getValues()[1]));
        machineData.add(data);

        tempValue.setText(Integer.toString(data.getValues()[2]));

        dataCounter++;
        chartsUpdate();

    }

    private void startGetData(){
        Log.d(getClass().getName(),"startGetData()" + " thread:" + currentThread().getId());
        dataCounter = machineData.size();

        AtomicInteger counter = new AtomicInteger();
        apiService.getMachineDataUpdate(machine.getId())
                .repeatWhen(completed ->  completed.delay(DATA_UPDATE_DELAY, TimeUnit.MILLISECONDS)
                        .takeWhile(v -> started))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())
                .subscribe(machineDataUpdate -> {
                            counter.getAndIncrement();
                            Log.d(counter.toString() + " thread:" + currentThread().getId() + " data", Integer.toString(machine.getId()));

                            boolean dataCheck = machine.checkData(machineDataUpdate);
                            if(!dataCheck && !inDanger){
                                Log.i(getClass().getName() + " " + machine.getId(), "data check failed");
                                goToDangerMode();
                            }
                            addData(machineDataUpdate);
                        },
                        Throwable::printStackTrace);
    }

    public void pauseData() { started = false;}

    public void stopData(){
        pauseData();

        machineData.clear();
        rpmEntries.clear();
        efficiencyEntries.clear();

        tempValue.setText("N.D.");

        dataCounter = 0;

        chartsUpdate();
    }

    public void resumeData(){
        started = true;
        startGetData();
    }


    private void chartsUpdate(){

        BarDataSet rpmDataSet = new BarDataSet(rpmEntries, "data");
        rpmDataSet.setColor(machine.getValueInDanger() == MachineValue.RPM && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);
        rpmDataSet.setDrawValues(false);

        BarData rpmBarData = new BarData(rpmDataSet);

        rpmChart.getAxisLeft().setTextColor(machine.getValueInDanger() == MachineValue.RPM && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

        LineDataSet efficiencyDataSet = new LineDataSet(efficiencyEntries, "data");
        efficiencyDataSet.setColor(machine.getValueInDanger() == MachineValue.Efficiency && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);
        efficiencyDataSet.setDrawCircles(false);
        efficiencyDataSet.setDrawValues(false);
        efficiencyDataSet.setColor(getResources().getColor(R.color.holo_red));

        LineData efficiencyLineData = new LineData(efficiencyDataSet);

        efficiencyChart.getAxisLeft().setTextColor(machine.getValueInDanger() == MachineValue.Efficiency && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

        rpmChart.setData(rpmBarData);
        rpmChart.invalidate();

        efficiencyChart.setData(efficiencyLineData);
        efficiencyChart.invalidate();

        tempValue.setTextColor(machine.getValueInDanger() == MachineValue.Temp && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

    }

    private void chartsSetUp(){

        // chart 1 - BAR CHART
        rpmChart.setTouchEnabled(false);
        rpmChart.setDrawBorders(false);
        rpmChart.setDrawMarkers(false);
        rpmChart.setDescription(null);

        rpmChart.getXAxis().setDrawLabels(false);
        rpmChart.getXAxis().setTextColor(machine.getValueInDanger() == MachineValue.RPM && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);
        rpmChart.getXAxis().setDrawAxisLine(false);
        rpmChart.getXAxis().setDrawGridLines(false);

        rpmChart.getAxisRight().setDrawAxisLine(false);
        rpmChart.getAxisRight().setDrawGridLines(false);
        rpmChart.getAxisLeft().setDrawAxisLine(false);
        rpmChart.getAxisLeft().setDrawGridLines(true);
        rpmChart.getAxisLeft().setDrawLabels(true);
        rpmChart.getAxisLeft().setTextColor(machine.getValueInDanger() == MachineValue.RPM && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);
        rpmChart.getAxisLeft().setAxisMaximum(5000);
        rpmChart.getAxisLeft().setAxisMinimum(0);


        // chart 2 - LINE CHART
        efficiencyChart.setTouchEnabled(false);
        efficiencyChart.setDrawBorders(false);
        efficiencyChart.setDrawMarkers(false);
        efficiencyChart.setDescription(null);

        efficiencyChart.getXAxis().setDrawLabels(false);

        efficiencyChart.getXAxis().setTextColor(machine.getValueInDanger() == MachineValue.Efficiency && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);
        efficiencyChart.getXAxis().setDrawAxisLine(false);
        efficiencyChart.getXAxis().setDrawGridLines(false);

        efficiencyChart.getAxisRight().setDrawLabels(true);
        efficiencyChart.getAxisRight().setDrawAxisLine(false);
        efficiencyChart.getAxisRight().setDrawGridLines(false);
        efficiencyChart.getAxisLeft().setDrawAxisLine(false);
        efficiencyChart.getAxisLeft().setDrawGridLines(true);
        efficiencyChart.getAxisLeft().setDrawLabels(true);
        efficiencyChart.getAxisLeft().setTextColor(machine.getValueInDanger() == MachineValue.RPM && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

        efficiencyChart.getAxisLeft().setAxisMaximum(100);
        efficiencyChart.getAxisLeft().setAxisMinimum(0);

        tempValue.setTextColor(machine.getValueInDanger() == MachineValue.Temp && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

    }

    private void sendDangerMode(){
        apiService.commandMachine(machine.getId(), "danger")
        .subscribe(apiResult -> Log.i(getClass().getName(), "sendDangerMode: " + apiResult.getResult()));
    }

    private void goToDangerMode(){
        inDanger = true;
        Intent intent = new Intent(getActivity(), DangerActivity.class);

        intent.putExtra(DangerActivity.MACHINE_DATA_EXTRA, machineData);
        intent.putExtra(DangerActivity.MACHINE_EXTRA, (Serializable) machine);

        startActivityForResult(intent, getMachineId());
    }

    public void resolveDanger(){
        Intent intent = new Intent();
        getActivity().setResult(DangerActivity.RESULT_DANGER, intent);
        getActivity().finish();
    }

}