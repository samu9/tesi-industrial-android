package com.example.industrial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.TextView;

import com.example.industrial.models.Area;
import com.example.industrial.models.Machine;
import com.example.industrial.models.Sector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private static final int area_id = 1;
    private static final int sector_id = 1;

    private ArrayList<Machine> machines = new ArrayList<>();
    private List<MachineFragment> fragments = new ArrayList<>();

    String[] footerTexts = new String[2];

    TextView footer;
    TextView timestamp;
    ViewPager viewPager;

    DataService service = new DataService(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ScreenSlidePagerAdapter screenSlidePagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        footer = findViewById(R.id.footer);
        timestamp = findViewById(R.id.timestamp);
        viewPager = findViewById(R.id.body_layout);
        viewPager.setAdapter(screenSlidePagerAdapter);

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

        service.getSectorMachines(sector_id, new DataService.VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(Object response) {
                machines = (ArrayList<Machine>) response;
                Machine m = machines.get(2);

                for(int i = 0; i < machines.size(); i++){
                    fragments.add(MachineFragment.newInstance(machines.get(i).getName(), machines.get(i).getId()));
                }
                screenSlidePagerAdapter.notifyDataSetChanged();

//                getSupportFragmentManager().beginTransaction().replace(R.id.body_layout, fragments.get(0)).commit();
            }
        });

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}

