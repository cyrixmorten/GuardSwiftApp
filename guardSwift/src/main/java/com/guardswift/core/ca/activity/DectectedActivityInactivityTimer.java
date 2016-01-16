package com.guardswift.core.ca.activity;

import android.content.Context;
import android.util.Log;

import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by cyrix on 6/18/15.
 */
class DectectedActivityInactivityTimer {

    private static final String TAG = DectectedActivityInactivityTimer.class.getSimpleName();

    private static final int TRIGGER_AFTER_MINUTES = 2;

    private final Context mContext;
    private Timer mInactivityTimer;
    private boolean isTriggered;

    public DectectedActivityInactivityTimer(Context context) {
        mContext = context.getApplicationContext();
    }

    public void start() {
//        if (BuildConfig.DEBUG)
//            return;

        Log.d(TAG, "Starting inactivity time - will trigger in: " + TimeUnit.MINUTES.toMillis(TRIGGER_AFTER_MINUTES));
        mInactivityTimer = new Timer();
        mInactivityTimer.schedule(new InactivityTimerTask(), TimeUnit.MINUTES.toMillis(TRIGGER_AFTER_MINUTES));

        isTriggered = false;
    }

    public void stop() {

        if (mInactivityTimer != null) {
            mInactivityTimer.cancel();
            mInactivityTimer = null;
        }
    }

    public boolean isTriggered() {
        return isTriggered;
    }

    private class InactivityTimerTask extends TimerTask {
        @Override
        public void run() {

            Log.w(TAG, "Inactivity timer triggered!");
            FusedLocationTrackerService.stop(mContext);
            WiFiPositioningService.stop(mContext);

            isTriggered = true;

        }
    }

}
