package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.List;
import java.util.Set;

public abstract class BaseTaskCache<T extends ParseTask> extends ParseCache<T> implements ParseTasksCache.GenericTaskCache {

    protected BaseTaskCache(Class<T> subClass, Context context) {
        super(subClass, context);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void setSelected(ParseTask task) {
        put(SELECTED, (T) task);
    }

    public T getSelected() {
        return get(SELECTED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addArrived(ParseTask task) {
        addUnique(ARRIVED, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeArrived(ParseTask task) {
        removeUnique(ARRIVED, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ParseTask> getArrived() {
        return (Set<ParseTask>) (Set<?>) getSet(ARRIVED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isArrived(ParseTask task) {
        return hasUnique(ARRIVED, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ParseTask> getAllGeofencedTasks() {
        return (Set<ParseTask>) (Set<?>) getSet(GEOFENCED_ALL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addGeofencedTasks(List<ParseTask> tasks) {
        for (ParseTask task : tasks) {
            addUnique(GEOFENCED_ALL, (T) task);
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
//        clearSet(GEOFENCE_WITHIN); // keeping within while rebuilding
        clearSet(GEOFENCE_OUTSIDE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean moveWithinGeofence(ParseTask task) {
        removeUnique(GEOFENCE_OUTSIDE, (T) task);
        return addUnique(GEOFENCE_WITHIN, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean moveOutsideGeofence(ParseTask task) {
        removeUnique(GEOFENCE_WITHIN, (T) task);
        return addUnique(GEOFENCE_OUTSIDE, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeGeofence(ParseTask task) {
        removeUnique(GEOFENCE_OUTSIDE, (T) task);
        removeUnique(GEOFENCE_WITHIN, (T) task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ParseTask> getWithinGeofence() {
        return (Set<ParseTask>) (Set<?>) getSet(GEOFENCE_WITHIN);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ParseTask> getOutsideGeofence() {
        return (Set<ParseTask>) (Set<?>) getSet(GEOFENCE_OUTSIDE);
    }


}
