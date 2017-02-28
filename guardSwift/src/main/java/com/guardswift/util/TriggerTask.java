package com.guardswift.util;

import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by cyrix on 6/18/15.
 */
public class TriggerTask {

    private static final String TAG = TriggerTask.class.getSimpleName();


    private java.util.Timer timer;

    public TriggerTask() {
    }

    public void start(java.util.TimerTask task, int triggerAfterMinutes) {
        Log.d(TAG, "Starting inactivity time - will trigger in: " + TimeUnit.MINUTES.toMillis(triggerAfterMinutes));
        timer = new java.util.Timer();
        timer.schedule(task, TimeUnit.MINUTES.toMillis(triggerAfterMinutes));
    }

    public void stop() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


}
