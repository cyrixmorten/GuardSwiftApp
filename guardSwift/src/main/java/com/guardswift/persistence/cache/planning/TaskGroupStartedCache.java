package com.guardswift.persistence.cache.planning;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class TaskGroupStartedCache extends ParseCache<TaskGroupStarted> {

    private static final String TAG = TaskGroupStartedCache.class.getSimpleName();

    private static final String SELECTED = "selected";


    @Inject
    TaskGroupStartedCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(TaskGroupStarted.class, context);
    }

    public void setSelected(TaskGroupStarted taskGroupStarted) {
        put(SELECTED, taskGroupStarted);
    }

    public TaskGroupStarted getSelected() {
        return get(SELECTED);
    }

}
