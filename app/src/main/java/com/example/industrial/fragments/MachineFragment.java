package com.example.industrial.fragments;

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

import com.example.industrial.API.APIClient;
import com.example.industrial.API.APIInterface;
import com.example.industrial.R;
import com.example.industrial.activities.DangerActivity;
import com.example.industrial.activities.MachineLogActivity;
import com.example.industrial.menu.MenuActivity;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;
import com.example.industrial.models.MachineValue;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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

        clearData();

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
                    sendMachineCommand(Machine.START);
                    break;
                case R.id.pause:
                    message = "Pausing " + machine.getName();
                    sendMachineCommand(Machine.PAUSE);

                    break;
                case R.id.resume:
                    message = "Resuming " + machine.getName();
                    sendMachineCommand(Machine.RESUME);

                    break;
                case R.id.stop:
                    message = "Stopping " + machine.getName();
                    sendMachineCommand(Machine.STOP);

                    break;
                case R.id.go_to_danger:
                    Log.i(getClass().getName(),"Go to danger");
                    sendDangerMode();
                    break;
                case R.id.halt:
                    resolveDanger();
                    break;
                case R.id.resolve:
                    message = "Danger resolved";
                    sendMachineCommand("resolve");
                    break;
                case R.id.log_menu_item:
                    goToLogs();
                    break;
            }
            if(message != null){
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }

        // ritorno da danger mode risolta
        if(requestCode == machine.getId() && resultCode == DangerActivity.RESULT_DANGER){
            inDanger = false;
            machine.setStatus(Machine.START);
            clearData();

            ArrayList<MachineData> returnedData = (ArrayList<MachineData>) data.getSerializableExtra(DangerActivity.MACHINE_DATA_EXTRA);
            if(returnedData != null){
                for(MachineData returned: returnedData){
                    addData(returned);
                }
            }

        }
    }

    private void sendMachineCommand(String command){
        String newStatus = command == Machine.RESUME? Machine.START : command;
        updateMachineStatus(newStatus);

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
                            case Machine.RESUME:
                                resumeData();
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

    private void addData(MachineData data){
        if(dataCounter + 1 > MAX_DATA_BARS){
            rpmEntries.remove(0);
            efficiencyEntries.remove(0);
            machineData.remove(0);
        }

        rpmEntries.add(new BarEntry(dataCounter, data.getValues()[0]));
        efficiencyEntries.add(new Entry(dataCounter, data.getValues()[1]));
        machineData.add(data);

        tempValue.setText(Integer.toString(data.getValues()[2]) + "°");

        dataCounter++;
        chartsUpdate();

    }

    private void clearData(){
        machineData.clear();
        rpmEntries.clear();
        efficiencyEntries.clear();

        tempValue.setText("N.D.");

        dataCounter = 0;

        chartsUpdate();
    }

    private void startGetData(){
        Log.d(getClass().getName(),"startGetData()" + " thread:" + currentThread().getId());
        dataCounter = machineData.size();

        AtomicInteger counter = new AtomicInteger();
        apiService.getMachineDataUpdate(machine.getId())
                .repeatWhen(completed ->  completed.delay(DATA_UPDATE_DELAY, TimeUnit.MILLISECONDS)
                        .takeWhile(v -> started))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineDataUpdate -> {
                            counter.getAndIncrement();
                            Log.d(counter.toString() + " thread:" + currentThread().getId() + " data", Integer.toString(machine.getId()));

                            boolean dataCheck = machine.checkData(machineDataUpdate);
                            if(!dataCheck && !inDanger){
                                Log.i(getClass().getName() + " " + machine.getId(), "data check failed");
                                goToDangerMode();
                            }
                            // danger mode risolta, posso ritornare
                            if(dataCheck && inDanger){
                                backToNormalMode();
                            }
                            addData(machineDataUpdate);
                        },
                        Throwable::printStackTrace);
    }

    public void pauseData() { started = false;}

    public void stopData(){
        pauseData();
        clearData();
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
        efficiencyDataSet.setColor(machine.getValueInDanger() == MachineValue.Efficiency && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

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
        efficiencyChart.getAxisLeft().setTextColor(machine.getValueInDanger() == MachineValue.Efficiency && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

        efficiencyChart.getAxisLeft().setAxisMaximum(100);
        efficiencyChart.getAxisLeft().setAxisMinimum(0);

        tempValue.setTextColor(machine.getValueInDanger() == MachineValue.Temp && inDanger?
                getResources().getColor(R.color.holo_red) : Color.WHITE);

    }

    private void goToLogs(){
        Intent intent = new Intent(getActivity(), MachineLogActivity.class);
        intent.putExtra(MachineLogActivity.MACHINE_ID_EXTRA, machine.getId());
        startActivity(intent);
    }

    private void sendDangerMode(){
        apiService.setMachineDanger(getMachineId())
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
        apiService.commandMachine(getMachineId(), "resolve").subscribe(apiResult -> {
//            if(apiResult.getResult()){
//                Intent intent = new Intent();
//                getActivity().setResult(DangerActivity.RESULT_DANGER, intent);
//                getActivity().finish();
//            }
        });
    }

    public void backToNormalMode(){
        final Intent intent = new Intent();
        intent.putExtra(DangerActivity.MACHINE_DATA_EXTRA, machineData);
        getActivity().setResult(DangerActivity.RESULT_DANGER, intent);
        getActivity().finish();
    }

}