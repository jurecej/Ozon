package com.app.ble.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.ble.R;

public class StartActivity extends AppCompatActivity {

    TextView tvName, tvSkip;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        tvName = findViewById(R.id.tv_name);
        tvSkip = findViewById(R.id.tv_skip);
        btnConnect = findViewById(R.id.btn_connect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}