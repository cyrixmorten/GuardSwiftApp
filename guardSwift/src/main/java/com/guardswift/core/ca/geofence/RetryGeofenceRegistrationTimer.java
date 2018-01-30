package com.guardswift.core.ca.geofence;

import android.content.Context;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class RetryGeofenceRegistrationTimer {

    private static final String TAG = RetryGeofenceRegistrationTimer.class.getSimpleName();

    private static final int TRIGGER_AFTER_MINUTES = 2;

    private Timer mTimer;
    private Context context;

    public RetryGeofenceRegistrationTimer(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {

        if (running()) {
            return;
        }

        long triggerMillis = TimeUnit.MINUTES.toMillis(TRIGGER_AFTER_MINUTES);
        mTimer = new Timer();
        mTimer.schedule(new RetryGeofenceRegistrationTimerTask(), triggerMillis);

    }

    public void stop() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    boolean running() {
        return mTimer != null;
    }


    private class RetryGeofenceRegistrationTimerTask extends TimerTask {
        @Override
        public void run() {
            RegisterGeofencesIntentService.start(RetryGeofenceRegistrationTimer.this.context, false);
            stop();
        }
    }

}
