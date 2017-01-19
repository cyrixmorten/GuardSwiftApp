package com.guardswift.ui.dialog.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.persistence.cache.task.TaskCache;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.core.tasks.controller.AlarmController;
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
    AlarmController alarmController;

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

//    public static void start(final Context context, String alarmId) {
//
//        Log.d(TAG, "START " + alarmId);
//
//
//        Intent i = new Intent(context, AlarmDialogActivity.class);
//
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        i.putExtra(EXTRA_ALARM_ID, alarmId);
//        context.startActivity(i);
//
//    }


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(TAG, "onCreate");
        createAndShowAlarmDialog(alarm);

//        ParseQuery<ParseTask> alarmQuery;
//        String alarmId = "No alarmId";
//        if (getIntent().hasExtra(EXTRA_ALARM_ID)) {
//            alarmId = getIntent().getStringExtra(EXTRA_ALARM_ID);
//            Log.d(TAG, "Using alarmId");
//            alarmQuery = new AlarmQueryBuilder(true).matchingObjectId(alarmId).build();
//        } else {
//            Log.d(TAG, "No alarmId");
//            alarmQuery = new AlarmQueryBuilder(true)
//                    .whereStatus(ParseTask.STATUS.PENDING, ParseTask.STATUS.ABORTED)
//                    .sortByTimeStarted()
//                    .build();
//        }
//
//        final String finalAlarmId = alarmId;
//        alarmQuery.getFirstInBackground(new GetCallback<ParseTask>() {
//            @Override
//            public void done(ParseTask alarm, ParseException e) {
//                if (e != null || alarm == null) {
//                    new HandleException(TAG, "Find alarm " + finalAlarmId, e);
//                    AlarmDialogActivity.this.finish();
//                    return;
//                }
//
//                createAndShowAlarmDialog(alarm);
//            }
//        });

    }


    private void stopAlarm() {
        mSounds.stopAlarm();
        snoozeHandler.removeCallbacks(snoozeRunnable);
        AlarmDialogActivity.alarm = null;
    }

    private void snoozeAlarm() {
        stopAlarm();
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
        stopAlarm();
        super.onDestroy();
    }


    private void createAndShowAlarmDialog(final ParseTask alarm) {

        if (isDestroyed()) {
            return;
        }

        currentAlarmSound = alarm.isAborted() ? Sounds.ALARM_CANCELLED : Sounds.ALARM_NEW;
        mSounds.playSoundRepeating(currentAlarmSound);

        Log.d(TAG, "createAndShowAlarmDialog");

        String alarmBody =
                alarm.getClientName() + "\n" +
                        alarm.getFullAddress() + "\n" +
                        getText(R.string.security_level) + ": " + alarm.getPriority();

        if (alarm.isAborted()) {
            String canceled = "--- " + getString(R.string.canceled).toUpperCase() + " ---";
            alarmBody = canceled + "\n\n" + alarmBody + "\n\n" + canceled;
        }

        Log.d(TAG, "alarmBody: " + alarmBody);

        new CommonDialogsBuilder.MaterialDialogs(AlarmDialogActivity.this).ok(R.string.alarm, alarmBody, new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (alarm.isPending()) {
                    alarmController.performAction(AlarmController.ACTION.ACCEPT, alarm, false);
                }
                AlarmDialogActivity.this.finish();

                Intent intent = new Intent(AlarmDialogActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(MainActivity.SELECT_ALARMS, true);
                AlarmDialogActivity.this.startActivity(intent);

                stopAlarm();
                AlarmNotification.cancel(AlarmDialogActivity.this);
            }
        })
        .cancelable(false)
        .canceledOnTouchOutside(false)
        .show();


    }


}
