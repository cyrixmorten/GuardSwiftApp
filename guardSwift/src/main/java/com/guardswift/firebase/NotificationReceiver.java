package com.guardswift.firebase;


import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.guardswift.ui.dialog.activity.AlarmDialogActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class NotificationReceiver extends FirebaseMessagingService {

    private static String TAG = NotificationReceiver.class.getSimpleName();

    public NotificationReceiver() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Map<String, String> mapData = remoteMessage.getData();
        if (mapData != null) {
            try {
                JSONObject json = new JSONObject(mapData.get("data"));
                if (json.has("alarmId")) {
                    AlarmDialogActivity.start(getApplicationContext(), json.getString("alarmId"));
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

}
