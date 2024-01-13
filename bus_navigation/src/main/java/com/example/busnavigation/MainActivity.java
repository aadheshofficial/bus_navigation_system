package com.example.busnavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Handler handler;
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.bus_navigation_logo);
        textView = findViewById(R.id.bus_navigation_logo_name);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        zoomInAnimation(textView);
        zoomInAnimation(imageView);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        },2000);

    }
    private void zoomInAnimation(ImageView imageView) {
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        imageView.startAnimation(zoomIn);
    }
    private void zoomInAnimation(TextView textView) {
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        textView.startAnimation(zoomIn);
    }
}