package com.freak.android.getlocation;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "BT_RECEIVER";

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent received");
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        //int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);

        if (state == BluetoothAdapter.STATE_ON) {
            Log.i(TAG, "BT on");
            Intent newIntent = new Intent(context, MyService.class);
            context.startService(newIntent);
        }
        else if (state == BluetoothAdapter.STATE_OFF) {
            Log.i(TAG, "BT off");
            Intent newIntent = new Intent(context, MyService.class);
            context.stopService(newIntent);
        }
    }
}
