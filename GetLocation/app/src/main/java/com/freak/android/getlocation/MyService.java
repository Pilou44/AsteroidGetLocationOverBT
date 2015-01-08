package com.freak.android.getlocation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class MyService extends Service implements ReceiveThreadListener {
    private static final String TAG = "MY_SERVICE";
    private static final boolean DEBUG = true;

    private static final UUID mUuid = UUID.fromString("4364cf1a-7621-11e4-b116-123b93f75cba");

    private BluetoothServerSocket mServerSocket;
    private ReceiveThread mThread;
    private boolean mAlreadyConnected;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG, "Start service");
        mAlreadyConnected = false;

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
        mThread.setListener(this);
        mThread.start();

    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Destroy service");

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

    @Override
    public void onThreadFinished() {
        if (mAlreadyConnected) {
            if (DEBUG)
                Log.d(TAG, "Receive thread stopped,make service killable again");
            stopForeground(true);
            mAlreadyConnected = false;
        }
    }

    @Override
    public void onClientConnected() {
        if (!mAlreadyConnected) {
            if (DEBUG)
                Log.d(TAG, "First connection successful, make service not killable");

            Intent intent = new Intent(this, MyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            mAlreadyConnected = true;
            Notification notif = new Notification.Builder(this)
                    .setContentTitle(this.getString(R.string.notif_title))
                    .setContentText(this.getString(R.string.notif_text))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .build();

            startForeground(291112, notif);
        }
    }
}
