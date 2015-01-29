package com.freak.fidji.locationoverbt;

public interface SendingThreadListener {

    public void onThreadFinished(LocationDevice device);

    void onConnected(LocationDevice mDevice);
}
