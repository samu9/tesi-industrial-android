package com.example.industrial.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.industrial.API.APIClient;
import com.example.industrial.API.APIInterface;
import com.example.industrial.fragments.MachineFragment;
import com.example.industrial.R;
import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.menu.MenuActivity;
import com.example.industrial.models.Machine;
import com.example.industrial.models.MachineData;

import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DangerActivity extends BaseActivity {
    public static final String MACHINE_EXTRA = "machine";
    public static final String MACHINE_DATA_EXTRA = "machine data";
    public static final String MACHINE_STATUS = "machine status";

    public static final int RESULT_DANGER = 3000;

    FrameLayout content;
    TextView instructionMessage;
    MachineFragment fragment;

    Machine machine;
    ArrayList<MachineData> machineData;

    APIInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);

        content = findViewById(R.id.danger_content);
        instructionMessage = findViewById(R.id.instruction_message);
        instructionMessage.setText("Select 'Next instruction' on the menu.");

        machine = (Machine) getIntent().getSerializableExtra(MACHINE_EXTRA);
        Log.d(getClass().getName(), "machine status " + machine.getStatus());
        machineData = new ArrayList<>();
        machineData.addAll((ArrayList<MachineData>) getIntent().getSerializableExtra(MACHINE_DATA_EXTRA));

        fragment = MachineFragment.newInstance(machine, R.menu.danger_menu, machineData, true);

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.danger_content, fragment)
            .commit();

        apiService = APIClient.getInstance().create(APIInterface.class);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == MenuActivity.RESULT_MENU && data != null){
            final int id = data.getIntExtra(MenuActivity.EXTRA_MENU_ITEM_ID_KEY,
                    MenuActivity.EXTRA_MENU_ITEM_DEFAULT_VALUE);
            if (id == R.id.next_instruction){
                apiService.getDangerInstruction(machine.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(apiResult -> instructionMessage.setText(apiResult.getMessage()));
                Log.e(getClass().getSimpleName(),"next instruction");
            }
        }
    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        switch (gesture) {
            case TAP:
                fragment.onSingleTapUp();
                return true;
            default:
                return false;
        }
    }
}