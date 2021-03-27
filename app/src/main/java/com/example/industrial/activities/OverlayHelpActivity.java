package com.example.industrial.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.industrial.API.APIClient;
import com.example.industrial.R;
import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.models.User;
import com.squareup.picasso.Picasso;

public class OverlayHelpActivity extends BaseActivity {
    public static final String ASSISTANT_EXTRA = "assistant_key";

    ImageView imgView;
    TextView name, role, phone;

    User assistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay_help);

        assistant = (User) getIntent().getSerializableExtra(ASSISTANT_EXTRA);

        name = findViewById(R.id.overlay_help_name);
        name.setText(assistant.getName());

        role = findViewById(R.id.overlay_help_role);
        role.setText(assistant.getRole());

        phone = findViewById(R.id.overlay_help_phone);
        phone.setText(assistant.getPhone());

        imgView = findViewById(R.id.overlay_help_img);
        String imgUrl = APIClient.getBaseUrl() + assistant.getImgUrl();
        Picasso.get().load(imgUrl).centerCrop().fit().into(imgView);

    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        switch (gesture){
            case TAP:
                finish();
                return true;
            default:
                return false;
        }
    }
}