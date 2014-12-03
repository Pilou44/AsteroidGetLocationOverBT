package com.freak.android.getlocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MyActivity extends Activity {

    public static final String PREFERENCE_NAME = "asteroid_location";
    private TextView mText;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mText = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.go_maps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
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
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, 0);
        final Double latitude = Double.longBitsToDouble(pref.getLong("latitude", Double.doubleToLongBits(0.0)));
        final Double longitude = Double.longBitsToDouble(pref.getLong("longitude", Double.doubleToLongBits(0.0)));
        final Double accuracy = Double.longBitsToDouble(pref.getLong("accuracy", Double.doubleToLongBits(0.0)));

        mText.setText("Latitude = " + latitude + "\nLongitude = " + longitude + "\nAccuracy = " + accuracy);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + latitude + "," + longitude + " (" + MyActivity.this.getString(R.string.my_car) + ")"));
                startActivity(intent);
            }
        });
    }
}
