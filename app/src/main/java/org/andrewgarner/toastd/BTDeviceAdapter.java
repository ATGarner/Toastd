package org.andrewgarner.toastd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for the listview of all devices
 * Created by andrewgarner on 6/27/15.
 */
public class BTDeviceAdapter extends ArrayAdapter<MyBluetoothDevice> {

    public BTDeviceAdapter(Context c, ArrayList<MyBluetoothDevice> devices){
        super(c,R.layout.bt_device_layout,devices);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        MyBluetoothDevice device = getItem(position);

        ViewHolder holder;
        if(convertView==null){
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.bt_device_layout, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.device_name);
            holder.address = (TextView) convertView.findViewById(R.id.device_address);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(device.getName()==null || device.getName().equals(""))
            holder.name.setText("<Unknown Name>");
        else
            holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        return convertView;
    }

    private static class ViewHolder{
        TextView name;
        TextView address;
    }

}
