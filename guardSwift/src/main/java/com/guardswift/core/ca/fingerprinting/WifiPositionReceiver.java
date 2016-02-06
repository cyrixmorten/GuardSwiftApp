package com.guardswift.core.ca.fingerprinting;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.FingerprintingModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingBroadcastReceiver;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.Set;

import javax.inject.Inject;

import dk.alexandra.positioning.wifi.Coordinates;

/**
 * Created by cyrix on 2/18/15.
 */
public class WifiPositionReceiver extends InjectingBroadcastReceiver {

    private static final String TAG = WifiPositionReceiver.class.getSimpleName();


    @Inject
    FingerprintingModule fingerprintingModule;
    @Inject
    GSTasksCache tasksCache;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);

        Coordinates coordinates = (Coordinates) intent.getExtras().get(WiFiPositioningService.WIFI_POSITION);
        String symbolic = intent.getStringExtra(WiFiPositioningService.WIFI_POSITION_SYMBOLIC);
        double probability = intent.getDoubleExtra(WiFiPositioningService.WIFI_POSITION_PROBABILITY, 0f);
        double distance = intent.getDoubleExtra(WiFiPositioningService.WIFI_POSITION_ESTIMATED_DISTANCE, Double.MAX_VALUE);

        if (ActivityDetectionModule.Recent.getDetectedActivityType() == DetectedActivity.IN_VEHICLE) {
//            Log.e(TAG, "Not accepting checkpoints while in vehicle");
            return;
        }

        if (distance > 5 || probability < 0.99f) {
//            Log.e(TAG, "Distance too large: " + distance);
            return;
        }

        saveEventIfWithinDistance(context, symbolic, probability, distance);

    }


    private void saveEventIfWithinDistance(final Context context, final String symbolic, final double probability, final double distance) {
//        Log.d(TAG, "Checkpoint candidate: " + symbolic + " - " + distance);

        final Set<GSTask> arrivedTasks = tasksCache.getArrived(GSTask.TASK_TYPE.REGULAR);

        for (final GSTask task: arrivedTasks) {

            if (task.getClient() == null || !task.getClient().hasCheckPoints()) {
                // skip missing client or checkpoints
                continue;
            }

            final ClientLocation checkpoint = task.getClient().findCheckpoint(symbolic);

            if (checkpoint == null || checkpoint.isChecked()) {
                // checkpoint is already checked
                continue;
            }

            checkpoint.setLastDistance(distance);
            checkpoint.setLastProbability(probability);


            checkpoint.setChecked(true);
            checkpoint.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        new HandleException(context, TAG, "pin ClientLocation", e);
                        return;
                    }

                    new EventLog.Builder(context)
                            .taskPointer(task, GSTask.EVENT_TYPE.CHECKPOINT)
                            .checkpoint(checkpoint, false)
                            .wifiSample(fingerprintingModule.getLastKnownSample())
                            .automatic(true)
                            .saveAsync();
                }
            });


        }
    }

}
