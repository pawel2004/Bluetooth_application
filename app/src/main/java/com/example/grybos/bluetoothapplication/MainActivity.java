package com.example.grybos.bluetoothapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

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
    private Button connect;
    private Button send;
    private TextView status;
    private ProgressDialog progressDialog;
    private Communication communication;

    //serwerowe
    private static final String APP_NAME = "Gryboś_chat";
    private static final UUID MY_UUID = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView1 = findViewById(R.id.list_view1);
        listView2 = findViewById(R.id.list_view2);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        gps_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        getSupportActionBar().setTitle("Nazwa urządzenia: " + bluetoothAdapter.getName());

        connect = findViewById(R.id.connect);
        send = findViewById(R.id.send);
        status = findViewById(R.id.status);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("server listening...");
        progressDialog.setCancelable(false);

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

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Server server = new Server();
                server.start();
                progressDialog.show();

            }
        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Client client = new Client(devices.get(i));
                client.start();

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

    private class Server extends Thread{

        //server socket
        private BluetoothServerSocket bluetoothServerSocket;
        //prosty konstruktor
        public Server(){

            try {
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();

            //klient socket
            BluetoothSocket bluetoothClientSocket = null;

            Log.d("xxx", "server starting...");

            while (bluetoothClientSocket == null){

                Log.d("xxx", "server listening...");

                try {
                    bluetoothClientSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bluetoothClientSocket != null){

                    Log.d("xxx", "server success connected!!!");

                    Message message = Message.obtain();
                    message.what = 0;
                    handler.sendMessage(message);

                    break;

                }

            }

        }
    }

    private class Client extends Thread{

        private BluetoothSocket bluetoothSocket;
        private BluetoothDevice bluetoothDevice;

        //klient w konstruktorze przekazuje dane urządzenia

        public Client(BluetoothDevice bluetoothDevice){

            this.bluetoothDevice = bluetoothDevice;

            try {
                bluetoothSocket = this.bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();

            //koniec szukania urządzeń

            bluetoothAdapter.cancelDiscovery();

            //łącz z serwerem
            try {
                bluetoothSocket.connect();
                Log.d("xxx", "client connected !!!");
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what){

                case 0:
                    status.setText("Połączono!!!");
                    progressDialog.dismiss();
                    break;

                case 1:
                    status.setText("Połączono!!!");
                    break;
            }

            return false;
        }
    });

    private class Communication extends Thread{

        private BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public Communication(BluetoothSocket bluetoothSocket){

            this.bluetoothSocket = bluetoothSocket;
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void writeMessage(byte[] data){

            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();

            byte[] buffer = new byte[1024];
            int bytes = 0;

            while (true){

                try {
                    bytes = inputStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bytes > 0){

                    handler.obtainMessage(1, bytes, -1, buffer).sendToTarget();

                }

            }

        }
    }

}
