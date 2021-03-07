package com.example.industrial;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.models.Machine;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    private static int area_id;
    private static int sector_id;

    private ArrayList<Machine> machines = new ArrayList<>();
    private List<MachineFragment> fragments = new ArrayList<>();

    String[] footerTexts = new String[2];

    TextView footer;
    TextView timestamp;
    ViewPager viewPager;

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

        APIInterface apiService = APIClient.getInstance().create(APIInterface.class);

        apiService.getCurrentPosition()
                .subscribeOn(Schedulers.io())
                .flatMap(location -> {
                    area_id = location.area_id;
                    sector_id = location.sector_id;
                    return apiService.getArea(area_id);
                })
                .flatMap(area -> {
                    Log.i("test", "sectors count:" + area.getSectors_count());
                    footerTexts[0] = area.getName() + " - " + area.getSectors_count();
                    return apiService.getSector(sector_id);
                })
                .flatMap(sector -> {
                    footerTexts[1] = sector.getName();
                    footer.setText("Area: " + footerTexts[0]+ " - Sector: " + footerTexts[1]);
                    return apiService.getSectorMachines(sector_id);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        machines -> {
                            for(int i = 0; i < machines.size(); i++){
                                Log.i("DEBUG", machines.get(i).getName());
                                fragments.add(MachineFragment.newInstance(machines.get(i), R.menu.main_menu));

                            }
                            screenSlidePagerAdapter.notifyDataSetChanged();

                            // per mantenere tutti i fragment attivi nel viewpager
                            viewPager.setOffscreenPageLimit(fragments.size());
                        },
                        error -> Log.e("MyTag", "Throwable " + error.getMessage())
                );

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("MainActivity", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("MainActivity", "onDestroy");
    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        switch (gesture) {
            case TAP:
                fragments.get(viewPager.getCurrentItem()).onSingleTapUp();
                return true;
            default:
                return super.onGesture(gesture);
        }
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

