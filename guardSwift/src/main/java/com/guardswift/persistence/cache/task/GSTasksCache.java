package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.Preferences;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.TaskFactory;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class GSTasksCache extends Preferences {

    private static final String TAG = GSTasksCache.class.getSimpleName();

    private static final String LAST_SELECTED_TASK_TYPE = "LAST_SELECTED_TASK_TYPE";

    public interface TaskCache {

        String SELECTED = "selected";
        String ARRIVED = "arrived";

        String GEOFENCED_ALL = "geofenced_all";
        String GEOFENCE_WITHIN  = "geofence_within";
        String GEOFENCE_OUTSIDE  = "geofence_outside";

        void setSelected(GSTask task);
        GSTask getSelected();

        void addArrived(GSTask task);
        void removeArrived(GSTask task);
        Set<GSTask> getArrived();

        boolean isArrived(GSTask task);

        // clear all geofence data
        void clearGeofenced();

        Set<GSTask> getAllGeofencedTasks();
        void addGeofencedTasks(List<GSTask> tasks);
        void removeAllGeofencedTasks();

        boolean moveWithinGeofence(GSTask task);
        boolean moveOutsideGeofence(GSTask task);
        void removeGeofence(GSTask task);
        Set<GSTask> getWithinGeofence();
        Set<GSTask> getOutsideGeofence();

        boolean acceptsTask(GSTask task);
        GSTask getConcreteTask();

        void clear();
    }


    private List<TaskCache> taskCaches = Lists.newArrayList();

    @Inject
    public GSTasksCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context, StaticTaskCache staticTaskCache, CircuitUnitCache circuitUnitCache, DistrictWatchClientCache districtWatchClientCache) {
        super(context, "GSTasksCache");

        taskCaches.add(staticTaskCache);
        taskCaches.add(circuitUnitCache);
        taskCaches.add(districtWatchClientCache);
    }


    public void setSelected(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task)) {
                taskCache.setSelected(task);

                put(LAST_SELECTED_TASK_TYPE, task.getTaskType().toString());

                return;
            }
        }

        throw new IllegalArgumentException("task not known " + task);
    }

    public GSTask getLastSelected() {
        String lastSelectedTaskType = getString(LAST_SELECTED_TASK_TYPE);

        for (GSTask task: new TaskFactory().getTasks()) {
            GSTask.TASK_TYPE type = task.getTaskType();
            if (type.toString().equals(lastSelectedTaskType)) {
                return (GSTask)task.getCache().getSelected();
            }
        }

        return null;
    }

    public void addArrived(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task)) {
                taskCache.addArrived(task);
                return;
            }
        }

        throw new IllegalArgumentException("task not known " + task);
    }

    public boolean isArrived(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task)) {
                return taskCache.isArrived(task);
            }
        }

        throw new IllegalArgumentException("task not known " + task);
    }

    public void removeArrived(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task)) {
                taskCache.removeArrived(task);
                return;
            }
        }

        throw new IllegalArgumentException("task not known " + task);
    }

    public Set<GSTask> getArrived(GSTask.TASK_TYPE task_type) {
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.getConcreteTask().getTaskType() == task_type) {
                return taskCache.getArrived();
            }
        }

        throw new IllegalArgumentException("task not known " + task_type);
    }

    public Set<GSTask> getArrivedWithCheckpoints(GSTask.TASK_TYPE task_type) {
        Set<GSTask> arrivedWithCheckpoints = Sets.newHashSet();
        for (GSTask task: getArrived(task_type)) {
            Client client = task.getClient();
            if (client != null && client.hasCheckPoints()) {
                arrivedWithCheckpoints.add(task);
            }
        }
//        for (TaskCache taskCache: taskCaches) {
//            if (taskCache.getConcreteTask().getTaskType() == task_type) {
//                Set<GSTask> arrived = taskCache.getArrived();
//                for (GSTask task: arrived) {
//                    Client client = task.getClient();
//                    if (client != null && client.hasCheckPoints()) {
//                        arrivedWithCheckpoints.add(task);
//                    }
//                }
//            }
//        }

        return arrivedWithCheckpoints;
    }

    public void setAllGeofencedTasks(List<GSTask> tasks) {
        removeAllGeofencedTasks();
        for (TaskCache taskCache: taskCaches) {
            taskCache.addGeofencedTasks(tasks);
        }
    }

    public boolean isGeofenced(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            for (GSTask taskGeofenced: taskCache.getAllGeofencedTasks()) {
                if (task.getObjectId().equals(taskGeofenced.getObjectId()))
                    return true;
            }
        }
        return false;
    }

    public Set<GSTask> getAllGeofencedTasks() {
        Set<GSTask> tasks = Sets.newConcurrentHashSet();
        for (TaskCache taskCache: taskCaches) {
            tasks.addAll(taskCache.getAllGeofencedTasks());
        }
        return tasks;
    }

    public void removeAllGeofencedTasks() {
        for (TaskCache taskCache: taskCaches) {
            taskCache.removeAllGeofencedTasks();
        }
    }

    public void clear() {
        for (TaskCache taskCache: taskCaches) {
            taskCache.clear();
        }
        super.clear();
    }





    public void removeGeofence(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task)) {
                taskCache.removeGeofence(task);
            }
        }
    }


    public boolean moveWithinGeofence(GSTask task) {
        boolean movedWithin = false;
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task) && taskCache.moveWithinGeofence(task)) {
//                task.getGeofenceStrategy().enterGeofence(context, task);
                movedWithin = true;
            }
        }
        // notify UI
