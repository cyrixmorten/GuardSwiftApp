package com.guardswift.persistence.cache.planning;

import android.content.Context;

import com.google.common.collect.Lists;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class TaskGroupStartedCache extends ParseCache<TaskGroupStarted> {

    private static final String TAG = TaskGroupStartedCache.class.getSimpleName();

    private static final String ACTIVE = "active";
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

    public void addActive(TaskGroupStarted taskGroupStarted) {
        addUnique(ACTIVE, taskGroupStarted);
    }

    public TaskGroupStarted matching(TaskGroup taskGroup) {
        Set<TaskGroupStarted> taskGroupsStarted = getSet(ACTIVE);

        List<TaskGroupStarted> candidates = Lists.newArrayList();
        for (TaskGroupStarted taskGroupStarted: taskGroupsStarted) {
//            Log.w(TAG, "Active: " + taskGroupStarted.getName() + " - " + taskGroupStarted.getCreatedAt());
            if (taskGroupStarted.getTaskGroup().equals(taskGroup))
                candidates.add(taskGroupStarted);
        }
//        Log.w(TAG, "Candidates: " + candidates.size());
        TaskGroupStarted mostRecent = null;
        for (TaskGroupStarted candidate: candidates) {
            if (mostRecent == null) {
                mostRecent = candidate;
            } else {
                if (new DateTime(candidate.getCreatedAt()).isAfter(new DateTime(mostRecent.getCreatedAt()))) {
                    mostRecent = candidate;
                }
            }
        }
//        Log.w(TAG, "Mostrecent: " + mostRecent);
        return mostRecent;
    }


}
