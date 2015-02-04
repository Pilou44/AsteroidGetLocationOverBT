package com.freak.android.getlocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class DisplayLocationActivity extends Activity implements OnMapReadyCallback {

    private TextView mText;
    private Button mButton;
    private ShareActionProvider mShareActionProvider;
    private MapView mMap;
    private Circle mCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_location);
        mText = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.go_maps);
        mMap = (MapView) findViewById(R.id.map);

        mMap.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        mMap.onResume();
        super.onResume();
        refresh();
    }

    @Override
    public void onLowMemory() {
        mMap.onLowMemory();
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        mMap.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMap.onPause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        mMap.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_location, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        sharePosition();
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
            sharePosition();
            return true;
        }
        else if (id == R.id.action_stats) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.key_preferences), 0);
        final Double latitude = Double.longBitsToDouble(pref.getLong(getString(R.string.key_latitude), Double.doubleToLongBits(0.0)));
        final Double longitude = Double.longBitsToDouble(pref.getLong(getString(R.string.key_longitude), Double.doubleToLongBits(0.0)));
        final Double accuracy = Double.longBitsToDouble(pref.getLong(getString(R.string.key_accuracy), Double.doubleToLongBits(0.0)));

        float accuracyM = ((float)((int)(accuracy * 100))) / 100;

        mText.setText(getText(R.string.latitude_value) + latitude.toString() + getText(R.string.lat_lng_unit) + "\n" + getText(R.string.longitude_value) + longitude.toString() + getText(R.string.lat_lng_unit) + "\n" + getText(R.string.accuracy_value) + accuracyM + getText(R.string.accuracy_unit));
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + latitude + "," + longitude + " (" + DisplayLocationActivity.this.getString(R.string.my_car) + ")"));
                startActivity(intent);
            }
        });

        mMap.getMapAsync(this);
    }

    public void sharePosition() {
        Intent shareIntent;
        if (mShareActionProvider != null) {
            SharedPreferences pref = getSharedPreferences(getString(R.string.key_preferences), 0);
            final Double latitude = Double.longBitsToDouble(pref.getLong(getString(R.string.key_latitude), Double.doubleToLongBits(0.0)));
            final Double longitude = Double.longBitsToDouble(pref.getLong(getString(R.string.key_longitude), Double.doubleToLongBits(0.0)));

            shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, this.getString(R.string.title));
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://maps.google.com?q=" + latitude + "," + longitude);
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.key_preferences), 0);
        final Double latitude = Double.longBitsToDouble(pref.getLong(getString(R.string.key_latitude), Double.doubleToLongBits(0.0)));
        final Double longitude = Double.longBitsToDouble(pref.getLong(getString(R.string.key_longitude), Double.doubleToLongBits(0.0)));
        final Double accuracy = Double.longBitsToDouble(pref.getLong(getString(R.string.key_accuracy), Double.doubleToLongBits(0.0)));

        LatLng location = new LatLng(latitude, longitude);

        MapsInitializer.initialize(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));

        if (mCircle != null){
            mCircle.remove();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(location)
                .radius(accuracy) // In meters
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .fillColor(0x7F0000FF);

        mCircle = map.addCircle(circleOptions);
    }
}
