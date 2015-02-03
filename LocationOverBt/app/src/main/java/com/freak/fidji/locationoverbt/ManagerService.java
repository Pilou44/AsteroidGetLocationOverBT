package com.freak.fidji.locationoverbt;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ManagerService extends IntentService implements ServiceConnection{

    private static final boolean DEBUG = true;
    private static final String TAG = "MANAGER_SERVICE";

    private String mState;
    private BluetoothDevice mDevice;
    private boolean isBound;

    public ManagerService() {
        super("ManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Create service, handle intent");
        if (intent != null) {
            mState = intent.getStringExtra(BtEventsReceiver.ACTION_EXTRA);
            mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Intent serviceIntent = new Intent(this, LocationService.class);
            if (DEBUG)
                Log.d(TAG, "Start service");
            this.startService(serviceIntent);
            if (DEBUG)
                Log.d(TAG, "Bind service");
            this.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

            while (!isBound){
                try {
                    if (DEBUG)
                        Log.d(TAG, "Sleep");
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onServiceConnected(ComponentName className, IBinder service) {
        if (DEBUG)
            Log.d(TAG, "Service connected");

        LocationService locationService = ((LocationService.LocalBinder)service).getService();
        if (mState.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            if (DEBUG)
                Log.d(TAG, "Start thread");
            locationService.startThread(mDevice);
        }
        else if (mState.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            if (DEBUG)
                Log.d(TAG, "Stop thread");
            locationService.stopThread(mDevice);
        }
        else {
            if (DEBUG)
                Log.d(TAG, "Unknown action");
        }
        isBound = true;
    }

    public void onServiceDisconnected(ComponentName className) {
        if (DEBUG)
            Log.d(TAG, "Service disconnected");
        isBound = false;
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Destroy service");
        if (isBound)
            unbindService(this);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG, "Create service");
    }
}
