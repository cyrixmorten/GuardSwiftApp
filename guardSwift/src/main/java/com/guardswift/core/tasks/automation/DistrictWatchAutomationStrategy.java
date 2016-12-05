package com.guardswift.core.tasks.automation;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Sounds;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by cyrix on 6/7/15.
 */
public class DistrictWatchAutomationStrategy implements TaskAutomationStrategy {

    private static final String TAG = DistrictWatchAutomationStrategy.class.getSimpleName();

    private final DistrictWatchClient task;
    private final Context context;

    private Timer timer;


    public DistrictWatchAutomationStrategy(DistrictWatchClient task) {
        this.task = task;
        this.context = GuardSwiftApplication.getInstance();
    }

    @Override
    public void automaticArrival() {
        TaskController controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.ARRIVE, task)) {
            Log.w(TAG, "automaticArrival " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.arrived);
            controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
            startResetTimer();
        }

    }

    @Override
    public void automaticDeparture() {
        Log.w(TAG, "automaticDeparture " + task.getTaskType() + " " + task.getClientName());
        TaskController controller = task.getController();
        controller.performAutomaticAction(TaskController.ACTION.RESET, task);
        stopResettimertask();
    }


    public void startResetTimer() {
        //set a new Timer
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Location location = LocationModule.Recent.getLastKnownLocation();
                float distMeters = ParseModule.distanceBetweenMeters(location, task.getPosition());
                Log.w(TAG, "Reset timer, distance to task in meters; " + distMeters);
                if (distMeters > task.getGeofenceStrategy().getGeofenceRadius() / 2) {
                    automaticDeparture();
                } else {
                    Log.w(TAG, "Reseting reset timer for districtWatch: " + task.getFullAddress());
                    startResetTimer();
                }
            }
        }, TimeUnit.MINUTES.toMillis(5));
    }
    public void stopResettimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
