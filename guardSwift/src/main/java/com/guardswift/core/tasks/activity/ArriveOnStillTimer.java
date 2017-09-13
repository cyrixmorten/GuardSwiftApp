package com.guardswift.core.tasks.activity;

import android.util.Log;

import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class ArriveOnStillTimer {

    private static final String TAG = ArriveOnStillTimer.class.getSimpleName();

    private static final int TRIGGER_AFTER_MINUTES = 1;

    private Timer mTimer;
    private ParseTask task;
    private  ArriveWhenNotInVehicleStrategy.TriggerArrival arrival;

    public ArriveOnStillTimer(ParseTask task, ArriveWhenNotInVehicleStrategy.TriggerArrival arrival) {
        this.task = task;
        this.arrival = arrival;
    }

    public void start() {

        long triggerMillis = TimeUnit.MINUTES.toMillis(TRIGGER_AFTER_MINUTES);
        Log.d(TAG, "Starting still arrival timer - will trigger in: " + triggerMillis + " " + task.getObjectId() + " " + task.getClientName());
        mTimer = new Timer();
        mTimer.schedule(new InactivityTimerTask(), triggerMillis);

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


    private class InactivityTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Arrival triggered by still timer");
            arrival.trigger();

        }
    }

}
