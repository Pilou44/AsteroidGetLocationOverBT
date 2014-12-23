package com.freak.fidji.locationoverbt;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Vector;

public class MyService extends Service implements SendingThreadListener{

    private static final String TAG = "LOCATION_OVER_BT";
    private static final boolean DEBUG = true;

    private final IBinder mBinder = new LocalBinder();

    private Vector<BluetoothDevice> mConnectedDevices;
    private Vector<SendingThread> mSendingThreads;
    private boolean atLeastOneDeviceConnected;

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG)
            Log.d(TAG, "Create service");

        mConnectedDevices = new Vector<BluetoothDevice>();
        mSendingThreads = new Vector<SendingThread>();
    }

    public void startThread(BluetoothDevice device) {
        if (DEBUG)
            Log.d(TAG, "Device connected");
        mConnectedDevices.add(device);
        SendingThread thread = new SendingThread(this, device);
        thread.setListener(MyService.this);

        if (DEBUG)
            Log.d(TAG, "Start thread for device " + (mConnectedDevices.size() - 1));
        thread.start();
        mSendingThreads.add(thread);
    }

    public void stopThread(BluetoothDevice device) {
        if (DEBUG)
            Log.d(TAG, "Device disconnected, try to identify");
        for (int i = 0 ; i < mConnectedDevices.size() ; i++){
            if (mConnectedDevices.get(i).getAddress().equals(device.getAddress())) {
                if (DEBUG)
                    Log.d(TAG, "Device identified, try to stop thread");
                mSendingThreads.get(i).disconnect();
                break;
            }
        }
    }

    @Override
    public void onThreadFinished(BluetoothDevice device) {
        if (DEBUG)
            Log.d(TAG, "onThreadFinished for device " + device.getAddress());
        for (int i = 0 ; i < mConnectedDevices.size() ; i++){
            if (mConnectedDevices.get(i).getAddress().equals(device.getAddress())) {
                if (DEBUG)
                    Log.d(TAG, "Device identified, remove thread");
                mConnectedDevices.remove(i);
                mSendingThreads.remove(i);
                break;
            }
        }
        if (mConnectedDevices.size() == 0){
            if (DEBUG)
                Log.d(TAG, "No more device");

            if (atLeastOneDeviceConnected) {
                if (DEBUG)
                    Log.d(TAG, "Receive thread stopped,make service killable again");
                stopForeground(true);
                atLeastOneDeviceConnected = false;
            }
        }

    }

    @Override
    public void onConnected(BluetoothDevice device) {
        if (!atLeastOneDeviceConnected) {
            if (DEBUG)
                Log.d(TAG, "A device has successfully connected, make service not killable");
            atLeastOneDeviceConnected = true;

            Notification notification = new Notification(R.drawable.ic_launcher, this.getString(R.string.notif_title), System.currentTimeMillis());
            PendingIntent pi = PendingIntent.getService(this, 0, null, 0);
            notification.setLatestEventInfo(this, this.getString(R.string.notif_title), this.getString(R.string.notif_text), pi);

            startForeground(291112, notification);
        }
    }
}
