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
    private static final int TIMEOUT_IN_SECONDS = 5;
    private static final long TIME_TO_WAIT = 100;
    private static final int MAX_WAITING_LOOPS = TIMEOUT_IN_SECONDS * 1000 / ((int)TIME_TO_WAIT);

    private final Context mContext;
    private final BluetoothServerSocket mServerSocket;
    private boolean mRunning;

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
        BluetoothSocket socket;
        byte[] buffer = new byte[100];
        // Keep listening until exception occurs or a socket is returned
        while (mRunning) {
            try {
                if (DEBUG)
                    Log.d(TAG, "Waiting for connection");
                socket = mServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                mRunning = false;
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                if (DEBUG)
                    Log.d(TAG, "Read datas");
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
                        try {
                            if (DEBUG)
                                Log.d(TAG, "Read datas");
                            while (dIn.available() > 0) {
                                buffer[index] = dIn.readByte();
                                index++;
                            }
                            if (DEBUG)
                                Log.d(TAG, index + " bytes read");

                            if (DEBUG)
                                Log.d(TAG, "Convert to Location");
                            Parcel parcel = Parcel.obtain();
                            parcel.unmarshall(buffer, 0, index);
                            parcel.setDataPosition(0); // this is extremely important!
                            Location location = Location.CREATOR.createFromParcel(parcel);

                            if (DEBUG)
                                Log.d(TAG, "Store location");
                            SharedPreferences pref = mContext.getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putLong("latitude", Double.doubleToRawLongBits(location.getLatitude()));
                            editor.putLong("longitude", Double.doubleToRawLongBits(location.getLongitude()));
                            editor.putLong("accuracy", Double.doubleToRawLongBits(location.getAccuracy()));
                            editor.apply();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Log.e(TAG, "Too much waiting loops");
                    }
                }

                try {
                    if (DEBUG)
                        Log.d(TAG, "All done, close sockets");
                    socket.close();
                    mServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error while closing sockets");
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG, "End of thread");
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

}
