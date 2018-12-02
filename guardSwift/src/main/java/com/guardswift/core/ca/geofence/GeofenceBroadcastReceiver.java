package com.guardswift.core.ca.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "GeofenceBroadcastReceiver");

        GeofencingModule geofencingModule = new GeofencingModule();

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {

            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    String[] geofenceIds = new String[event.getTriggeringGeofences().size()];
                    for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
                        geofenceIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
                    }


                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        Log.e(TAG, "GeofenceBroadcastReceiver GEOFENCE_ENTER");
                        geofencingModule.onEnteredGeofences(geofenceIds, false);
                    } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        Log.e(TAG, "GeofenceBroadcastReceiver GEOFENCE_EXIT");
                        geofencingModule.onExitedGeofences(geofenceIds, false);
                    }
                }
            }

        }
    }



    private void onError(int i) {
        Log.e(TAG, "Error: " + i);
    }
}