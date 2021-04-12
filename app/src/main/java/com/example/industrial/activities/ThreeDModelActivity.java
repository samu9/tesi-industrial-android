package com.example.industrial.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.example.industrial.R;
import com.example.industrial.Renderer;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

public class ThreeDModelActivity extends BaseActivity {

    Renderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_d_model);

        final RajawaliSurfaceView surface = findViewById(R.id.threed_model_surface);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        // Add mSurface to your root view
//        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

        renderer = new Renderer(this);
        surface.setSurfaceRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        renderer.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}