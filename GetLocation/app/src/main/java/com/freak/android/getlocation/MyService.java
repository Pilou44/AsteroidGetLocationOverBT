package com.freak.android.getlocation;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by GBeguin on 27/11/2014.
 */
public class MyService extends Service implements ReceiveThreadListener {
    private static final String TAG = "MY_SERVICE";
    private static final UUID mUuid = UUID.fromString("4364cf1a-7621-11e4-b116-123b93f75cba");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private ReceiveThread thread;
    private int startId;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Start service");
        //Toast.makeText(this, "BT on, start service", Toast.LENGTH_SHORT).show();

        Notification notif = new Notification.Builder(this)
                .setContentTitle("Get location over BT")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        Log.d(TAG, "Create socket");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mBluetoothAdapter.getName(), mUuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServerSocket = tmp;

        Log.d(TAG, "Create thread");
        Toast.makeText(this, "Listening to BT RFCOMM, create thread", Toast.LENGTH_SHORT).show();
        thread = new ReceiveThread(this, mServerSocket);
        thread.setListener(this);
        thread.start();

        startForeground(291112, notif);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy service");
        //Toast.makeText(this, "Destroy service ", Toast.LENGTH_SHORT).show();

        stopForeground(true);

        if (thread != null) {
            thread.cancel();
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
        Log.d(TAG, "Thread finished");
        Toast.makeText(this, "Thread finished", Toast.LENGTH_SHORT).show();
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClientConnected() {
        Log.d(TAG, "Client connected");
        Toast.makeText(this, "Client connected", Toast.LENGTH_SHORT).show();
    }
}
