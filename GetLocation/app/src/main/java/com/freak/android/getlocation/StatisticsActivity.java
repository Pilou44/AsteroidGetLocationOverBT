package com.freak.android.getlocation;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class StatisticsActivity extends Activity {

    private TextView mText;
    private StatisticsManager mStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mStatsManager = ((GetLocationApplication)getApplicationContext()).getStatisticsManager();

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
            mStatsManager.clear();
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        mText.setText("" +
                getText(R.string.connection_timeout) + mStatsManager.getConnectionTimeout() + "\n" +
                getText(R.string.received_locations) + mStatsManager.getReceivedLocations() +  "\n" +
                getText(R.string.min_time_to_receive) + mStatsManager.getMinTimeToReceive() + getText(R.string.time_unit) + "\n" +
                getText(R.string.max_time_to_receive) + mStatsManager.getMaxTimeToReceive() + getText(R.string.time_unit) + "\n" +
                getText(R.string.last_time_to_receive) + mStatsManager.getLastTimeToReceive() + getText(R.string.time_unit)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
