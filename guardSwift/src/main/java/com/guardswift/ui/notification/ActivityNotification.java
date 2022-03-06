package com.guardswift.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.ui.activity.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ActivityNotification {



    private static boolean channelCreated;
    private static NotificationCompat.Builder mBuilder;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createChannel(Context context) {
        String CHANNEL_ID = "location_channel";

        if (channelCreated) {
            return CHANNEL_ID;
        }

        CharSequence name = context.getString(R.string.location);// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_MIN;

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mgr != null) {
            mgr.createNotificationChannel(mChannel);
        }

        channelCreated = true;

        return CHANNEL_ID;
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(context, 0, notificationIntent, 0);
        }

    }

    public static Notification create(Context context, String contentText) {



        String channelId = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = createChannel(context);
        }

        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(context.getText(R.string.activity_recognition))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(getPendingIntent(context))
                .setColor(Color.GREEN)
                .setColorized(true);

        return mBuilder.build();
    }

    public static void update(Context context, DetectedActivity activity) {
        mBuilder.setContentText(ActivityDetectionModule.getHumanReadableNameFromType(context.getApplicationContext(), activity.getType()));

        NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mgr != null) {
            mgr.notify(NotificationID.ACTIVITY, mBuilder.build());
        }
    }
}
