package org.andrewgarner.toastd;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by andrewgarner on 6/29/15.
 */
public class IOService extends IntentService {
    public static final String NOTIFICATION = "org.andrewgarner.toastd.IOService";

    BTAsyncClient clientTask;
    BluetoothDevice mDevice;
    BluetoothAdapter mBluetoothAdapter;
    String TAG = "Toastd";

    public IOService(){
        super("IOService");
    }

    @Override
    protected void onHandleIntent(Intent intent){

        Log.v("Toastd", "BT Service / start!");

        BluetoothDevice device = (BluetoothDevice) intent.getExtras().get("device");
        if(device!=null) {
            Log.i(TAG, "BT Service / start / " + device.getName());
            mDevice = device;
            startConnection();
        }
        else {
            Log.i(TAG, "BT Service / start / device null!");
            publishResult(8);
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        clientTask = new BTAsyncClient(this,mDevice, mBluetoothAdapter, mHandler);
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


    private Handler mHandler  = new Handler(new Handler.Callback() {
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
                    Log.d(TAG,"Service / Handler / me: "+writeMessage);
                    //Toast.makeText(getApplicationContext(), "me:" + writeMessage, Toast.LENGTH_SHORT).show();
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG,"Service / Handler / "+readMessage);
                    //Toast.makeText(getApplicationContext(),readMessage, Toast.LENGTH_SHORT).show();
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    break;
                case IO_FINISHED:
                    Log.d(TAG,"Service / Handler / IO finished");
                    //Toast.makeText(getApplicationContext(),"IO Finished", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_SUCCESS:
                    Log.d(TAG,"Service / Handler / Connection Success");
                    //Toast.makeText(getApplicationContext(),"Connection Success", Toast.LENGTH_SHORT).show();

                    new BTAsyncIO((BluetoothSocket) msg.obj, mHandler).execute();
                    break;
                case CONNECTION_FAILED:
                    Log.d(TAG,"Service / Handler / Connection Failed");
                    //Toast.makeText(getApplicationContext(),"Connection Failed", Toast.LENGTH_SHORT).show();
                    break;

            }
            return true;
        }
    });
}
