package com.example.industrial.activities;

import android.os.Bundle;

import com.example.industrial.fragments.MachineFragment;
import com.example.industrial.R;
import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;

import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class DangerActivity extends BaseActivity {
    public static final String MACHINE_EXTRA = "machine";
    public static final String MACHINE_DATA_EXTRA = "machine data";
    public static final String MACHINE_STATUS = "machine status";

    public static final int RESULT_DANGER = 3000;

    FrameLayout content;

    MachineFragment fragment;

    Machine machine;
    ArrayList<MachineData> machineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);
//        setTheme(R.style.DangerTheme);

        content = findViewById(R.id.danger_content);

        machine = (Machine) getIntent().getSerializableExtra(MACHINE_EXTRA);
        Log.d(getClass().getName(), "machine status " + machine.getStatus());
        machineData = new ArrayList<>();
        machineData.addAll((ArrayList<MachineData>) getIntent().getSerializableExtra(MACHINE_DATA_EXTRA));

        fragment = MachineFragment.newInstance(machine, R.menu.danger_menu, machineData, true);

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.danger_content, fragment)
            .commit();

    }

    @Override
    protected void onPause() {
        Log.i(getClass().getName(), "onPause");

        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(getClass().getName(), "onResume");
        super.onResume();
    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        switch (gesture) {
            case TAP:
                fragment.onSingleTapUp();
                return true;
            default:
                return super.onGesture(gesture);
        }
    }
}