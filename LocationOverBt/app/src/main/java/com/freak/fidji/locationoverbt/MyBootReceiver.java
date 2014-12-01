package com.freak.fidji.locationoverbt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBootReceiver extends BroadcastReceiver {
    private static final String TAG = "BOOT_RECEIVER";

    public MyBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Boot received");
        Intent newIntent = new Intent(context, MyService.class);
        context.startService(newIntent);
    }
}
