package com.example.class_24b_2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private AppCompatButton loginButton;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private boolean angle;
    private boolean specificWifi;
    private boolean nfc;
    private boolean isConnectedToWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        findViews();
        checkSensors(this);
        checkFineLocation(this);
        checkBluetooth(this);
        startSensorMonitoring();
        onClick();
    }

    public void checkFineLocation(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        }
    }

    public void checkBluetooth(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH}, PERMISSIONS_REQUEST_CODE);
        }
    }

    public void checkSensors(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BODY_SENSORS}, PERMISSIONS_REQUEST_CODE);
        }
    }


    private void findViews() {
        loginButton = findViewById(R.id.login_button);
    }

    private void onClick() {
        loginButton.setOnClickListener(v -> grantPermissions());
    }

    private void startSensorMonitoring() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void checkNfc() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported", Toast.LENGTH_SHORT).show();
        } else {
            nfc = nfcAdapter.isEnabled();
        }
    }


    public void checkWifiName(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
            isConnectedToWifi = true;
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            String currentSSID = wifiInfo.getSSID();
            if (currentSSID.startsWith("\"") && currentSSID.endsWith("\"")) {
                currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
            }
            specificWifi = currentSSID.equals("DLINK1");
        } else {
            isConnectedToWifi = false;
            specificWifi = false;
        }
    }

    private void grantPermissions() {
        checkWifiName(this);
        checkNfc();
        checkWifiName(this);
        if (angle && specificWifi && nfc) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "You need to allow body sensors", Toast.LENGTH_SHORT).show();
        } else if (!angle) {
            Toast.makeText(this, "You need to turn your phone to 90 degrees", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Phone is at 90 degrees", Toast.LENGTH_SHORT).show();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "You need to allow fine location", Toast.LENGTH_SHORT).show();
        } else if (!isConnectedToWifi) {
            Toast.makeText(this, "You need to connect to a DLINK1 WiFi network", Toast.LENGTH_SHORT).show();
            checkWifiName(this);
        } else if (!specificWifi) {
            Toast.makeText(this, "You need to connect to DLINK1 wifi", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "You are connected to DLINK1 wifi", Toast.LENGTH_SHORT).show();

        }
        if (!nfc) {
            Toast.makeText(this, "You need to enable nfc", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You enabled nfc", Toast.LENGTH_SHORT).show();

        }

    }


@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSIONS_REQUEST_CODE) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                Log.d("k", "Permission granted: " + permissions[i]);
            } else {
                Log.d("k", "Permission denied: " + permissions[i]);
            }
        }
    }
}

@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        float x = event.values[0];
        float z = event.values[2];

        angle = Math.abs(x) < 1 && Math.abs(z) > 9;
    }
}

@Override
protected void onResume() {
    super.onResume();
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
        startSensorMonitoring();
    }
}

@Override
protected void onPause() {
    super.onPause();
    if (sensorManager != null) {
        sensorManager.unregisterListener(this);
    }
}

@Override
public void onAccuracyChanged(Sensor sensor, int accuracy) {
}
}
