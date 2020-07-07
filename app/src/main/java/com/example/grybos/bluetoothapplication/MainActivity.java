package com.example.grybos.bluetoothapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    //zmienne
    private ListView listView1;
    private ListView listView2;
    private ArrayList<String> list = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private boolean bluetoothState;
    private Intent bIntent;
    private ArrayAdapter<String> adapter;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ListViewAdapter adapter2;
    private IntentFilter startFilter;
    private IntentFilter foundFilter;
    private IntentFilter finishFilter;
    private IntentFilter scanFilter;
    private Receivers receivers;
    private LocationManager gps_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView1 = findViewById(R.id.list_view1);
        listView2 = findViewById(R.id.list_view2);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        gps_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkIfBluetooth();

        adapter2 = new ListViewAdapter(
                MainActivity.this,
                R.layout.device_info,
                devices
        );
        listView2.setAdapter(adapter2);

        receivers = new Receivers(MainActivity.this, adapter2, devices);
        startFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        finishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        scanFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        adapter = new ArrayAdapter<String>(
                MainActivity.this,
                R.layout.drawer_object,
                R.id.txt1,
                list
        );
        listView1.setAdapter(adapter);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i){

                    case 0:

                        if (!bluetoothState){

                            startActivityForResult(bIntent, 222);

                            bluetoothState = true;

                        }
                        else {

                            bluetoothAdapter.disable();

                            bluetoothState = false;

                            refresh();

                        }

                        break;

                    case 1:

                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                        startActivityForResult(intent, 100);

                        break;

                    case 2:

                        Log.d("xxx", "Zaczynam");

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){

                            Log.d("xxx", "Nie ma uprawnienia!");

                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                    },
                                    999);

                        }else {

                            if (!gps_manager.isLocationEnabled()){

                                Log.d("location", "Nie ma GPS!");

                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                            }else {

                                devices.clear();

                                bluetoothAdapter.startDiscovery();

                            }

                        }

                        break;

                    case 3:

                        Set<BluetoothDevice> bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();

                        devices.clear();

                        for (BluetoothDevice device : bluetoothDeviceSet){

                            devices.add(device);

                        }

                        adapter2.notifyDataSetChanged();

                        break;
                }

            }
        });

    }

    private void checkIfBluetooth(){

        if (bluetoothAdapter != null){

            Log.d("xxx", "Bluetooth supported");

            bluetoothState = bluetoothAdapter.isEnabled();

            bIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        }
        else {

            Log.d("xxx", "Bluetooth not supported");

            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Twoje urządzenie nie obsługuje Bluetooth!");
            alert.setCancelable(false);
            alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    finish();

                }
            });
            alert.show();

        }

    }

    private void refresh(){

        list.clear();

        list.add("Włącz Bluetooth");
        list.add("Pozwól na wyszukiwanie");
        list.add("Szukaj urządzeń");
        list.add("Pokaż sparowane");

        if (bluetoothState){

            list.set(0, "Wyłącz Bluetooth");

        }
        else {list.set(0, "Włącz Bluetooth");}

        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();

        registerReceiver(receivers.startReceiver, startFilter);
        registerReceiver(receivers.findReceiver, foundFilter);
        registerReceiver(receivers.stopReceiver, finishFilter);
        registerReceiver(receivers.scanReceiver, scanFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receivers.startReceiver);
        unregisterReceiver(receivers.findReceiver);
        unregisterReceiver(receivers.stopReceiver);
        unregisterReceiver(receivers.scanReceiver);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 222 && resultCode == RESULT_OK){

            refresh();

        }

        if (requestCode == 100){

            bluetoothState = true;

            refresh();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == 999 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//
//            Log.d("xxx", "Działa!");
//
//            bluetoothAdapter.startDiscovery();
//
//        }

    }
}
