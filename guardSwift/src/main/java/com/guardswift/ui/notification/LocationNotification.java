package com.guardswift.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.guardswift.R;
import com.guardswift.ui.activity.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class LocationNotification {



    private static boolean channelCreated;
    private static NotificationCompat.Builder mBuilder;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createChannel(Context context) {
        String CHANNEL_ID = "activity_channel";

        if (channelCreated) {
            return CHANNEL_ID;
        }

        CharSequence name = context.getString(R.string.activity);
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
        return PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    public static Notification create(Context context, String contentText) {

        String channelId = createChannel(context);


        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(context.getText(R.string.gps_position))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(getPendingIntent(context))
                .setColor(Color.BLUE)
                .setColorized(true);

        return mBuilder.build();
    }

    public static void update(Context context, Location location) {
        mBuilder.setContentText(context.getString(R.string.latlng, location.getLatitude(), location.getLongitude()));

        NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mgr != null) {
            mgr.notify(NotificationID.LOCATION, mBuilder.build());
        }
    }
}
