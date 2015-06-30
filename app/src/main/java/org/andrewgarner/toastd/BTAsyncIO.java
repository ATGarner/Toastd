package org.andrewgarner.toastd;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by andrewgarner on 6/28/15.
 */
public class BTAsyncIO extends AsyncTask<Void, Void, Void> {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;
    private String TAG = "Toastd";

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int IO_FINISHED = 6;

    public BTAsyncIO(BluetoothSocket socket, Handler handler){
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler=handler;

        Log.v(TAG,"BT IO / start!");

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "BT IO / socket getStream error"+e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    protected Void doInBackground(Void ... Params){
        Log.v(TAG,"BT IO / background / begin");
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        String hello = "hello";
        byte[] b = hello.getBytes(Charset.forName("UTF-8"));
        write(b);
        while (true) {
            try {
                Log.v(TAG,"BT IO / background / Listening...");
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "BT IO / doInBackground / IO error "+e);
                break;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        Log.v(TAG,"BT IO / onPostExecute");
        mHandler.obtainMessage(IO_FINISHED).sendToTarget();
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "BT IO / write error "+e);
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancelIO() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "BT IO / Socket close error "+e);
        }
        Log.v(TAG, "BT IO / Shutting down...");
        mHandler.obtainMessage(IO_FINISHED).sendToTarget();
        cancel(true);
    }
}
