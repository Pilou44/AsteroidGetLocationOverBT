package com.freak.android.getlocation;

import android.app.Application;

public class GetLocationApplication extends Application {

    private StatisticsManager mStatsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mStatsManager = new StatisticsManager(this);
    }

    public StatisticsManager getStatisticsManager() {
        return mStatsManager;
    }
}
