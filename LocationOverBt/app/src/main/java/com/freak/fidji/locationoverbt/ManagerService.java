package com.freak.fidji.locationoverbt;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ManagerService extends IntentService {

    private static final boolean DEBUG = true;
    private static final String TAG = "MANAGER_SERVICE";

    private String mState;
    private BluetoothDevice mDevice;

    public ManagerService() {
        super("ManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Create service, handle intent");
        if (intent != null) {
            mState = intent.getStringExtra(MyBootReceiver.ACTION_EXTRA);
            mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Intent serviceIntent = new Intent(this, MyService.class);
            if (DEBUG)
                Log.d(TAG, "Start service");
            this.startService(serviceIntent);
            if (DEBUG)
                Log.d(TAG, "Bind service");
            this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DEBUG)
                Log.d(TAG, "Service connected");

            MyService myService = ((MyService.LocalBinder)service).getService();
            if (mState.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
                myService.startThread(mDevice);
            }
            else if (mState.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                myService.stopThread(mDevice);
            }
            ManagerService.this.unbindService(mConnection);
        }

        public void onServiceDisconnected(ComponentName className) {
            if (DEBUG)
                Log.d(TAG, "Service disconnected");
        }
    };

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Destroy service");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG, "Create service");
    }
}
