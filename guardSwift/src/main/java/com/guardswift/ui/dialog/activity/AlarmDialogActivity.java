package com.guardswift.ui.dialog.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.guardswift.R;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GuardLoginActivity;
import com.guardswift.core.tasks.controller.AlarmController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.eventbus.events.ParseObjectUpdatedEvent;
import com.guardswift.ui.dialog.fragment.AlarmDialogFragment;
import com.guardswift.ui.dialog.fragment.AlarmDialogFragment.AlarmDialogCallback;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.cache.task.AlarmCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.util.Sounds;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

//import com.guardswift.modules.LocationsModule;

public class AlarmDialogActivity extends AbstractDialogActivity {

	protected static final String TAG = AlarmDialogActivity.class
			.getSimpleName();

	@Inject FragmentManager fm;

    @Inject
    AlarmCache alarmCache;
    @Inject
    GuardCache guardCache;
    @Inject
    AlarmController alarmController;

	@Inject Sounds mSounds;

    private static List<Alarm> mAlarms;
	private static Alarm mAlarm;

    private boolean isNew = true;
    private Handler snoozeHandler = new Handler();
    private Runnable snoozeRunnable = new SnoozeRunnable();

    public static void show(final Context context) {
        AlarmCache alarmCache = GuardSwiftApplication.getInstance().getCacheFactory().getAlarmCache();
        GuardCache guardCache = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();

        if (!guardCache.isLoggedIn())
            return;

        ParseQuery<Alarm> query = new Alarm.QueryBuilder(true)
                .whereNotAssigned().notMatching(alarmCache.getSelected())
                .whereNotIgnoredBy(guardCache.getLoggedIn()).sortedByCreateDate().build();

        query.findInBackground(new FindCallback<Alarm>() {

            @Override
            public void done(List<Alarm> alarms, ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage(), e);
                    return;
                }
                Log.d(TAG, "Found alarms: " + alarms.size());
                if (alarms.size() > 0) {

                    mAlarms = alarms;
                    mAlarm = alarms.get(0);

                    Intent i = new Intent(context, AlarmDialogActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);


                }
            }
        });
    }

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

        alarmCache.setDialogShowing(true);

		Log.d(TAG, "onCreate");

        createAndShowAlarmDialog();
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
        alarmCache.setDialogShowing(false);
		super.onDestroy();
	}


	public void onEventMainThread(ParseObjectUpdatedEvent ev) {
		ExtendedParseObject object = ev.getObject();
		if (object instanceof Alarm) {

			Alarm alarm = (Alarm) object;

			if (alarm.equals(mAlarm) && alarm.isAccepted()) {
				this.finish();
			}

		}
	}

	private void createAndShowAlarmDialog() {

		Log.e(TAG, "showDialog");

        if (mAlarm.getClient() == null) {
            Crashlytics.logException(new IllegalStateException("Alarm missing client: " + mAlarm.getObjectId()));
            finish();
            return;
        }


        ParseGeoPoint targetGeoPoint = mAlarm.getClient().getPosition();
        ParseModule.DistanceStrings distanceStrings = ParseModule.distanceBetweenString(
                LocationModule.Recent.getLastKnownLocation(), targetGeoPoint);

		DialogFragment dialog = AlarmDialogFragment.newInstance(guardCache.isLoggedIn(),
				mAlarm, mAlarms.size(), distanceStrings, new AlarmDialogCallback() {


                    @Override
                    public void ok() {
                        // neutral button when not logged in

                        Intent intent = new Intent(AlarmDialogActivity.this, GuardLoginActivity.class);
                        startActivity(Intent.makeRestartActivityTask(intent.getComponent()));

                        AlarmDialogActivity.this.finish();
                    }

					@Override
					public void ignore() {
                        alarmController.performAction(AlarmController.ACTION.IGNORE, mAlarm, false);

						AlarmDialogActivity.this.finish();
					}

                    @Override
					public void accept() {
                        alarmController.performAction(AlarmController.ACTION.ACCEPT, mAlarm);

                        new MaterialDialog.Builder(AlarmDialogActivity.this)
                                .title(R.string.alarm)
                                .positiveText(android.R.string.ok)
                                .negativeText(android.R.string.cancel)
                                .content(R.string.open_alarm_details)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        alarmController.performAction(TaskController.ACTION.OPEN, mAlarm, false);
                                        AlarmDialogActivity.this.finish();
                                    }
                                }).show();

					}

					@Override
					public void open() {
                        alarmController.performAction(AlarmController.ACTION.OPEN, mAlarm, false);
						AlarmDialogActivity.this.finish();
					}
				});
		dialog.setCancelable(false);
        fm.beginTransaction().add(dialog, "dialog").commitAllowingStateLoss();

//        if (parse.isAlarmResponsible()) {
//            Toast.makeText(this, "ALARM RESPONSIBLE", Toast.LENGTH_LONG).show();
//        }
	}



}
