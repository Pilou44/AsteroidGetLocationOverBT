package com.freak.fidji.locationoverbt;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.GregorianCalendar;
import java.util.Vector;


public class StateActivity extends Activity {

    private static final String TAG = "STATE_ACTIVITY";
    private static final boolean DEBUG = true;
    private LocationService mLocationService;
    private TextView mTextMobile1, mTextMobile2;
    private boolean mConnected;
    private Handler mHandler;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConnected = true;
            mLocationService = ((LocationService.LocalBinder)service).getService();
            mHandler.post(mRunnable);
        }

        public void onServiceDisconnected(ComponentName className) {
            mConnected = false;
            mHandler.removeCallbacks(mRunnable);
        }
    };

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            refresh();
            if (mConnected)
                mHandler.postDelayed(mRunnable, 2000);
        }
    };//runnable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        mTextMobile1 = (TextView) findViewById(R.id.text_mobile1);
        mTextMobile2 = (TextView) findViewById(R.id.text_mobile2);

        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, LocationService.class);
        bindService(serviceIntent, mConnection, 0);
    }

    @Override
    protected void onPause() {
        unbindService(mConnection);
        mConnected = false;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_state, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        if (DEBUG)
            Log.d(TAG, "refresh");

        Vector<LocationDevice> connectedDevices = mLocationService.getDevices();
        LocationDevice device;
        String text;

        try {
            device = connectedDevices.get(0);
            String name = device.getName();
            String address = device.getAddress();
            String state = getStringState(device.getState());
            int nbTransmissions = device.getNbTransmissions();
            GregorianCalendar lastTransmission = device.getLastTransmission();
            int hours = lastTransmission.get(GregorianCalendar.HOUR_OF_DAY);
            int minutes = lastTransmission.get(GregorianCalendar.MINUTE);
            int seconds = lastTransmission.get(GregorianCalendar.SECOND);
            text =  name + " (" + address + ")\n" +
                    getString(R.string.state) + state + "\n" +
                    getString(R.string.sent_locations) + nbTransmissions + "\n" +
                    getString(R.string.last_transmission) + hours + ":" +
                    (minutes < 10 ? "0" + minutes : minutes) + ":" +
                    (seconds < 10 ? "0" + seconds : seconds);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            text = getString(R.string.no_connected_device);
        }
        mTextMobile1.setText(text);

        try {
            device = connectedDevices.get(1);
            String name = device.getName();
            String address = device.getAddress();
            String state = getStringState(device.getState());
            int nbTransmissions = device.getNbTransmissions();
            GregorianCalendar lastTransmission = device.getLastTransmission();
            int hours = lastTransmission.get(GregorianCalendar.HOUR_OF_DAY);
            int minutes = lastTransmission.get(GregorianCalendar.MINUTE);
            int seconds = lastTransmission.get(GregorianCalendar.SECOND);
            text =  name + " (" + address + ")\n" +
                    getString(R.string.state) + state + "\n" +
                    getString(R.string.sent_locations) + nbTransmissions + "\n" +
                    getString(R.string.last_transmission) + hours + ":" +
                    (minutes < 10 ? "0" + minutes : minutes) + ":" +
                    (seconds < 10 ? "0" + seconds : seconds);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            text = getString(R.string.no_connected_device);
        }
        mTextMobile2.setText(text);

    }

    private String getStringState(int state) {
        switch (state) {
            case LocationDevice.STATE_CONNECTED:
                return getString(R.string.state_connected);
            case LocationDevice.STATE_LOCATION_RECEIVER:
                return getString(R.string.state_location_receiver);
            case LocationDevice.STATE_DISCONNECTED:
                return getString(R.string.state_disconnected);
            case LocationDevice.STATE_UNKNOWN:
                return getString(R.string.state_unknown);
        }
        return "";
    }
}
