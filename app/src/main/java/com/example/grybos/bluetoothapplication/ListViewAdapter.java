package com.example.grybos.bluetoothapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListViewAdapter extends ArrayAdapter{

    //zmienne
    private ArrayList<BluetoothDevice> _list;
    private Context _context;
    private int _resource;

    public ListViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList objects) {
        super(context, resource, objects);

        this._list= objects;
        this._context = context;
        this._resource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(_resource, null);

        TextView txt1 = (TextView) convertView.findViewById(R.id.t1);
        txt1.setText("Nazwa: " + _list.get(position).getName());
        TextView txt2 = (TextView) convertView.findViewById(R.id.t2);
        txt2.setText("MAC: " + _list.get(position).getAddress());
        TextView txt3 = (TextView) convertView.findViewById(R.id.t3);
        txt3.setText("Bond state: " + _list.get(position).getBondState());

        return convertView;
    }
}
