package com.guardswift.firebase;


import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.activity.AlarmDialogActivity;
import com.parse.GetCallback;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class AlarmReceiver extends FirebaseMessagingService {

    private static String TAG = AlarmReceiver.class.getSimpleName();

    public AlarmReceiver() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> mapData = remoteMessage.getData();
        if (mapData != null) {
            try {
                JSONObject json = new JSONObject(mapData.get("data"));
                if (json.has("alarmId")) {
                    handleNewAlarmNotification(json.getString("alarmId"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (String key: mapData.keySet()) {
                Log.d(TAG, "Key: " + key + " Value: " + mapData.get(key));
            }
        }


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Log.d(TAG, "Notification Message Body: " + notification.getBody());
        }

    }

    private void handleNewAlarmNotification(final String alarmId) {


        new ParseTask().getQueryBuilder(false).matchingObjectId(alarmId).build().getFirstInBackground(new GetCallback<ParseTask>() {
            @Override
            public void done(ParseTask object, ParseException e) {
                if (object != null) {
                    Log.w(TAG, "handleNewAlarmNotification: " + object.getObjectId());
                    new ParseTask().pinUpdate(object, new ExtendedParseObject.DataStoreCallback<ParseTask>() {
                        @Override
                        public void success(List<ParseTask> objects) {
                            // rebuilding geofence to include new alarm
                            RegisterGeofencesIntentService.start(getApplicationContext());

                            // notify about the new alarm
                            Guard guard = GuardSwiftApplication.getLastActiveGuard();
                            Log.d(TAG, "has guard: " + (guard != null));
                            if (guard != null) {
                                Log.d(TAG, "guard name: " + guard.getName());
                                Log.d(TAG, "isAlarmSoundEnabled: " + guard.isAlarmSoundEnabled());
                                if (guard.isAlarmSoundEnabled()) {
                                    AlarmDialogActivity.start(getApplicationContext(), alarmId);
                                }
                            }
                        }

                        @Override
                        public void failed(ParseException e) {
                            new HandleException(TAG, "Failed to pin alarm to local datastore", e);
                        }
                    });
                } else {
                    new HandleException(TAG, "Unable to download alarm with id: " + alarmId, e);
                }
            }
        });

    }

}
