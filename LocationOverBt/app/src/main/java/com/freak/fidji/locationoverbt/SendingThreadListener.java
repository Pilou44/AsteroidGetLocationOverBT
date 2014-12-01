package com.freak.fidji.locationoverbt;

import android.bluetooth.BluetoothDevice;

/**
 * Created by GBeguin on 27/11/2014.
 */
public interface SendingThreadListener {

    public void onThreadFinished(BluetoothDevice device);

}
