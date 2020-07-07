package com.example.grybos.bluetoothapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class Receivers {

    private ProgressDialog pDialog;
    private ListViewAdapter adapter;
    private ArrayList<BluetoothDevice> devices;

    public Receivers(Context context, ListViewAdapter adapter, ArrayList<BluetoothDevice> devices) {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Wyszukiwanie w toku");
        pDialog.setCancelable(false);

        this.adapter = adapter;
        this.devices = devices;

    }

    public BroadcastReceiver startReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d("mode", "action" + action);

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

                Log.d("context", "Start wyszukiwania...");

                pDialog.show();

            }

        }
    };

    public  BroadcastReceiver findReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d("mode", "action" + action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)){

                Log.d("context", "Znaleziono urządzenie bluetooth...");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                devices.add(device);
                adapter.notifyDataSetChanged();

            }

        }
    };

    public BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d("mode", "action: " + action);

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d("context", "koniec wyszukiwania...");
                // wyłącz progressDialog
                pDialog.dismiss();
            }

        }
    };

    public BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d("mode", "action: " + action);

            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                if (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    Log.d("xxx","nie jest w trybie discoverable mode ale może odbierać połączenie");
                } else if (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Log.d("xxx","tryb wyszukiwania");
                } else if (mode == BluetoothAdapter.SCAN_MODE_NONE) {
                    Log.d("xxx","nie odbiera połączeń");
                } else {
                    Log.d("xxx","inny błąd");
                }
            }

        }
    };

}
