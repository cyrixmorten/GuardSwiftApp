package com.guardswift.ui.dialog.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.tasks.controller.AlarmController;
import com.guardswift.persistence.cache.task.TaskCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.notification.AlarmNotification;
import com.guardswift.util.Sounds;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class AlarmDialogActivity extends AbstractDialogActivity {

    protected static final String TAG = AlarmDialogActivity.class
            .getSimpleName();

    private final static String EXTRA_ALARM_ID = "EXTRA_ALARM_ID";

    @Inject
    FragmentManager fm;

    @Inject
    TaskCache alarmCache;

    @Inject
    Sounds mSounds;

    private int currentAlarmSound = Sounds.ALARM_NEW;

    private Handler snoozeHandler = new Handler();
    private Runnable snoozeRunnable = new SnoozeRunnable();

    private static ParseTask alarm;

    public static void start(final Context context, ParseTask alarm) {

        Log.d(TAG, "START " + alarm);

        if (AlarmDialogActivity.alarm == null) {
            AlarmDialogActivity.alarm = alarm;

            Intent i = new Intent(context, AlarmDialogActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d(TAG, "onCreate");

        if (alarm != null) {
            createAndShowAlarmDialog();
            AlarmNotification.show(this, alarm);
        } else {
            finish();
        }
    }


    private void playAlarmSound() {
        currentAlarmSound = alarm.isAborted() ? Sounds.ALARM_CANCELLED : Sounds.ALARM_NEW;
        mSounds.playSoundRepeating(currentAlarmSound);
    }

    private void stopAlarmSound() {
        mSounds.stopAlarm();
        snoozeHandler.removeCallbacks(snoozeRunnable);
    }

    private void snoozeAlarm() {
        stopAlarmSound();
        snoozeHandler.postDelayed(snoozeRunnable, TimeUnit.MINUTES.toMillis(1));
    }

    private class SnoozeRunnable implements Runnable {

        @Override
        public void run() {
            if (mSounds != null) {
                mSounds.playSoundRepeating(currentAlarmSound);
            }
        }
    }

    @Override
    protected void onDestroy() {
        
        stopAlarmSound();

        AlarmDialogActivity.alarm = null;

        super.onDestroy();
    }


    private void createAndShowAlarmDialog() {

        if (isDestroyed()) {
            return;
        }

        playAlarmSound();
        Log.d(TAG, "createAndShowAlarmDialog");

//        String formattedDate = android.text.format.DateUtils.formatDateTime(AlarmDialogActivity.this,
//                alarm.getCreatedAt().getTime(),
//                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

        String formattedTime = DateFormat.getTimeFormat(this).format(alarm.getCreatedAt());
        String alarmHeader =  alarm.getCentralName() + " " + formattedTime + "\n\n";

        String alarmBody = "";
        if (alarm.isAborted()) {
            String canceled = "--- " + getString(R.string.canceled).toUpperCase() + " ---";
            alarmBody = canceled + "\n\n";
        }

        alarmBody += alarm.getOriginal();

        String alarmMessage = alarmHeader + alarmBody;


        Log.d(TAG, "alarmBody: " + alarmMessage);

        new CommonDialogsBuilder.MaterialDialogs(AlarmDialogActivity.this).ok(R.string.alarm, alarmMessage, new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (alarm.isPending()) {
                    AlarmController.getInstance().performAction(AlarmController.ACTION.ACCEPT, alarm, false);
                }

                AlarmDialogActivity.this.finish();

                Intent intent = new Intent(AlarmDialogActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(MainActivity.SELECT_ALARMS, true);
                AlarmDialogActivity.this.startActivity(intent);

                AlarmNotification.cancel(AlarmDialogActivity.this);
                stopAlarmSound();
            }
        })
        .cancelable(false)
        .canceledOnTouchOutside(false)
        .show();


    }


}
