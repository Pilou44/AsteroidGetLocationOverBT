package com.freak.android.getlocation;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class MyService extends Service {
    private static final String TAG = "MY_SERVICE";
    private static final boolean DEBUG = true;

    private static final UUID mUuid = UUID.fromString("4364cf1a-7621-11e4-b116-123b93f75cba");

    private BluetoothServerSocket mServerSocket;
    private ReceiveThread mThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG, "Start service");

        Notification notif = new Notification.Builder(this)
                .setContentTitle("Get location over BT")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        if (DEBUG)
            Log.d(TAG, "Create socket");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(bluetoothAdapter.getName(), mUuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServerSocket = tmp;

        if (DEBUG)
            Log.d(TAG, "Create thread");
        mThread = new ReceiveThread(this, mServerSocket);
        mThread.start();

        startForeground(291112, notif);
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Destroy service");

        stopForeground(true);

        if (mThread != null) {
            mThread.cancel();
        }

        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

}
