package org.andrewgarner.toastd;

import android.bluetooth.BluetoothDevice;

/**
 * Class to encapsulate a bluetooth device
 * Currently doesn't do anything more than the BluetoothDevice class,
 * but more functionality will be added later when the chat section becomes active
 * Created by andrewgarner on 6/27/15.
 */
public class MyBluetoothDevice {
    private BluetoothDevice device;
    private String device_name;
    private String device_address;

    public MyBluetoothDevice(BluetoothDevice device){
        this.device=device;
        device_name = device.getName();
        device_address = device.getAddress();
    }
    public String getName(){
        return device_name;
    }
    public String getAddress(){
        return device_address;
    }
    public BluetoothDevice getDevice(){
        return device;
    }
}
