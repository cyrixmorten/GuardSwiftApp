package com.guardswift.jobs.periodic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.ui.GuardSwiftApplication;

import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;


public class TrackerUploadJob extends Job {

    public static final String TAG = "TrackerUploadJob";

    private static int jobId;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Tracker.upload(GuardSwiftApplication.getInstance(), null, true).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    new HandleException(TAG, "Failed to perform periodic Tracker upload", task.getError());
                    return null;
                }

                Log.d(TAG, "Done uploading Tracker");

                return null;
            }
        });

        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        Log.d(TAG, "scheduleJob");
        jobId = new JobRequest.Builder(TrackerUploadJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    public static void cancelJob() {
        Log.d(TAG, "cancelJob");
        JobManager.instance().cancel(jobId);
    }

}
