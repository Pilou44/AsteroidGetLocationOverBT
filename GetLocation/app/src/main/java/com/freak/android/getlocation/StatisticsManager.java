package com.freak.android.getlocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class StatisticsManager {

    private final Context mContext;
    private final SharedPreferences.Editor mEditor;
    private int mConnectionTimeout, mReceivedLocations, mMinTimeToReceive, mMaxTimeToReceive, mLastTimeToReceive;

    @SuppressLint("CommitPrefEdits")
    public StatisticsManager(Context context){
        mContext = context;

        SharedPreferences pref = context.getSharedPreferences(MyActivity.PREFERENCE_NAME, 0);
        mEditor = pref.edit();

        mConnectionTimeout = pref.getInt(context.getString(R.string.key_connection_timeout), 0);
        mReceivedLocations = pref.getInt(context.getString(R.string.key_received_locations), 0);
        mMinTimeToReceive = pref.getInt(context.getString(R.string.key_min_time), Integer.MAX_VALUE);
        mMaxTimeToReceive = pref.getInt(context.getString(R.string.key_max_time), 0);
        mLastTimeToReceive = pref.getInt(context.getString(R.string.key_last_time), 0);

    }

    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    public int getReceivedLocations() {
        return mReceivedLocations;
    }

    public int getMinTimeToReceive() {
        return mMinTimeToReceive;
    }

    public int getMaxTimeToReceive() {
        return mMaxTimeToReceive;
    }

    public int getLastTimeToReceive() {
        return mLastTimeToReceive;
    }

    public void incConnectionTimeout() {
        mConnectionTimeout++;
        mEditor.putInt(mContext.getString(R.string.key_connection_timeout), mConnectionTimeout);
        mEditor.apply();
    }

    public void incReceivedLocations() {
        mReceivedLocations++;
        mEditor.putInt(mContext.getString(R.string.key_received_locations), mReceivedLocations);
        mEditor.apply();
    }

    public void setMinTimeToReceive(int minTimeToReceive) {
        mMinTimeToReceive = minTimeToReceive;
        mEditor.putInt(mContext.getString(R.string.key_min_time), mMinTimeToReceive);
        mEditor.apply();
    }

    public void setMaxTimeToReceive(int maxTimeToReceive) {
        mMaxTimeToReceive = maxTimeToReceive;
        mEditor.putInt(mContext.getString(R.string.key_max_time), mMaxTimeToReceive);
        mEditor.apply();
    }

    public void setLastTimeToReceive(int lastTimeToReceive) {
        mLastTimeToReceive = lastTimeToReceive;
        mEditor.putInt(mContext.getString(R.string.key_last_time), mLastTimeToReceive);
        mEditor.apply();
    }

    public void clear() {
        mEditor.putInt(mContext.getString(R.string.key_connection_timeout), 0);
        mEditor.putInt(mContext.getString(R.string.key_received_locations), 0);
        mEditor.putInt(mContext.getString(R.string.key_min_time), Integer.MAX_VALUE);
        mEditor.putInt(mContext.getString(R.string.key_max_time), 0);
        mEditor.putInt(mContext.getString(R.string.key_last_time), 0);
        mEditor.apply();
    }
}
