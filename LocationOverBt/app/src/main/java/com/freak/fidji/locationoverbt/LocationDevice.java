package com.freak.fidji.locationoverbt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;

public class LocationDevice {

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_LOCATION_RECEIVER = 2;
    public static final int STATE_DISCONNECTED = 3;

    private final BluetoothDevice mDevice;
    private int mState;
    private int mNbTransmissions;
    private GregorianCalendar mLastTransmission;

    public LocationDevice(BluetoothDevice device, int state) {
        mDevice = device;
        mState = state;
        mNbTransmissions = 0;
        mLastTransmission = new GregorianCalendar(1993, GregorianCalendar.JANUARY, 1);
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public int getState() {
        return mState;
    }

    public String getName() {
        return mDevice.getName();
    }

    public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        return mDevice.createRfcommSocketToServiceRecord(uuid);
    }

    public void incrementTransmissions() {
        mNbTransmissions++;
    }

    public void setLastTransmission(GregorianCalendar time) {
        mLastTransmission = time;
    }

    public int getNbTransmissions() {
        return mNbTransmissions;
    }

    public GregorianCalendar getLastTransmission() {
        return mLastTransmission;
    }

    public void setState(int state) {
        mState = state;
    }

}
