package com.guardswift.jobs.oneoff;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.ui.GuardSwiftApplication;

import java.util.concurrent.TimeUnit;


public class RebuildGeofencesJob extends Job {

    public static final String TAG = "RebuildGeofencesJob";

    private static int jobId;
    private static boolean jobScheduled;

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        boolean force = params.getExtras().getBoolean("force", false);

        jobScheduled = false;

        if (new com.guardswift.util.Device(GuardSwiftApplication.getInstance()).isOnline()) {
            RegisterGeofencesIntentService.start(GuardSwiftApplication.getInstance(), force);
            return Result.SUCCESS;
        }

        return Result.RESCHEDULE;
    }


    public static void scheduleJob(boolean force) {
        if (jobScheduled && !force) {
            return;
        }

        Log.d(TAG, "scheduleJob force: " + force);

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putBoolean("force", force);

        jobId = new JobRequest.Builder(RebuildGeofencesJob.TAG)
                .setBackoffCriteria(TimeUnit.SECONDS.toMillis(10), JobRequest.BackoffPolicy.LINEAR)
                .setUpdateCurrent(true)
                .addExtras(extras)
                .startNow()
                .build()
                .schedule();

        jobScheduled = true;

        Log.d(TAG, "scheduleJob: " + jobId);
    }

    public static void cancelJob() {
        Log.d(TAG, "cancelJob: " + jobId);
        JobManager.instance().cancel(jobId);
    }

}
