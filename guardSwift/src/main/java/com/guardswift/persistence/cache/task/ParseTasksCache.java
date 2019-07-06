package com.guardswift.persistence.cache.task;

import android.content.Context;
import android.util.Log;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.Preferences;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class ParseTasksCache extends Preferences {

    private static final String TAG = ParseTasksCache.class.getSimpleName();

    private static final String LAST_SELECTED_TASK_TYPE = "LAST_SELECTED_TASK_TYPE";


    private TaskCache taskCache;

    @Inject
    public ParseTasksCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context, TaskCache taskCache) {
        super(context, "ParseTasksCache");

        this.taskCache = taskCache;
    }


    public void setSelected(ParseTask task) {

        taskCache.setSelected(task);

        put(LAST_SELECTED_TASK_TYPE, task.getTaskType().toString());

        Log.d(TAG, "LAST_SELECTED_TASK_TYPE: " + task.getTaskType().toString());

    }

    public ParseTask getLastSelected() {
        return taskCache.getSelected();
    }


    public void clear() {
        taskCache.clear();
        super.clear();
    }


}
