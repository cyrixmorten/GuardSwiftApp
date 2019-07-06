package com.guardswift.firebase;


import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.activity.AlarmDialogActivity;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

public class AlarmReceiver extends FirebaseMessagingService {

    private static String TAG = AlarmReceiver.class.getSimpleName();

    private static DateTime lastAlarmReceive;

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



    private void handleNewAlarmNotification(final String alarmId) {

        ParseTask alarm = ParseTask.createWithoutData(ParseTask.class, alarmId);

        final Capture<ParseTask> alarmCapture = new Capture<>();

        final Task<ParseTask> fetchAlarm = alarm.fetchInBackground();

        fetchAlarm.onSuccessTask(task -> {

            alarmCapture.set(task.getResult());

            return task.getResult().pinInBackground();
        }).onSuccessTask((Continuation<Void, Task<Guard>>) task -> {
            Guard guard = GuardSwiftApplication.getLastActiveGuard();


            if (guard != null) {
                return guard.fetchInBackground();
            }

            throw new Exception("No last active guard found");

        }).continueWith(task -> {
            ParseTask alarm1 = alarmCapture.get();

            if (task.isFaulted() || alarm1 == null) {
                new HandleException(TAG, "Failed receive alarm: " + alarmId, task.getError());

                return null;
            }


            Guard guard = task.getResult();


            boolean someTimePast =  lastAlarmReceive == null ||  Seconds.secondsBetween(lastAlarmReceive, new DateTime()).isGreaterThan(Seconds.seconds(30));

            if (guard.isAlarmSoundEnabled() && someTimePast) {

                AlarmDialogActivity.start(AlarmReceiver.this, alarm1);

                lastAlarmReceive = new DateTime();
            }


            return null;
        });


    }



}
