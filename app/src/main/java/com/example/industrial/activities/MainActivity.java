package com.example.industrial.activities;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.industrial.API.APIClient;
import com.example.industrial.API.APIInterface;
import com.example.industrial.fragments.MachineFragment;
import com.example.industrial.R;
import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.models.Machine;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
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
                                fragments.add(MachineFragment.newInstance(machines.get(i), R.menu.main_menu, null, false));

                            }
                            screenSlidePagerAdapter.notifyDataSetChanged();

                            // per mantenere tutti i fragment attivi nel viewpager
                            viewPager.setOffscreenPageLimit(fragments.size());
                        },
                        error -> {
                            Toast.makeText(this, "CONNECTION ERROR", Toast.LENGTH_SHORT).show();

                            Log.e("MyTag", "Throwable " + error.getMessage());
                        }
                );
        Observable.interval(1000L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(longTimed -> {
                    timestamp.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                            .format(new Timestamp(System.currentTimeMillis())));
                });

    }


    @Override
    protected void onResume() {
//        for(MachineFragment fragment: fragments){
//            if(fragment.getMachineStatus() == Machine.START){
//                fragment.onResume();
//            }
//        }
        super.onResume();
        Log.d("MainActivity", "onResume");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if(resultCode == MenuActivity.RESULT_MENU) {
//            for (MachineFragment fragment : fragments) {
//                Log.d(getClass().getName(), "fragment " + fragment.getMachineId());
//                if (fragment.getMachineStatus() == Machine.START && requestCode != fragment.getMachineId()) {
//                    fragment.resumeData();
//                }
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onPause() {

        ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

        Log.i(getClass().getName(),"onPause" +  cn.toString());

//        for(MachineFragment fragment: fragments){
//            fragment.onPause();
//        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for(int i = 0; i < fragments.size(); i++){

            transaction.remove(fragments.get(i));
        }
        transaction.commit();

        Log.d(getClass().getName(), "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        Log.i(getClass().getName(),"OnGesture");
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

