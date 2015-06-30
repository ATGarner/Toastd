package org.andrewgarner.toastd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * AsyncTask to act as a bluetooth server to allow incoming connections on a BT socket
 * Created by andrewgarner on 6/28/15.
 */
public class BTAsyncServer extends AsyncTask<Void, Void, BluetoothSocket> {
    private final BluetoothServerSocket mmServerSocket;
    private String TAG = "Toastd";
    private Handler mHandler;
    private BTAsyncIO ioTask;

    public static final int CONNECTION_SUCCESS = 7;
    public static final int CONNECTION_FAILED = 8;

    public BTAsyncServer(BluetoothAdapter adapter, Handler handler){
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        mHandler = handler;
        try {
            UUID myuuid = UUID.fromString("e67eef9b-0eb6-42e7-83c6-62ac247136c0");
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = adapter.listenUsingRfcommWithServiceRecord("Toastd", myuuid);
            Log.v(TAG, "BT Server / adapter begin listening");
        } catch (IOException e) {
            Log.e(TAG, "BT Server / listen error"+e);
        }
        mmServerSocket = tmp;
    }

    @Override
    protected BluetoothSocket doInBackground(Void ... Params){
        Log.v(TAG,"BT Server / doInBackground()");
        BluetoothSocket socket;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG,"BT Server / doInBackground / accept error"+e);
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                Log.v(TAG,"BT Server / doInBackground  / client accepted");
                // Do work to manage the connection (in a separate thread)
                //manageConnectedSocket(socket);

                try {
                    mmServerSocket.close();
                }catch(Exception e){
                    Log.e(TAG,"BT Server / doInBackground  / close error "+e);
                }
                return socket;
                //break;
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(BluetoothSocket socket){
        if(socket==null){
            Log.v(TAG,"BT Server / onPostExecute / null...");
            mHandler.obtainMessage(CONNECTION_FAILED).sendToTarget();
        } else {
            Log.v(TAG,"BT Server / onPostExecute / Success!");
            mHandler.obtainMessage(CONNECTION_SUCCESS, socket).sendToTarget();
            //ioTask = new BluetoothAsyncIO(socket, mHandler);
            //ioTask.execute();
        }

    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void stopServer() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG,"BTServer / cancel error"+e);
        }

        try {
            ioTask.cancelIO();
        }catch(Exception e){
            //Log.e(TAG,"BT Server / ioTask cancel error");
        }
        cancel(true);
    }

}
