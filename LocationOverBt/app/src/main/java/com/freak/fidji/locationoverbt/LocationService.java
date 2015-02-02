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

public class LocationService extends Service implements SendingThreadListener{

    private static final String TAG = "LOCATION_OVER_BT";
    private static final boolean DEBUG = true;

    private final IBinder mBinder = new LocalBinder();

    private Vector<LocationDevice> mConnectedDevices;
    private Vector<SendingThread> mSendingThreads;
    private boolean atLeastOneDeviceConnected;

    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
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

        mConnectedDevices = new Vector<LocationDevice>();
        mSendingThreads = new Vector<SendingThread>();
    }

    public void startThread(BluetoothDevice device) {
        if (DEBUG)
            Log.d(TAG, "Device connected");
        LocationDevice locationDevice = new LocationDevice(device, LocationDevice.STATE_CONNECTED);
        mConnectedDevices.add(locationDevice);
        SendingThread thread = new SendingThread(this, locationDevice);
        thread.setListener(this);

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
                mConnectedDevices.get(i).setState(LocationDevice.STATE_DISCONNECTED);
                break;
            }
        }
    }

    @Override
    public void onThreadFinished(LocationDevice device) {
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
    public void onConnected(LocationDevice device) {
        if (!atLeastOneDeviceConnected) {
            if (DEBUG)
                Log.d(TAG, "A device has successfully connected, make service not killable");
            atLeastOneDeviceConnected = true;

            Notification notification = new Notification(R.drawable.ic_launcher, this.getString(R.string.notif_title), System.currentTimeMillis());
            Intent intent = new Intent(this, StateActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            notification.setLatestEventInfo(this, this.getString(R.string.notif_title), this.getString(R.string.notif_text), pi);

            startForeground(291112, notification);
        }
    }

    public Vector<LocationDevice> getDevices() {
        return mConnectedDevices;
    }
}
