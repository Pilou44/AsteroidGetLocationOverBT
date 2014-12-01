package com.freak.fidji.locationoverbt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.Vector;

public class MyService extends Service implements SendingThreadListener{

    private static final String TAG = "LOCATION_OVER_BT";

    private Vector<BluetoothDevice> mConnectedDevices;
    private Vector<SendingThread> mSendingThreads;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Create service");

        mConnectedDevices = new Vector<BluetoothDevice>();
        mSendingThreads = new Vector<SendingThread>();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {

            Log.d(TAG, "Instantiate filters");
            IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

            Log.d(TAG, "Register receivers");
            this.registerReceiver(mReceiver, filter1);
            this.registerReceiver(mReceiver, filter2);
            this.registerReceiver(mReceiver, filter3);

        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Unregister receiver");
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(TAG, "Device connected");
                mConnectedDevices.add(device);
                SendingThread thread = new SendingThread(context, device);
                thread.setListener(MyService.this);

                Log.d(TAG, "Start thread for device " + (mConnectedDevices.size() - 1));
                thread.start();
                mSendingThreads.add(thread);
            }
            else if ((BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) || (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))) {
                Log.d(TAG, "Device disconnected, try to identify");
                for (int i = 0 ; i < mConnectedDevices.size() ; i++){
                    if (mConnectedDevices.get(i).getAddress().equals(device.getAddress())) {
                        Log.d(TAG, "Device identified, try to stop thread");
                        mSendingThreads.get(i).disconnect();
                        break;
                    }
                }
            }
        }
    };

    @Override
    public void onThreadFinished(BluetoothDevice device) {
        Log.d(TAG, "onThreadFinished for device " + device.getAddress());
        for (int i = 0 ; i < mConnectedDevices.size() ; i++){
            if (mConnectedDevices.get(i).getAddress().equals(device.getAddress())) {
                Log.d(TAG, "Device identified, remove thread");
                mConnectedDevices.remove(i);
                mSendingThreads.remove(i);
                break;
            }
        }
    }
}
