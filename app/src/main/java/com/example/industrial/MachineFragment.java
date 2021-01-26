package com.example.industrial;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.industrial.models.Area;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MachineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MachineFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MACHINE_NAME = "machine_name";
    private static final String MACHINE_ID = "machine_id";

    // TODO: Rename and change types of parameters
    private String machineName;
    private int machineId;

    DataService service = new DataService(getContext());

    public MachineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
     * @param id Parameter 2.
     * @return A new instance of fragment MachineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MachineFragment newInstance(String name, int id) {
        MachineFragment fragment = new MachineFragment();
        Bundle args = new Bundle();
        args.putString(MACHINE_NAME, name);

        args.putInt(MACHINE_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            machineName = getArguments().getString(MACHINE_NAME);
            machineId = getArguments().getInt(MACHINE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_machine, container, false);
        TextView nameView = v.findViewById(R.id.machine_name);
        TextView idView = v.findViewById(R.id.machine_id);

        nameView.setText(machineName);
        idView.setText(Integer.toString(machineId));

        service.getArea(1, new DataService.VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(Object response) {
                Area area = (Area) response;
                System.out.println("Area " + area.getName() + " from Fragment");
            }
        });


        return v;
    }
}