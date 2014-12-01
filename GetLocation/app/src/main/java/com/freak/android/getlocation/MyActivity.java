package com.freak.android.getlocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.prefs.Preferences;


public class MyActivity extends Activity {

    public static final String PREFERENCE_NAME = "asteroid_location";
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mText = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, 0);
        Double latitude = Double.longBitsToDouble(pref.getLong("latitude", Double.doubleToLongBits(0.0)));
        Double longitude = Double.longBitsToDouble(pref.getLong("longitude", Double.doubleToLongBits(0.0)));
        Double accuracy = Double.longBitsToDouble(pref.getLong("accuracy", Double.doubleToLongBits(0.0)));

        mText.setText("Latitude = " + latitude + "\nLongitude = " + longitude + "\nAccuracy = " + accuracy);

        //Intent newIntent = new Intent(this, MyService.class);
        //this.startService(newIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
