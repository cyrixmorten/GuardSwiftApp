package com.guardswift.fabric;


import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.guardswift.BuildConfig;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;

public class TrackEvent {

    public static void taskAction(TaskController.ACTION action, final ParseTask task, final boolean automatic) {
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent(String.valueOf(action))
                    .putCustomAttribute("TaskType", task.getTaskTypeString())
                    .putCustomAttribute("Automatic", String.valueOf(automatic)));
        }
    }
}
