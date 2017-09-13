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

    public interface GenericTaskCache {

        String SELECTED = "selected";
        String ARRIVED = "arrived";

        String GEOFENCED_ALL = "geofenced_all";
        String GEOFENCE_WITHIN = "geofence_within";
        String GEOFENCE_OUTSIDE = "geofence_outside";

        void setSelected(ParseTask task);

        ParseTask getSelected();

        void addArrived(ParseTask task);

        void removeArrived(ParseTask task);

        Set<ParseTask> getArrived();

        boolean isArrived(ParseTask task);

        // clear all geofence data
        void clearGeofenced();

        Set<ParseTask> getAllGeofencedTasks();

        void addGeofencedTasks(List<ParseTask> tasks);

        void removeAllGeofencedTasks();

        boolean moveWithinGeofence(ParseTask task);

        boolean moveOutsideGeofence(ParseTask task);

        void removeGeofence(ParseTask task);

        Set<ParseTask> getWithinGeofence();

        Set<ParseTask> getOutsideGeofence();

        ParseTask getConcreteTask();

        void clear();
    }


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

    public void addArrived(ParseTask task) {
        taskCache.addArrived(task);
    }

    public boolean isArrived(ParseTask task) {
        return taskCache.isArrived(task);
    }

    public void removeArrived(ParseTask task) {
        taskCache.removeArrived(task);
    }

    public Set<ParseTask> getArrived() {
        return taskCache.getArrived();
    }


    public void setAllGeofencedTasks(List<ParseTask> tasks) {
        removeAllGeofencedTasks();
        taskCache.addGeofencedTasks(tasks);
    }

    public boolean isGeofenced(ParseTask task) {
        for (ParseTask taskGeofenced : taskCache.getAllGeofencedTasks()) {
            if (task.getObjectId().equals(taskGeofenced.getObjectId()))
                return true;
        }
        return false;
    }

    public Set<ParseTask> getAllGeofencedTasks() {

        return taskCache.getAllGeofencedTasks();
    }

    public void removeAllGeofencedTasks() {
        taskCache.removeAllGeofencedTasks();
    }

    public void clear() {
        taskCache.clearGeofenced();
        taskCache.clear();
        super.clear();
    }


    public void removeGeofence(ParseTask task) {
        taskCache.removeGeofence(task);
    }


    public boolean moveWithinGeofence(ParseTask task) {

        return taskCache.moveWithinGeofence(task);
    }

    public boolean isWithinGeofence(ParseTask task) {
        for (ParseTask taskWithin : taskCache.getWithinGeofence()) {
            if (task.getObjectId().equals(taskWithin.getObjectId()))
                return true;
        }
        return false;
    }

    public Set<ParseTask> getWithinGeofence() {
        return taskCache.getWithinGeofence();
    }

    public boolean moveOutsideGeofence(ParseTask task) {
        return taskCache.moveOutsideGeofence(task);
    }

    public boolean isMovedOutsideGeofence(ParseTask task) {
        for (ParseTask taskMovedOutside : taskCache.getOutsideGeofence()) {
            if (task.getObjectId().equals(taskMovedOutside.getObjectId()))
                return true;
        }
        return false;
    }


    public Set<ParseTask> getOutsideGeofence() {
        return taskCache.getOutsideGeofence();
    }


}
