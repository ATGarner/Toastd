package org.andrewgarner.toastd;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Service to handle BT connections and interactions
 * Made so that multiple activites in the app can listen to the BT connection
 * Created by andrewgarner on 6/29/15.
 */
public class BTService extends Service {
    BTAsyncClient clientTask;
    BluetoothDevice mDevice;
    BluetoothAdapter mBluetoothAdapter;
    BTAsyncServer serverTask;
    String TAG = "Toastd";
    public static final String NOTIFICATION = "org.andrewgarner.toastd.IOService";
    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.v("Toastd", "BT Service / start!");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice device;
        try {
            device = (BluetoothDevice) intent.getExtras().get("device");
        }catch(Exception e){
            device=null;
        }
        if(device!=null) {
            Log.i(TAG, "BT Service / start / " + device.getName());
            mDevice = device;
            startConnection();
        }
        else{
            Log.i(TAG, "BT Service / start / start server!");
            serverTask= new BTAsyncServer(mBluetoothAdapter, mHandler);
            serverTask.execute();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
        //return null;
    }


    public class LocalBinder extends Binder {
        BTService getService() {
            return BTService.this;
        }
    }

    private void publishResult(int type){
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(INTENT_MESSAGE_TYPE, type);
        sendBroadcast(intent);
    }
    private void publishResult(int type, String message){
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(INTENT_MESSAGE_TYPE, type);
        intent.putExtra(INTENT_MESSAGE_STRING, message);
        sendBroadcast(intent);
    }


    private void startConnection(){
        clientTask = new BTAsyncClient(getApplicationContext(),mDevice, mBluetoothAdapter, mHandler);
        clientTask.execute();
    }

    public static final String INTENT_MESSAGE_TYPE = "message_type";
    public static final String INTENT_MESSAGE_STRING = "message_string";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int IO_FINISHED = 6;
    public static final int CONNECTION_SUCCESS = 7;
    public static final int CONNECTION_FAILED = 8;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i(TAG, "HandleMessage!");
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG,"Handler / me: "+writeMessage);
                    Toast.makeText(BTService.this, "me:" + writeMessage, Toast.LENGTH_SHORT).show();
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG,"Handler / "+readMessage);
                    Toast.makeText(BTService.this,readMessage, Toast.LENGTH_SHORT).show();
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    break;
                case IO_FINISHED:
                    Log.d(TAG,"Handler / IO finished");
                    Toast.makeText(BTService.this,"IO Finished", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_SUCCESS:
                    Log.d(TAG,"Handler / Connection Success");
                    Toast.makeText(BTService.this,"Connection Success", Toast.LENGTH_SHORT).show();

                    new BTAsyncIO((BluetoothSocket) msg.obj, mHandler).execute();
                    break;
                case CONNECTION_FAILED:
                    Log.d(TAG,"Handler / Connection Failed");
                    Toast.makeText(BTService.this,"Connection Failed", Toast.LENGTH_SHORT).show();
                    break;

            }
            return true;
        }
    });
}
