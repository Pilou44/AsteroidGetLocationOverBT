package com.freak.android.getlocation;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Parcel;
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

        SharedPreferences pref = mContext.getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();

        int nbBytes;

        BluetoothSocket socket = null;
        byte[] buffer = new byte[100];

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
                    nbBytes = dIn.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    nbBytes = 0;
                }

                if (DEBUG)
                    Log.d(TAG, nbBytes + " bytes read");

                if (nbBytes > 0) {
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

                    if (DEBUG)
                        Log.d(TAG, "Convert to Location");
                    Parcel parcel = Parcel.obtain();
                    parcel.unmarshall(buffer, 0, nbBytes);
                    parcel.setDataPosition(0); // this is extremely important!
                    Location location = Location.CREATOR.createFromParcel(parcel);

                    if (DEBUG)
                        Log.d(TAG, "Store location");
                    editor.putLong("latitude", Double.doubleToRawLongBits(location.getLatitude()));
                    editor.putLong("longitude", Double.doubleToRawLongBits(location.getLongitude()));
                    editor.putLong("accuracy", Double.doubleToRawLongBits(location.getAccuracy()));
                    editor.apply();
                    mStatsManager.incReceivedLocations();
                }
                else {
                    if (DEBUG)
                        Log.d(TAG, "No data to read");
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
