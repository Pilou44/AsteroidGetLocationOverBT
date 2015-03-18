package com.freak.android.getlocation;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;

public class ReceiveThread extends Thread {

    private static final String TAG = "RECEIVE_THREAD";
    private static final boolean DEBUG = true;
    private static final int CONNECTION_TIMEOUT = 120000;
    private static final long SLEEP_TIME = 100;

    private final Context mContext;
    private final BluetoothServerSocket mServerSocket;
    private final StatisticsManager mStatsManager;
    private boolean mRunning;
    private ReceiveThreadListener mListener = null;

    public ReceiveThread(Context context, BluetoothServerSocket serverSocket) {
        if (DEBUG)
            Log.d(TAG, "Create thread");
        mContext = context;
        mServerSocket = serverSocket;
        mStatsManager = ((GetLocationApplication)context.getApplicationContext()).getStatisticsManager();
        mRunning = false;
    }

    @Override
    public void run() {
        Log.i(TAG, "Run thread");

        SharedPreferences pref = mContext.getSharedPreferences(mContext.getString(R.string.key_preferences), 0);
        SharedPreferences.Editor editor = pref.edit();

        int transaction;
        double latitude;
        double longitude;
        double altitude;
        double accuracy;
        long time;
        boolean okReading;

        BluetoothSocket socket = null;

        try {
            if (DEBUG)
                Log.d(TAG, "Waiting for connection");
            socket = mServerSocket.accept(CONNECTION_TIMEOUT);
        } catch (IOException e) {
            Log.e(TAG, "No connection for " + (CONNECTION_TIMEOUT / 1000) + " s, aborting");
            e.printStackTrace();

            mStatsManager.incConnectionTimeout();

            mRunning = false;
        }

        // If a connection was accepted
        if (socket != null) {
            if (DEBUG)
                Log.d(TAG, "Connected");

            if (mListener != null){
                mListener.onClientConnected();
            }

            DataInputStream dIn = null;
            if (DEBUG)
                Log.d(TAG, "Create input reader");
            try {
                dIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                mRunning = false;
            }

            long lastTime = 0;
            while (mRunning) {
                try {
                    if (DEBUG)
                        Log.d(TAG, "Read datas");
                    transaction = dIn.readInt();
                    latitude = dIn.readDouble();
                    longitude = dIn.readDouble();
                    altitude = dIn.readDouble();
                    accuracy = dIn.readDouble();
                    time = dIn.readLong();
                    okReading = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    transaction = 0;
                    latitude = 0;
                    longitude = 0;
                    altitude = 0;
                    accuracy = 0;
                    time = 0;
                    okReading = false;
                }

                if (okReading) {
                    if (DEBUG)
                        Log.d(TAG, "Calculate time between two receptions");
                    long currentTime = new GregorianCalendar().getTimeInMillis();
                    if (lastTime > 0) {
                        int timeToReceive = (int)(currentTime - lastTime);
                        if (timeToReceive < mStatsManager.getMinTimeToReceive()) {
                            mStatsManager.setMinTimeToReceive(timeToReceive);
                        }
                        if (timeToReceive > mStatsManager.getMaxTimeToReceive()) {
                            mStatsManager.setMaxTimeToReceive(timeToReceive);
                        }
                        mStatsManager.setLastTimeToReceive(timeToReceive);
                    }
                    lastTime = currentTime;

                    if (DEBUG) {
                        Log.d(TAG, "Store location");
                        Log.d(TAG, "Transaction number " + transaction);
                        Log.d(TAG, "Latitude\t: " + latitude);
                        Log.d(TAG, "Longitude\t: " + longitude);
                        Log.d(TAG, "Altitude\t: " + altitude);
                        Log.d(TAG, "Accuracy\t: " + accuracy);
                        Log.d(TAG, "Time\t: " + time);
                    }
                    editor.putLong(mContext.getString(R.string.key_latitude), Double.doubleToRawLongBits(latitude));
                    editor.putLong(mContext.getString(R.string.key_longitude), Double.doubleToRawLongBits(longitude));
                    editor.putLong(mContext.getString(R.string.key_accuracy), Double.doubleToRawLongBits(accuracy));
                    editor.putLong(mContext.getString(R.string.key_date), new GregorianCalendar().getTimeInMillis());
                    editor.apply();
                    mStatsManager.incReceivedLocations();
                }

                try {
                    if (DEBUG)
                        Log.d(TAG, "Sleep " + SLEEP_TIME + " ms");
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } // while (mRunning)

            try {
                if (DEBUG)
                    Log.d(TAG, "All done, close client socket");
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing client socket");
                e.printStackTrace();
            }

        } // if (socket != null)

        try {
            if (DEBUG)
                Log.d(TAG, "All done, close server socket");
            mServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing server socket");
            e.printStackTrace();
        }

        Log.i(TAG, "End of thread");
        if (mListener != null){
            mListener.onThreadFinished();
        }
    }

    @Override
    public synchronized void start() {
        if (DEBUG)
            Log.d(TAG, "Start thread");
        mRunning = true;
        super.start();
    }

    public void cancel() {
        if (DEBUG)
            Log.d(TAG, "Stop thread");
        mRunning = false;
    }

    public void setListener(ReceiveThreadListener listener) {
        this.mListener = listener;
    }
}
