package org.andrewgarner.toastd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends ActionBarActivity{

    private int REQUEST_ENABLE_BT =1;
    private BluetoothAdapter mBluetoothAdapter;
    private static String TAG = "Toastd";
    private ArrayList<MyBluetoothDevice> mDeviceList;
    private BTDeviceAdapter mAdapter;
    private BTAsyncServer serverTask;
    private BTAsyncClient clientTask;
    private BTService mBluetoothService;
    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"-----------Start----------");
        setContentView(R.layout.activity_main);
        setup();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mBluetoothDiscoverReceiver);
        //doUnbindService();
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                Log.v(TAG,"result OK!");
                //User enabled bluetooth
                operateBluetooth();
            } else{
                //User did not enable bluetooth
                Log.v(TAG,"result not OK!");
            }
        }


    }

    private void setup(){
        setupInterface();
        startBluetooth();
    }

    /**
     * Sets up GUI for the app
     * Includes the listview to display devices and textviews to show local device
     * also puts buttons for discovery and scanning
     */
    private void setupInterface(){

        getSupportActionBar().setElevation(0);

        mDeviceList = new ArrayList<>();
        mAdapter = new BTDeviceAdapter(this,mDeviceList);
        final ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(mAdapter);
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyBluetoothDevice item = (MyBluetoothDevice) lv.getItemAtPosition(position);

                serverTask.stopServer();
                //clientTask = new BluetoothAsyncClient(getApplicationContext(),item.getDevice(),mBluetoothAdapter, mHandler);
                //clientTask.execute();
                Intent i = new Intent(getApplicationContext(), BTService.class);
                i.putExtra("device", item.getDevice());
                startService(i);
            }
        });

        Button discoverButton = (Button) findViewById(R.id.discovery_button);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDiscoverable();
            }
        });
        findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Log.v(TAG,"Scan started");
                //Clear previous scan results and set the progress bar to show scanning
                ProgressBar ScanBar = (ProgressBar)findViewById(R.id.scanBar);
                ScanBar.setIndeterminate(true);
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();

                mBluetoothAdapter.startDiscovery();
            }
        });

    }

    /**
     * Initialize bluetooth adapter and check if BT is supported
     */
    public void startBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {//BT not supported
            Log.v(TAG, "Bluetooth not supported...");
            Toast.makeText(this,"Bluetooth not supported!", Toast.LENGTH_SHORT).show();
        }else{ //bluetooth is supported
            //get the name and address of the local device
            TextView nameTV = (TextView)findViewById(R.id.my_device_name);
            TextView addressTV = (TextView) findViewById(R.id.my_device_address);

            nameTV.setText(mBluetoothAdapter.getName());
            addressTV.setText(mBluetoothAdapter.getAddress());

            Log.v(TAG,"Bluetooth supported!");
            //see if BT is on, if not then ask to turn it on
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Log.v(TAG,"Bluetooth is on");
                operateBluetooth();
            }
        }//End of supported bluetooth else statement
    }

    public void operateBluetooth(){
        //scanDevices();

        //Set up a listener for bluetooth devices found and for discovery finishing
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBluetoothDiscoverReceiver, filter);
        filter = new IntentFilter(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBluetoothDiscoverReceiver, filter);


        //Intent i = new Intent(getApplicationContext(), BTService.class);
        //i.putExtra("server", true);
        //startService(i);

        //Turn on BT server to listen for incoming connections
        serverTask= new BTAsyncServer(mBluetoothAdapter, mHandler);
        serverTask.execute();

    }

    /**
     * A connection has been opened on a socket. Set up IO AsyncTask
     * @param socket that has been opened by the client or server asynctask
     */
    private void socketConnectSuccess(BluetoothSocket socket){
        Toast.makeText(getApplicationContext(),"Connection Success", Toast.LENGTH_SHORT).show();
        new BTAsyncIO(socket, mHandler).execute();
    }

    /**
     * Displays all pairings the device has
     */
    public void scanDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            Log.i(TAG,"------- Paired Devices --------");
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mAdapter.add(new MyBluetoothDevice(device));
                Log.v(TAG, ">"+device.getName() + " | " + device.getAddress());
            }
            Log.i(TAG,"------------------------------");
        }
    }

    public void makeDiscoverable(){
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /**
     * Reciever for when a device is found or when discovery is finished
     */
    private final BroadcastReceiver mBluetoothDiscoverReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.v(TAG, device.getName() + " | " + device.getAddress());
                // Add the name and address to an array adapter to show in a ListView
                mAdapter.add(new MyBluetoothDevice(device));
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG,"Scan finished");
                ProgressBar ScanBar = (ProgressBar)findViewById(R.id.scanBar);
                ScanBar.setIndeterminate(false);
            }
        }
    };

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int IO_FINISHED = 6;
    public static final int CONNECTION_SUCCESS = 7;
    public static final int CONNECTION_FAILED = 8;


    /**
     * Received messages from the server, client, and IO asynctasks
     * to determine what is going on
     */
    private Handler mHandler  = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i(TAG, "Main / HandleMessage!");
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "Main / MESSAGE_STATE_CHANGE: " + msg.arg1);

                    break;
                //A message has been written to the paired device
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG,"Main / Handler / me: "+writeMessage);
                    Toast.makeText(getApplicationContext(),"me:"+writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                //A message has been received from the other device
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG,"Main / Handler / "+readMessage);
                    Toast.makeText(getApplicationContext(),readMessage, Toast.LENGTH_SHORT).show();
                    break;
                //The connection has been terminated and message transfer stopped
                case IO_FINISHED:
                    Log.d(TAG,"Main / Handler / IO finished");
                    Toast.makeText(getApplicationContext(),"IO Finished", Toast.LENGTH_SHORT).show();
                    break;
                //The creation of the connection is successful
                case CONNECTION_SUCCESS:
                    Log.d(TAG,"Main / Handler / Connection Success");
                    socketConnectSuccess((BluetoothSocket) msg.obj);
                    break;
                //The creation of the connection was not successful
                case CONNECTION_FAILED:
                    Log.d(TAG,"Main / Handler / Connection Failed");
                    Toast.makeText(getApplicationContext(),"Connection Failed", Toast.LENGTH_SHORT).show();
                    break;

            }
            return true;
        }
    });

