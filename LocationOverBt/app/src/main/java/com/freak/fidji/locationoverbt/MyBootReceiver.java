package com.freak.fidji.locationoverbt;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MyBootReceiver extends BroadcastReceiver {
    private static final String TAG = "BOOT_RECEIVER";
    private static final boolean DEBUG = true;
    private String mAction;
    private Context mContext;
    private BluetoothDevice mDevice;

    public MyBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG)
            Log.i(TAG, "Intent received");

        mContext = context;
        Intent newIntent = new Intent(context, MyService.class);
        mContext.startService(newIntent);
        mAction = intent.getAction();
        mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DEBUG)
                Log.i(TAG, "Service connected");

            MyService myService = ((MyService.LocalBinder)service).getService();
            if (mAction.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
                myService.startThread(mDevice);
            }
            else if (mAction.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                myService.stopThread(mDevice);
            }
            mContext.unbindService(mConnection);
        }

        public void onServiceDisconnected(ComponentName className) {
            if (DEBUG)
                Log.i(TAG, "Service disconnected");
        }
    };

}
