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
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.cache.task.TaskCache;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.persistence.parse.execution.query.AlarmQueryBuilder;
import com.guardswift.core.tasks.controller.AlarmController;
import com.guardswift.eventbus.events.ParseObjectUpdatedEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.Sounds;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class AlarmDialogActivity extends AbstractDialogActivity {

    protected static final String TAG = AlarmDialogActivity.class
            .getSimpleName();

    @Inject
    FragmentManager fm;

    @Inject
    TaskCache alarmCache;
    @Inject
    AlarmController alarmController;

    @Inject
    Sounds mSounds;


    private boolean isNew = true;
    private Handler snoozeHandler = new Handler();
    private Runnable snoozeRunnable = new SnoozeRunnable();

    private static String alarmId;

    public static void start(final Context context, String alarmId) {

        if (!alarmId.equals(AlarmDialogActivity.alarmId)) {
            AlarmDialogActivity.alarmId = alarmId;

            Intent i = new Intent(context, AlarmDialogActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(TAG, "onCreate");


        ParseQuery<ParseTask> alarmQuery;
        if (!alarmId.isEmpty()) {
            alarmQuery = new AlarmQueryBuilder(true).matchingObjectId(alarmId).build();
        } else {
            alarmQuery = new AlarmQueryBuilder(true)
                    .whereStatus(ParseTask.STATUS.PENDING)
                    .sortByTimeStarted()
                    .build();
        }

        alarmQuery.getFirstInBackground(new GetCallback<ParseTask>() {
            @Override
            public void done(ParseTask alarm, ParseException e) {
                if (e != null) {
                    new HandleException(TAG, "Find alarm", e);
                    return;
                }

                if (alarm != null) {
                    createAndShowAlarmDialog(alarm);
                }
            }
        });

    }


    @Override
    protected void onResume() {
        if (isNew) {
            mSounds.playAlarmSound();
        } else {
            snoozeAlarm();
        }

        isNew = false;
        super.onResume();
    }

    private void stopAlarm() {
        mSounds.stopAlarm();
        snoozeHandler.removeCallbacks(snoozeRunnable);
    }

    private void snoozeAlarm() {

        Log.d(TAG, "Snooze");

        stopAlarm();
        snoozeHandler.postDelayed(snoozeRunnable, TimeUnit.MINUTES.toMillis(1));
    }

    private class SnoozeRunnable implements Runnable {

        @Override
        public void run() {
            if (mSounds != null)
                mSounds.playAlarmSound();
        }
    }

    @Override
    protected void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }


    public void onEventMainThread(ParseObjectUpdatedEvent ev) {
        ExtendedParseObject object = ev.getObject();
        // stop sound if accepted
    }

    private void createAndShowAlarmDialog(final ParseTask alarm) {

        if (isDestroyed()) {
            return;
        }

        Log.d(TAG, "createAndShowAlarmDialog");

        String alarmBody =
                alarm.getClientName() + "\n" +
                        alarm.getFullAddress() + "\n" +
                        getText(R.string.security_level) + ": " + alarm.getPriority();

        new CommonDialogsBuilder.MaterialDialogs(AlarmDialogActivity.this).ok(R.string.alarm, alarmBody, new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                alarmController.performAction(AlarmController.ACTION.ACCEPT, alarm, false);
                AlarmDialogActivity.this.finish();
            }
        })
        .cancelable(false)
        .canceledOnTouchOutside(false)
        .show();


    }


}
