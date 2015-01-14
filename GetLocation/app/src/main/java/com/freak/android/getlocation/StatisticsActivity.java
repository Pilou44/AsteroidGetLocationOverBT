package com.freak.android.getlocation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class StatisticsActivity extends Activity {

    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mText = (TextView) findViewById(R.id.text_stats);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        else if (id == R.id.action_clear) {
            clear();
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clear() {
        SharedPreferences pref = this.getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(getString(R.string.key_connection_timeout), 0);
        editor.putInt(getString(R.string.key_received_locations), 0);
        editor.putInt(getString(R.string.key_min_time), Integer.MAX_VALUE);
        editor.putInt(getString(R.string.key_max_time), 0);
        editor.putInt(getString(R.string.key_last_time), 0);
        editor.apply();
    }

    private void refresh() {

        SharedPreferences pref = getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
        int connectionTimeout = pref.getInt(getString(R.string.key_connection_timeout), 0);
        int receivedLocations = pref.getInt(getString(R.string.key_received_locations), 0);
        int minTimeToReceive = pref.getInt(getString(R.string.key_min_time), 0);
        int maxTimeToReceive = pref.getInt(getString(R.string.key_max_time), 0);
        int lastTimeToReceive = pref.getInt(getString(R.string.key_last_time), 0);

        mText.setText("" +
                getText(R.string.connection_timeout) + connectionTimeout + "\n" +
                getText(R.string.received_locations) + receivedLocations +  "\n" +
                getText(R.string.min_time_to_receive) + minTimeToReceive + getText(R.string.time_unit) + "\n" +
                getText(R.string.max_time_to_receive) + maxTimeToReceive + getText(R.string.time_unit) + "\n" +
                getText(R.string.last_time_to_receive) + lastTimeToReceive + getText(R.string.time_unit)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
