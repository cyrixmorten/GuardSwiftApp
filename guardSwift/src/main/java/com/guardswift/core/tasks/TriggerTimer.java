package com.guardswift.core.tasks;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TriggerTimer {

    public interface Trigger {
        void trigger();
    }

    private static final String TAG = TriggerTimer.class.getSimpleName();


    private Timer mTimer;

    private final Trigger trigger;
    private final int afterSeconds;

    public TriggerTimer(Trigger trigger, int afterSeconds) {
        this.trigger = trigger;
        this.afterSeconds = afterSeconds;
    }

    public void start() {
        stop(); // cleanup if we already have a timer

        long triggerMillis = TimeUnit.SECONDS.toMillis(afterSeconds);
        mTimer = new Timer();
        mTimer.schedule(new InactivityTimerTask(), triggerMillis);

    }

    public void stop() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public boolean running() {
        return mTimer != null;
    }


    private class InactivityTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Arrival triggered by still timer");
            trigger.trigger();

        }
    }

}
