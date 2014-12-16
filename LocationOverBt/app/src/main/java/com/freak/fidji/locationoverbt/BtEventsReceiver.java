package com.freak.fidji.locationoverbt;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BtEventsReceiver extends BroadcastReceiver {
    public static final String ACTION_EXTRA = "CONNECTION_STATE";
    private static final String TAG = "BOOT_RECEIVER";
    private static final boolean DEBUG = true;

    public BtEventsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Intent received");

        Intent newIntent = new Intent(context, ManagerService.class);
        newIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        newIntent.putExtra(ACTION_EXTRA, intent.getAction());
        context.startService(newIntent);
    }

 }
