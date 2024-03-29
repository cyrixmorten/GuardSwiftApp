package com.guardswift.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.guardswift.jobs.periodic.TrackerUploadJob;


public class GSJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case TrackerUploadJob.TAG:
                return new TrackerUploadJob();
            default:
                return null;
        }
    }
}
