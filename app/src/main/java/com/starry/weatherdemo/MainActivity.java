package com.starry.weatherdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.starry.weatherdemo.view.SunView;
import com.starry.weatherdemo.view.WeatherView;

public class MainActivity extends AppCompatActivity {

    private SunView sunView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sunView = findViewById(R.id.weather_view);
        sunView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sunView.startAnim();
            }
        });
    }
}
