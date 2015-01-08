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

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {

        SharedPreferences pref = getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
        int connectionTimeout = pref.getInt("connection_timeout", 0);
        int connectionAbort = pref.getInt("connection_abort", 0);
        int connectionOpen = pref.getInt("connection_open", 0);
        int threadAbort = pref.getInt("thread_abort", 0);
        int minTimeToReceive = pref.getInt("min_time", 0);
        int maxTimeToReceive = pref.getInt("max_time", 0);
        int lastTimeToReceive = pref.getInt("last_time", 0);
        int corruptedDatas = pref.getInt("corrupted_datas", 0);

        mText.setText("" +
                getText(R.string.thread_abort) + threadAbort + "\n" +
                getText(R.string.connection_timeout) + connectionTimeout + "\n" +
                getText(R.string.connection_open) + connectionOpen +  "\n" +
                getText(R.string.connection_abort) + connectionAbort + "\n" +
                getText(R.string.min_time_to_receive) + minTimeToReceive + getText(R.string.time_unit) + "\n" +
                getText(R.string.max_time_to_receive) + maxTimeToReceive + getText(R.string.time_unit) + "\n" +
                getText(R.string.last_time_to_receive) + lastTimeToReceive + getText(R.string.time_unit) + "\n" +
                getText(R.string.corrupted_datas) + corruptedDatas);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
