package org.andrewgarner.toastd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * AsyncTask to allow connecting to sockets on other devices running the app as a BT server
 * Created by andrewgarner on 6/28/15.
 */
public class BTAsyncClient extends AsyncTask<Void, Void, BluetoothSocket> {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private String TAG = "Toastd";
    private Context mContext;
    private Handler mHandler;
    private BTAsyncIO ioTask;

    public static final int CONNECTION_SUCCESS = 7;
    public static final int CONNECTION_FAILED = 8;

    public BTAsyncClient(Context c, BluetoothDevice device, BluetoothAdapter adapter, Handler handler){
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = adapter;
        mContext = c;
        mHandler = handler;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            UUID myuuid = UUID.fromString("e67eef9b-0eb6-42e7-83c6-62ac247136c0");
            tmp = device.createRfcommSocketToServiceRecord(myuuid);
            Log.v(TAG,"BT Client / start socket");
        } catch (IOException e) {
            Log.v(TAG,"BT Client / start socket error");
        }
        mmSocket = tmp;
    }

    @Override
    protected BluetoothSocket doInBackground(Void ... Params){
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();
        Log.v(TAG,"BT Client / run()");

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out

            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.v(TAG,"BT Client / doInBackground / close error");
            }
            Log.v(TAG,"BT Client / doInBackground / socket IOException: "+connectException);
            return null;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
        return mmSocket;
    }
    @Override
    protected void onPostExecute(BluetoothSocket socket){
        if(socket==null){
            Log.v(TAG,"BT Client / onPostExecute null...");
            mHandler.obtainMessage(CONNECTION_FAILED).sendToTarget();
            //Toast.makeText(mContext, "Client Socket null", Toast.LENGTH_SHORT).show();
        } else {
            Log.v(TAG,"BT Client / onPostExecute Success!");
            mHandler.obtainMessage(CONNECTION_SUCCESS, socket).sendToTarget();
            //Toast.makeText(mContext, "Client Socket success!", Toast.LENGTH_SHORT).show();
            //ioTask = new BluetoothAsyncIO(socket, mHandler);
            //ioTask.execute();
        }

    }

    /** Will cancel an in-progress connection, and close the socket */
    public void stopClient() {
        Log.v(TAG,"BT Client / cancel()");
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.v(TAG,"BT Client / socket close error");
        }
        try {
            ioTask.cancelIO();
        }catch(Exception e){
            Log.e(TAG,"BT Server / ioTask cancel error");
        }
        cancel(true);
    }
}
