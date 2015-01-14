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

public class SendingThread extends Thread {

    private static final  String TAG = "SENDING_THREAD";
    private static final boolean DEBUG = true;
    private static final UUID MY_UUID = UUID.fromString("4364cf1a-7621-11e4-b116-123b93f75cba");
    private static final long TIME_BETWEEN_TO_SENDS = 15000;
    private static final long WAIT_FOR_SERVER = 30000;
    private final BluetoothDevice mDevice;
    private final Context mContext;
    private boolean mRunning;
    private SendingThreadListener mListener;

    public SendingThread(Context context, BluetoothDevice device) {
        if (DEBUG)
            Log.d(TAG, "Create thread for device " + device.getAddress());
        mContext = context;
        mDevice = device;
        mRunning = false;
        mListener = null;
    }

    @Override
    public synchronized void start() {
        Log.i(TAG, "Start thread for device " + mDevice.getAddress());
        mRunning = true;
        super.start();
    }

    @Override
    public void run() {
        BluetoothSocket tmp, socket;
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (DEBUG)
            Log.d(TAG, "Create socket for device " + mDevice.getAddress());
        tmp = null;
        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = tmp;

        if (socket != null) {
            try {
                if (DEBUG)
                    Log.d(TAG, "Sleep " + (WAIT_FOR_SERVER / 1000) + " s for server on  device " + mDevice.getAddress() + " to be ready");
                Thread.sleep(WAIT_FOR_SERVER);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (DEBUG)
                Log.d(TAG, "Connect socket " + mDevice.getAddress());
            try {
                socket.connect();
                if (mListener != null) {
                    if (DEBUG)
                        Log.d(TAG, "Inform service that connection is successful for device " + mDevice.getAddress());
                    mListener.onConnected(mDevice);
                }
            } catch (IOException e) {
                mRunning = false;
                Log.e(TAG, "Error while connecting for device " + mDevice.getAddress());
                e.printStackTrace();
            }

            while (mRunning) {
                if (DEBUG)
                    Log.d(TAG, "Retrieve location " + mDevice.getAddress());
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    if (DEBUG)
                        Log.d(TAG, "Convert location to byte array " + mDevice.getAddress());
                    Parcel parcel = Parcel.obtain();
                    location.writeToParcel(parcel, 0);
                    byte[] bytes = parcel.marshall();

                    if (DEBUG)
                        Log.d(TAG, "Send datas " + mDevice.getAddress());
                    try {
                        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                        dOut.write(bytes, 0, bytes.length);
                        dOut.flush();
                        dOut.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error while sending datas for device " + mDevice.getAddress());
                        e.printStackTrace();
                    }
                }
                else {
                    if (DEBUG)
                        Log.d(TAG, "No location to send to " + mDevice.getAddress());
                }
                
                try {
                    if (DEBUG)
                        Log.d(TAG, "Sleep " + (TIME_BETWEEN_TO_SENDS / 1000) + " s for device " + mDevice.getAddress());
                    Thread.sleep(TIME_BETWEEN_TO_SENDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } // while (mRunning)

            try {
                if (DEBUG)
                    Log.d(TAG, "Close socket for device " + mDevice.getAddress());
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing socket " + mDevice.getAddress());
                e.printStackTrace();
            }

        } // if (socket != null)

        Log.i(TAG, "Thread stopped for device " + mDevice.getAddress());
        if (mListener != null) {
            mListener.onThreadFinished(mDevice);
        }
    }

    public void disconnect() {
        if (DEBUG)
            Log.d(TAG, "Stop thread for device " + mDevice.getAddress());
        mRunning = false;
    }

    public void setListener(SendingThreadListener listener) {
        mListener = listener;
    }
}
