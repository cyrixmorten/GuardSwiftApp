package com.guardswift.core.tasks.activity;

import android.util.Log;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by cyrix on 6/18/15.
 */
class ArriveOnStillTimer {

    private static final String TAG = ArriveOnStillTimer.class.getSimpleName();

    private static final int TRIGGER_AFTER_MINUTES = 1;

    private Timer mTimer;
    private GSTask task;

    public ArriveOnStillTimer(GSTask task) {
        this.task = task;
    }

    public void start() {

        long triggerMillis = TimeUnit.MINUTES.toMillis(TRIGGER_AFTER_MINUTES);
        Log.d(TAG, "Starting still arrival timer - will trigger in: " + triggerMillis);
        mTimer = new Timer();
        mTimer.schedule(new InactivityTimerTask(), triggerMillis);

    }

    public void stop() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }


    private class InactivityTimerTask extends TimerTask {
        @Override
        public void run() {
            task.getAutomationStrategy().automaticArrival();

        }
    }

}
