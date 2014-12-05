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

    public MyBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG)
            Log.d(TAG, "Intent received");

        Intent newIntent = new Intent(context, ManagerService.class);
        newIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        newIntent.putExtra("CONNEXION_STATE", intent.getAction());
        context.startService(newIntent);
    }

 }