/*    private BroadcastReceiver IOServiceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(IOService.INTENT_MESSAGE_TYPE);
                switch (resultCode) {
                    case MESSAGE_STATE_CHANGE:
                        //Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        break;
                    case MESSAGE_WRITE:
                        String message_out = bundle.getString(IOService.INTENT_MESSAGE_STRING);
                        Log.d(TAG,"ServiceReceiver / me: "+message_out);
                        Toast.makeText(getApplicationContext(),"me:"+message_out, Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_READ:
                        String message_in = bundle.getString(IOService.INTENT_MESSAGE_STRING);
                        Log.d(TAG,"ServiceReceiver / "+message_in);
                        Toast.makeText(getApplicationContext(),message_in, Toast.LENGTH_SHORT).show();
                        //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                        break;
                    case IO_FINISHED:
                        Log.d(TAG,"Handler / IO finished");
                        Toast.makeText(getApplicationContext(),"IO Finished", Toast.LENGTH_SHORT).show();
                        break;
                    case CONNECTION_SUCCESS:
                        Log.d(TAG,"Handler / Connection Success");
                        //socketConnectSuccess((BluetoothSocket) msg.obj);
                        break;
                    case CONNECTION_FAILED:
                        Log.d(TAG,"Handler / Connection Failed");
                        Toast.makeText(getApplicationContext(),"Connection Failed", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBluetoothService = ((BTService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(getApplicationContext(), "Service connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBluetoothService = null;
            Toast.makeText(getApplicationContext(), "Service disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(MainActivity.this,
                BTService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }*/



}
