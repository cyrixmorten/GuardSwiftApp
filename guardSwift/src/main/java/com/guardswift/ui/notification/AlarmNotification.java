package com.guardswift.ui.notification;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.guardswift.R;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.ui.activity.MainActivity;

public class AlarmNotification {

    private static final int ID = 100;

    public static void show(Context context, ParseTask alarm) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra(MainActivity.SELECT_ALARMS, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0 /* request code */, intent,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.alarm))
                .setContentText(alarm.getFormattedAddress())
                .setAutoCancel(true)
                .setLights(Color.BLUE,1,1)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID /* ID of notification */, notificationBuilder.build());
    }

    public static void cancel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID);
    }
}
