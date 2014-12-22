package com.freak.android.getlocation;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "BT_RECEIVER";
    private static final boolean DEBUG = true;

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Intent received");
        int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
        int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

        if (connectionState == BluetoothAdapter.STATE_CONNECTED) {
            if (DEBUG)
                Log.d(TAG, "Connected to BT device");
            Intent newIntent = new Intent(context, MyService.class);
            context.startService(newIntent);
        }
        else if (connectionState == BluetoothAdapter.STATE_DISCONNECTED) {
            if (DEBUG)
                Log.d(TAG, "Disconnected from BT device");
            Intent newIntent = new Intent(context, MyService.class);
            context.stopService(newIntent);
        }
        else if (btState == BluetoothAdapter.STATE_OFF) {
            if (DEBUG)
                Log.d(TAG, "BT off");
            Intent newIntent = new Intent(context, MyService.class);
            context.stopService(newIntent);
        }

    }
}
