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
import com.guardswift.util.Device;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseSession;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

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
            for (String key : mapData.keySet()) {
                Log.d(TAG, "Key: " + key + " Value: " + mapData.get(key));
            }
        }


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Log.d(TAG, "Notification Message Body: " + notification.getBody());
        }

    }


    private void pinAlarmAndAddToGeofence(ParseTask alarm) {

        new ParseTask().pinUpdate(alarm, new ExtendedParseObject.DataStoreCallback<ParseTask>() {
            @Override
            public void success(List<ParseTask> objects) {
                // rebuilding geofence to include new alarm
                RegisterGeofencesIntentService.start(getApplicationContext());
            }

            @Override
            public void failed(ParseException e) {
                new HandleException(TAG, "Failed to pin alarm to local datastore", e);
            }
        });
    }

    private void handleNewAlarmNotification(final String alarmId) {

        ParseTask alarm = ParseTask.createWithoutData(ParseTask.class, alarmId);
        alarm.fetchInBackground().onSuccessTask(new Continuation<ParseObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<ParseObject> task) throws Exception {
                return task.getResult().pinInBackground();
            }
        }).onSuccessTask(new Continuation<Void, Task<Guard>>() {
            @Override
            public Task<Guard> then(Task<Void> task) throws Exception {
                Guard guard = GuardSwiftApplication.getLastActiveGuard();


                if (guard != null) {
                    return guard.fetchInBackground();
                }

                throw new Exception("No last active guard found");

            }
        }).continueWith(new Continuation<Guard, Object>() {
            @Override
            public Object then(Task<Guard> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();

                    new HandleException(TAG, "Failed receive alarm: " + alarmId, exception);

                    Device device = new Device(GuardSwiftApplication.getInstance());

                    // TODO: Builder pattern or bake into HandleException
                    ParseObject error = new ParseObject("Error");
                    error.put("owner", ParseUser.getCurrentUser());
                    error.put("installation", ParseInstallation.getCurrentInstallation());
                    error.put("platform", "Android");
                    error.put("tag", TAG);
                    error.put("gsVersion", device.getVersionCode());
                    error.put("context", "Receive alarm notification for id: " + alarmId);
                    error.put("message", exception.getMessage());
                    error.saveInBackground();

                    return null;
                }

                // Update geofence to include new alarm
                RegisterGeofencesIntentService.start(getApplicationContext());

                Guard guard = task.getResult();


                if (guard.isAlarmSoundEnabled()) {
                    AlarmDialogActivity.start(getApplicationContext(), alarmId);
                }

                return null;
            }
        });


    }

}
