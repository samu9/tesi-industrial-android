/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.industrial.menu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.recyclerview.widget.SnapHelper;

import com.example.industrial.activities.BaseActivity;
import com.example.industrial.R;
import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.models.Machine;

import java.util.ArrayList;
import java.util.List;

import info.androidhive.fontawesome.FontDrawable;

/**
 * Activity which provides the menu functionality. It creates the horizontal recycler view to move
 * between menu items.
 */
public class MenuActivity extends BaseActivity implements GlassGestureDetector.OnGestureListener {

  public static final int RESULT_MENU = 1000;

  public static final String EXTRA_MENU_ITEM_ID_KEY = "id";

  public static final int EXTRA_MENU_ITEM_DEFAULT_VALUE = -1;

  public static final String EXTRA_MENU_KEY = "menu_key";
  public static final String EXTRA_MACHINE_KEY = "machine";
  public static final String EXTRA_MACHINE_STATUS_KEY = "machine status";

  private MenuAdapter adapter;
  private List<GlassMenuItem> menuItems = new ArrayList<>();
  private int currentMenuItemIndex;
  private int machineId;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.menu_layout);

    machineId = getIntent().getIntExtra(EXTRA_MACHINE_KEY, -1);

    Log.d(getClass().getName(),"menu activity on machine id " + machineId);
    final RecyclerView recyclerView = findViewById(R.id.menuRecyclerView);
    adapter = new MenuAdapter(menuItems);
    final LayoutManager layoutManager = new LinearLayoutManager(this,
        LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    recyclerView.setFocusable(true);

    final SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);

    recyclerView.addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        final View foundView = snapHelper.findSnapView(layoutManager);
        if (foundView == null) {
          return;
        }
        currentMenuItemIndex = layoutManager.getPosition(foundView);
      }
    });
    Log.d(getClass().getName(), "Created");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    int menuResource = getIntent()
        .getIntExtra(EXTRA_MENU_KEY, EXTRA_MENU_ITEM_DEFAULT_VALUE);

    String machineStatus = getIntent().getStringExtra(EXTRA_MACHINE_STATUS_KEY);

    if (menuResource != EXTRA_MENU_ITEM_DEFAULT_VALUE) {
      final MenuInflater inflater = getMenuInflater();
      inflater.inflate(menuResource, menu);

      for (int i = 0; i < menu.size(); i++) {
        final MenuItem menuItem = menu.getItem(i);
        if(machineStatus.equals(Machine.STOP) && menuItem.getItemId() == R.id.go_to_danger){
          continue;
        }
        if(!machineStatus.equals(Machine.PAUSE) && menuItem.getItemId() == R.id.resume){
          continue;
        }
        if((machineStatus.equals(Machine.START) || machineStatus.equals(Machine.PAUSE)) && menuItem.getItemId() == R.id.start){
          continue;
        }
        if(machineStatus.equals(Machine.PAUSE) && menuItem.getItemId() == R.id.pause){
          continue;
        }
        if(machineStatus.equals(Machine.STOP) && (menuItem.getItemId() == R.id.stop || menuItem.getItemId() == R.id.pause)){
          continue;
        }

        FontDrawable icon;
        switch (menuItem.getItemId()){
          case R.id.start:
            icon = new FontDrawable(this, R.string.fa_play_solid,true,false);
            break;
          case R.id.pause:
            icon =  new FontDrawable(this, R.string.fa_pause_solid,true,false);
            break;
          case R.id.stop:
            icon =  new FontDrawable(this, R.string.fa_stop_solid,true,false);
            break;
          case R.id.resume:
            icon =  new FontDrawable(this, R.string.fa_step_forward_solid,true,false);
            break;
          case R.id.log_menu_item:
            icon =  new FontDrawable(this, R.string.fa_file_alt,true,false);
            break;
//          case R.id.power_off:
//            icon =  new FontDrawable(this, R.string.fa_power_off_solid,true,false);
//            break;
          default:
            icon = null;
        }

        GlassMenuItem glassMenuItem = new GlassMenuItem(menuItem.getItemId(), icon,
                menuItem.getTitle().toString());

        menuItems.add(glassMenuItem);
        Log.d(getClass().getName(),menuItem.getTitle().toString() + " - " + menuItem.getItemId());
        adapter.notifyDataSetChanged();
      }
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onGesture(GlassGestureDetector.Gesture gesture) {
    switch (gesture) {
      case TAP:
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_MENU_ITEM_ID_KEY, menuItems.get(currentMenuItemIndex).getId());

        setResult(RESULT_MENU, intent);
        finish();
        return true;
      default:
        return super.onGesture(gesture);
    }
  }
}