//        if (movedWithin) {
//            EventBusController.postUIUpdate(task, 1000);
//        }
        return movedWithin;
    }

    public boolean isWithinGeofence(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (!taskCache.getConcreteTask().getClass().equals(task.getClass())) {
//                Log.d(TAG, "isWithinGeofence skipping taskCache " + taskCache.getConcreteTask().getClass() + " != " + task.getClass());
                continue;
            }
            for (GSTask taskWithin: taskCache.getWithinGeofence()) {
                if (task.getObjectId().equals(taskWithin.getObjectId()))
                    return true;
            }
        }
        return false;
    }
    public Set<GSTask> getWithinGeofence() {
        Set<GSTask> tasks = Sets.newConcurrentHashSet();
        for (TaskCache taskCache: taskCaches) {
            tasks.addAll(taskCache.getWithinGeofence());
        }
        return tasks;
    }

    public boolean moveOutsideGeofence(GSTask task) {
        boolean movedOutside = false;
        for (TaskCache taskCache: taskCaches) {
            if (taskCache.acceptsTask(task) && taskCache.moveOutsideGeofence(task)) {
//                task.getGeofenceStrategy().exitGeofence(context, task);
                movedOutside = true;
            }
        }
        // notify UI
//        if (movedOutside) {
//            EventBusController.postUIUpdate(task, 1000);
//        }
        return movedOutside;
    }

    public boolean isMovedOutsideGeofence(GSTask task) {
        for (TaskCache taskCache: taskCaches) {
            if (!taskCache.getConcreteTask().getClass().equals(task.getClass())) {
//                Log.d(TAG, "isMovedOutsideGeofence skipping taskCache " + taskCache.getConcreteTask().getClass() + " != " + task.getClass());
                continue;
            }
            for (GSTask taskMovedOutside: taskCache.getOutsideGeofence()) {
                if (task.getObjectId().equals(taskMovedOutside.getObjectId()))
                    return true;
            }
        }
        return false;
    }
    public Set<GSTask> getOutsideGeofence() {
        Set<GSTask> tasks = Sets.newConcurrentHashSet();
        for (TaskCache taskCache: taskCaches) {
            tasks.addAll(taskCache.getOutsideGeofence());
        }
        return tasks;
    }


}
