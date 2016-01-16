package com.guardswift.core.tasks.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.guardswift.ui.dialog.activity.AlarmDialogActivity;
import com.guardswift.dagger.InjectingIntentService;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.Preferences;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

import javax.inject.Inject;


public class AlarmDownloaderIntentService extends InjectingIntentService {

    private static final String TAG = AlarmDownloaderIntentService.class.getSimpleName();

    public AlarmDownloaderIntentService() {
        super("AlarmDownloader");
    }

    @Inject
    GuardCache guardCache;

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        Preferences pref = new Preferences(getApplicationContext());

        Log.i(TAG, "Starting service @ " + SystemClock.elapsedRealtime());

//        if (Guard.Recent.getSelected(parsePref) == null) {
//            ParseAlarmReceiver.completeWakefulIntent(intent);
//            Log.i(TAG, "Not logged in - do not notify!");
//            return;
//        }

        fetchAndNotify(getApplicationContext(), intent);
    }


    private void fetchAndNotify(final Context context, Intent intent) {
        fetchAndNotify(context, intent, 0);
    }

    private void fetchAndNotify(final Context context, final Intent intent, final int retryCount) {

        // Log.e(TAG, jsonAlarm.toString());

        final Guard guard = guardCache.getLoggedIn();
        ParseQuery<Alarm> query = new Alarm.QueryBuilder(false)
                .whereNotAssigned()
                .whereNotIgnoredBy(guard)
                .build();

        new Alarm().updateAll(query,
                new ExtendedParseObject.DataStoreCallback<Alarm>() {

                    @Override
                    public void success(List<Alarm> alarms) {
                        if (!alarms.isEmpty()) {
                            AlarmDialogActivity.show(context);

                            try {
                                // hold on to the wakelock while potentially creating the alarm dialog
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            ParseAlarmReceiver.completeWakefulIntent(intent);
                        }
                    }

                    @Override
                    public void failed(ParseException e) {
                        Log.e(TAG, e.getMessage());
                        if (retryCount == 5) {
                            ParseAlarmReceiver.completeWakefulIntent(intent);
                            return; // give up
                        }

                        // unlikely as it takes internet connectivity to receive
                        // the push
                        if (e.getCode() == ParseException.TIMEOUT
                                || e.getCode() == ParseException.CONNECTION_FAILED) {
                            backOffAndRetry(context, intent, retryCount);
                        }
                    }

                });
    }

    private void backOffAndRetry(final Context context, final Intent intent, final int retryCount) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                fetchAndNotify(context, intent, retryCount + 1);
            }
        }, 5000 * retryCount);
    }


}
