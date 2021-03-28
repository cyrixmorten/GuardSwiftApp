package com.guardswift.fabric;


import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.guardswift.BuildConfig;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;

public class TrackEvent {

    public static void taskAction(TaskController.ACTION action, final ParseTask task, final boolean automatic) {
        if (!BuildConfig.DEBUG) {
            Bundle bundle = new Bundle();
            bundle.putString("TaskType", task.getTaskTypeString());
            bundle.putBoolean("Automatic", automatic);

            FirebaseAnalytics.getInstance(GuardSwiftApplication.getInstance()).logEvent(String.valueOf(action), bundle);
        }
    }
}
