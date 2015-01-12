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

public class ReceiveThread extends Thread {

    private static final String TAG = "RECEIVE_THREAD";
    private static final boolean DEBUG = true;
    public static final int TIMEOUT_IN_SECONDS = 8;
    private static final int TIME_TO_WAIT = 100;
    private static final int MAX_WAITING_LOOPS = TIMEOUT_IN_SECONDS * 1000 / TIME_TO_WAIT;
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int MAX_CONNECTION_ATTEMPT = 3;

    private final Context mContext;
    private final BluetoothServerSocket mServerSocket;
    private boolean mRunning;
    private ReceiveThreadListener mListener = null;

    public ReceiveThread(Context context, BluetoothServerSocket serverSocket) {
        if (DEBUG)
            Log.d(TAG, "Create thread");
        mContext = context;
        mServerSocket = serverSocket;
        mRunning = false;
    }

    @Override
    public void run() {
        Log.i(TAG, "Run thread");

        SharedPreferences pref = mContext.getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();

        if (DEBUG)
            Log.d(TAG, "Load statistics");
        int connectionTimeout = pref.getInt("connection_timeout", 0);
        int connectionAbort = pref.getInt("connection_abort", 0);
        int receivedMessages = pref.getInt("received_messages", 0);
        int receivedLocations = pref.getInt("received_locations", 0);
        int connectionOpen = pref.getInt("connection_open", 0);
        int threadAbort = pref.getInt("thread_abort", 0);
        int minTimeToReceive = pref.getInt("min_time", TIMEOUT_IN_SECONDS * 1000);
        int maxTimeToReceive = pref.getInt("max_time", 0);
        int lastTimeToReceive;
        int corruptedDatas = pref.getInt("corrupted_datas", 0);

        BluetoothSocket socket;
        byte[] buffer = new byte[100];
        int attempt = 0;
        // Keep listening until exception occurs or a socket is returned
        while (mRunning) {
            socket = null;
            try {
                if (DEBUG)
                    Log.d(TAG, "Waiting for connection");
                socket = mServerSocket.accept(CONNECTION_TIMEOUT);
            } catch (IOException e) {
                Log.e(TAG, "Error while waiting for connection");
                e.printStackTrace();
                attempt++;
                connectionTimeout++;
                editor.putInt("connection_timeout", connectionTimeout);
                editor.apply();
                if (attempt == MAX_CONNECTION_ATTEMPT) {
                    mRunning = false;
                    threadAbort++;
                    editor.putInt("thread_abort", threadAbort);
                    editor.apply();
                    Log.e(TAG, "Too much attempt, stop thread");
                }
            }

            // If a connection was accepted
            if (socket != null) {
                if (DEBUG)
                    Log.d(TAG, "Connected");
                connectionOpen++;
                editor.putInt("connection_open", connectionOpen);
                editor.apply();

                attempt = 0;
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
                }

                if (dIn != null) {
                    int loop = 0;
                    try {
                        while ((dIn.available() == 0) && (loop < MAX_WAITING_LOOPS)) {
                            if (DEBUG)
                                Log.d(TAG, "Wait for datas, loop number " + loop);
                            Thread.sleep(TIME_TO_WAIT);
                            loop++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        loop = MAX_WAITING_LOOPS;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        loop = MAX_WAITING_LOOPS;
                    }

                    if (loop < MAX_WAITING_LOOPS) {
                        int index = 0;

                        lastTimeToReceive = loop * TIME_TO_WAIT;
                        if (lastTimeToReceive < minTimeToReceive)
                            minTimeToReceive = lastTimeToReceive;
                        if (lastTimeToReceive > maxTimeToReceive)
                            maxTimeToReceive = lastTimeToReceive;

                        editor.putInt("min_time", minTimeToReceive);
                        editor.putInt("max_time", maxTimeToReceive);
                        editor.putInt("last_time", lastTimeToReceive);
                        editor.apply();

                        try {
                            if (DEBUG)
                                Log.d(TAG, "Read datas");
                            while (dIn.available() > 0) {
                                buffer[index] = dIn.readByte();
                                index++;
                            }
                            if (DEBUG)
                                Log.d(TAG, index + " bytes read");
                        } catch (IOException e) {
                            e.printStackTrace();
                            corruptedDatas++;
                            editor.putInt("corrupted_datas", corruptedDatas);
                            editor.apply();
                        }

                        if (index > 0) {
                            try {
                                if (DEBUG)
                                    Log.d(TAG, "Convert to Location");
                                Parcel parcel = Parcel.obtain();
                                parcel.unmarshall(buffer, 0, index);
                                parcel.setDataPosition(0); // this is extremely important!
                                Location location = Location.CREATOR.createFromParcel(parcel);

                                if (DEBUG)
                                    Log.d(TAG, "Store location");
                                editor.putLong("latitude", Double.doubleToRawLongBits(location.getLatitude()));
                                editor.putLong("longitude", Double.doubleToRawLongBits(location.getLongitude()));
                                editor.putLong("accuracy", Double.doubleToRawLongBits(location.getAccuracy()));
                                receivedLocations++;
                                editor.putInt("received_locations", receivedLocations);
                                editor.apply();
                            } catch (Exception e) { // TODO Get the correct exception when text is received
                                e.printStackTrace();
                                String string = new String (buffer, 0, index);
                                receivedMessages++;
                                editor.putInt("received_messages", receivedMessages);
                                editor.apply();
                                if (DEBUG)
                                    Log.d(TAG, "Received message = " + string);
                            }
                        }
                    }
                    else {
                        Log.e(TAG, "Too much waiting loops");
                        connectionAbort++;
                        editor.putInt("connection_abort", connectionAbort);
                        editor.apply();
                    }
                }

                try {
                    if (DEBUG)
                        Log.d(TAG, "All done, close client socket");
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error while closing client socket");
                    e.printStackTrace();
                }

            }
        }
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
