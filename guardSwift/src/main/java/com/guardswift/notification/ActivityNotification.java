package com.guardswift.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.ui.activity.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ActivityNotification {

    public static final int NOTIFY_ID = 100;

    private static boolean channelCreated;
    private static NotificationCompat.Builder mBuilder;

    private static String createChannel(Context context) {
        String CHANNEL_ID = "location_channel";

        if (channelCreated) {
            return CHANNEL_ID;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            CharSequence name = context.getString(R.string.location);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_MIN;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            mgr.createNotificationChannel(mChannel);
        }

        channelCreated = true;

        return CHANNEL_ID;
    }

    public static Notification create(Context context, String contentText) {

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        String channelId = createChannel(context);

        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(context.getText(R.string.activity_recognition))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setColor(Color.GREEN)
                .setColorized(true);

        return mBuilder.build();
    }

    public static void update(Context context, DetectedActivity activity) {
        mBuilder.setContentText(ActivityDetectionModule.getHumanReadableNameFromType(context.getApplicationContext(), activity.getType()));

        NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(NOTIFY_ID, mBuilder.build());
    }
}
