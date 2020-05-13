package com.syy.timeselectview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    TimeSelect timeSelect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeSelect = findViewById(R.id.ts_duration);
        timeSelect.setSelect(5, 10);
    }
}
