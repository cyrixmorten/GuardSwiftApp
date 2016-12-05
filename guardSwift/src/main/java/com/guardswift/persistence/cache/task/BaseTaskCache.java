package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

import java.util.List;
import java.util.Set;

/**
 * Created by cyrix on 10/21/15.
 */
public abstract class BaseTaskCache<T extends ParseObject & GSTask> extends ParseCache<T> implements GSTasksCache.GenericTaskCache {


    private final Class<T> type;

    protected BaseTaskCache(Class<T> subClass, Context context) {
        super(subClass, context);
        this.type = subClass;
    }


    @Override
    public boolean acceptsTask(GSTask task) {
        // http://stackoverflow.com/questions/1570073/java-instanceof-and-generics
        return task != null && (this.type.isAssignableFrom(task.getClass()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelected(GSTask task) {
        if (acceptsTask(task)) {
            put(SELECTED, (T) task);
        }
    }

    public T getSelected() {
        return get(SELECTED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addArrived(GSTask task) {
        if (acceptsTask(task)) {
            addUnique(ARRIVED, (T) task);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeArrived(GSTask task) {
        if (acceptsTask(task)) {
            removeUnique(ARRIVED, (T) task);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<GSTask> getArrived() {
        return (Set<GSTask>) (Set<?>) getSet(ARRIVED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isArrived(GSTask task) {
        return acceptsTask(task) && hasUnique(ARRIVED, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<GSTask> getAllGeofencedTasks() {
        return (Set<GSTask>) (Set<?>) getSet(GEOFENCED_ALL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addGeofencedTasks(List<GSTask> tasks) {
        for (GSTask task : tasks) {
            if (acceptsTask(task)) {
                addUnique(GEOFENCED_ALL, (T) task);
            }
        }
    }


    @Override
    public void clearGeofenced() {
        clearSet(GEOFENCED_ALL);
        clearSet(GEOFENCE_WITHIN);
        clearSet(GEOFENCE_OUTSIDE);
    }

    @Override
    public void removeAllGeofencedTasks() {
        clearSet(GEOFENCED_ALL);
//        clearSet(GEOFENCE_WITHIN);
//        clearSet(GEOFENCE_OUTSIDE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean moveWithinGeofence(GSTask task) {
        if (acceptsTask(task)) {
            removeUnique(GEOFENCE_OUTSIDE, (T) task);
            return addUnique(GEOFENCE_WITHIN, (T) task);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean moveOutsideGeofence(GSTask task) {
        if (acceptsTask(task)) {
            removeUnique(GEOFENCE_WITHIN, (T) task);
            return addUnique(GEOFENCE_OUTSIDE, (T) task);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeGeofence(GSTask task) {
        if (acceptsTask(task)) {
            removeUnique(GEOFENCE_OUTSIDE, (T) task);
            removeUnique(GEOFENCE_WITHIN, (T) task);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<GSTask> getWithinGeofence() {
        return (Set<GSTask>) (Set<?>) getSet(GEOFENCE_WITHIN);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<GSTask> getOutsideGeofence() {
        return (Set<GSTask>) (Set<?>) getSet(GEOFENCE_OUTSIDE);
    }


}
