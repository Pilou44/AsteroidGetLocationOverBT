package com.freak.fidji.locationoverbt;

import android.bluetooth.BluetoothDevice;

public interface SendingThreadListener {

    public void onThreadFinished(BluetoothDevice device);

    void onConnected(BluetoothDevice mDevice);
}
