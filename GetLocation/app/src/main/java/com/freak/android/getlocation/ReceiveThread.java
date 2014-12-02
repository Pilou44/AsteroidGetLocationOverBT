package com.freak.android.getlocation;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by GBeguin on 27/11/2014.
 */
public class ReceiveThread extends Thread {

    private static final String TAG = "RECEIVE_THREAD";
    private final Context mContext;
    private final BluetoothServerSocket mSocket;
    private boolean running;

    public ReceiveThread(Context context, BluetoothServerSocket serverSocket) {
        Log.i(TAG, "Create thread");
        mContext = context;
        mSocket = serverSocket;
        running = false;
    }

    @Override
    public void run() {
        Log.i(TAG, "Run thread");
        BluetoothSocket socket = null;
        byte[] buffer = new byte[100];
        // Keep listening until exception occurs or a socket is returned
        while (running) {
            try {
                Log.i(TAG, "Waiting for connection");
                socket = mSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                running = false;
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                try {
                    Log.i(TAG, "Read datas");
                    DataInputStream dIn = new DataInputStream(socket.getInputStream());

                    while (socket.isConnected()) {
                        int size = dIn.read(buffer, 0, buffer.length);

                        Log.i(TAG, "Convert to Location");
                        Parcel parcel = Parcel.obtain();
                        parcel.unmarshall(buffer, 0, size);
                        parcel.setDataPosition(0); // this is extremely important!
                        Location location = Location.CREATOR.createFromParcel(parcel);

                        Log.i(TAG, "Store location");
                        SharedPreferences pref = mContext.getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putLong("latitude", Double.doubleToRawLongBits(location.getLatitude()));
                        editor.putLong("longitude", Double.doubleToRawLongBits(location.getLongitude()));
                        editor.putLong("accuracy", Double.doubleToRawLongBits(location.getAccuracy()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public synchronized void start() {
        Log.i(TAG, "Start thread");
        running = true;
        super.start();
    }

    public void cancel() {
        Log.i(TAG, "Stop thread");
        running = false;
    }

}
