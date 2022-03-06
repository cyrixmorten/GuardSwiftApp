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

import com.guardswift.R;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.activity.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmNotification {



    private static boolean channelCreated;
    private static NotificationCompat.Builder mBuilder;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createChannel(Context context) {
        String CHANNEL_ID = "alarm_channel";

        if (channelCreated) {
            return CHANNEL_ID;
        }

        CharSequence name = context.getString(R.string.alarm);// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;

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
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra(MainActivity.SELECT_ALARMS, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getActivity(context,0 /* request code */, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(context,0 /* request code */, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }


    }

    private static Notification create(Context context, ParseTask alarm) {
        context = context.getApplicationContext();

        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createChannel(context);
        }

        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.alarm))
                .setContentText(alarm.getFormattedAddress())
                .setAutoCancel(true)
                .setLights(Color.BLUE,1,1)
                .setColor(Color.RED)
                .setColorized(true)
                .setContentIntent(getPendingIntent(context));

        return mBuilder.build();
    }



    public static void show(Context context, ParseTask alarm) {
        Notification notification = create(context, alarm);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NotificationID.ALARM, notification);
        }
    }

    public static void cancel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NotificationID.ALARM);
        }
    }
}
