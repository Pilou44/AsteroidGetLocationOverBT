package com.freak.fidji.locationoverbt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class TestActivity extends Activity {

    private static final String TAG = "TEST_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "Start service from activity");
        Intent newIntent = new Intent(this, MyService.class);
        this.startService(newIntent);
    }
}
