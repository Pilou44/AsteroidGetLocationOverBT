package com.freak.fidji.locationoverbt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by GBeguin on 27/11/2014.
 */
public class SendingThread extends Thread {

    private static final  String TAG = "SENDING_THREAD";
    private static final UUID mUuid = UUID.fromString("4364cf1a-7621-11e4-b116-123b93f75cba");
    private final BluetoothDevice mDevice;
    private final Context mContext;
    private BluetoothSocket mSocket;
    private boolean mRunning;
    private SendingThreadListener mListener;

    public SendingThread(Context context, BluetoothDevice device) {
        Log.i(TAG, "Create thread for device " + device.getAddress());
        mContext = context;
        mDevice = device;
        mRunning = false;
        mListener = null;


        Log.i(TAG, "Create socket for device " + mDevice.getAddress());        BluetoothSocket tmp = null;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(mUuid);
        } catch (IOException e) { }
        mSocket = tmp;
    }

    @Override
    public synchronized void start() {
        Log.i(TAG, "Start thread for device " + mDevice.getAddress());
        mRunning = true;
        super.start();
    }

    @Override
    public void run() {
        boolean connected = false;
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        while (mRunning == true) {
            Log.i(TAG, "Retrieve location " + mDevice.getAddress());
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                Log.i(TAG, "Convert location to byte array " + mDevice.getAddress());
                // https://gist.github.com/jacklt/6711967
                Parcel parcel = Parcel.obtain();
                location.writeToParcel(parcel, 0);
                byte[] bytes = parcel.marshall();

                Log.i(TAG, "Connect to socket " + mDevice.getAddress());
                try {
                    mSocket.connect();
                    connected = true;
                } catch (IOException e) {
                    Log.i(TAG, "Error while connecting for device " + mDevice.getAddress());
                    connected = false;
                    e.printStackTrace();
                }

                if (connected) {
                    Log.i(TAG, "Send location " + mDevice.getAddress());
                    try {
                        DataOutputStream dOut = new DataOutputStream(mSocket.getOutputStream());
                        dOut.write(bytes, 0, bytes.length);
                        dOut.flush();
                        dOut.close();
                    } catch (IOException e) {
                        Log.i(TAG, "Error while sending datas for device " + mDevice.getAddress());
                        e.printStackTrace();
                    }
                }

                try {
                    Log.i(TAG, "Close socket for device " + mDevice.getAddress());
                    mSocket.close();
                } catch (IOException e) {
                    Log.i(TAG, "Error while closing socket " + mDevice.getAddress());
                    e.printStackTrace();
                }

            }

            try {
                Log.i(TAG, "Sleep 10s for device " + mDevice.getAddress());
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "Thread stopped for device " + mDevice.getAddress());
        if (mListener != null) {
            mListener.onThreadFinished(mDevice);
        }
    }

    public void disconnect() {
        Log.i(TAG, "Stop thread for device " + mDevice.getAddress());
        mRunning = false;
    }

    public void setListener(SendingThreadListener listener) {
        mListener = listener;
    }
}
