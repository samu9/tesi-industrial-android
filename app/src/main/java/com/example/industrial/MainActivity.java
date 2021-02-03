package com.example.industrial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.industrial.models.Area;
import com.example.industrial.models.Machine;
import com.example.industrial.models.Sector;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends FragmentActivity {
    private static int area_id;
    private static int sector_id;

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



        // TODO Barros 7 - Instanzio retrofit
        APIInterface apiService = APIClient.getInstance().create(APIInterface.class);

        // TODO Barros 8 - faccio la chiamata getFakeCurrentPosition alla mia API, per farti vedere come funziona una singola chiamata
        apiService.getFakeCurrentPosition()
                // gestisco la response
                .map(result -> Log.i("MyTag", "Area ID: " + result.area_id + " -- Sector ID: " + result.sector_id))
                .subscribe();

        // TODO Barros 9 - faccio la chiamata getFakeCurrentPosition alla mia API e successivamente uso position per fare la chiamata a quella dell'area (però ricorda che io torno la lista)
        apiService.getFakeCurrentPosition()

                // Creo un thread in background per fare la chiamata così allegerisco il sistema
                .subscribeOn(Schedulers.newThread())

                // prendo la posizione e faccio la chiamata verso il servizio che torna l'area
                // ma al posto di quello che ho fatto a riga 78, avresti il codice commentato che segue
                // .flatMap(position -> apiService.getArea(position.id))
                .flatMap(position -> apiService.getFakeAreas())

                // ritorno sul main thread, altrimenti non posso cambiare la UI
                .observeOn(AndroidSchedulers.mainThread())

                // gestisco gli errori
                .doOnError(throwable -> Log.e("MyTag", "Throwable " + throwable.getMessage()))

                // leggo il risultato finale, ma al posto di quello che ho fatto a riga 91, avresti il codice commentato che segue
                // .subscribe(
                //      area -> footer.setText("Area: " + area.getId() + " - Settore: " + area.getName()),
                //      error -> Log.e("MyTag", "Throwable " + error.getMessage())
                // );
                .subscribe(
                        areas -> Log.i("MyTag", "Area ID: " + areas.getAreas().get(0).getId() + " -- Area Name: " + areas.getAreas().get(0).getName()),
                        error -> Log.e("MyTag", "Throwable " + error.getMessage())
                );


        service.getCurrentPosition(new DataService.VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(Object response) {
                DataService.Position position = (DataService.Position) response;
                area_id = position.area_id;
                sector_id = position.sector_id;

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

                        for(int i = 0; i < machines.size(); i++){
                            fragments.add(MachineFragment.newInstance(machines.get(i).getName(), machines.get(i).getId(), machines.get(i).getStatus()));
                        }
                        screenSlidePagerAdapter.notifyDataSetChanged();

//                getSupportFragmentManager().beginTransaction().replace(R.id.body_layout, fragments.get(0)).commit();
                    }
                });
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

