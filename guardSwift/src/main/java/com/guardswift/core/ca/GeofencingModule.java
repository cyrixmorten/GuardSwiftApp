package com.guardswift.core.ca;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Lists;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.TaskFactory;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;

import static com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;

/**
 * Created by cyrix on 3/12/15.
 */
public class GeofencingModule {

    private static final String TAG = GeofencingModule.class.getSimpleName();

    @Inject
    GSTasksCache tasksCache;

    private final Context context;

//    private final Object enterLock = new Object();
//    private final Object exitLock = new Object();

    @Inject
    public GeofencingModule(@ForApplication Context context) {

        this.context = context;
    }

//    public void rebuildGeofences(GSTask task, String caller) {
//        Log.e(TAG, "-- !! Rebuilding Geofences !! -- " + caller);
//        RegisterGeofencesIntentService.start(context, task);
//    }

    public void onWithinGeofences(String[] strings) {

        if (strings.length == 0)
            return;

//        Log.d(TAG, "-- onWithin " + Arrays.toString(strings));


        findTasksMatching(strings).onSuccess(new Continuation<List<GSTask>, Object>() {
            @Override
            public Object then(Task<List<GSTask>> listTask) throws Exception {


                for (GSTask task : listTask.getResult()) {

                    task.getGeofenceStrategy().withinGeofence();

//                    GSTask.getActivityStrategy().handleActivityInsideGeofence(context, ActivityDetectionModule.Recent.getDetectedActivity());
                }


                return null;
            }
        });

    }

    public void onEnteredGeofences(final String[] strings, final boolean usingGPS) {

        if (strings.length == 0)
            return;



//        synchronized (enterLock) {
//            Log.d(TAG, "-- !onEnter! " + Arrays.toString(strings));

//        final DetectedActivity latestDetectedActivity = ActivityDetectionModule.Recent.getDetectedActivity();

        findTasksMatching(strings).onSuccess(new Continuation<List<GSTask>, Object>() {
            @Override
            public Object then(Task<List<GSTask>> listTask) throws Exception {



//                int moved = 0;
                for (GSTask task : listTask.getResult()) {

                    Log.e(TAG, "enterGeofence: " + task.getClientName());

                    task.getGeofenceStrategy().enterGeofence();
                    if (tasksCache.moveWithinGeofence(task)) {
                        Log.e(TAG, "moved inside");
//                        task.getActivityStrategy().handleActivityInsideGeofence(latestDetectedActivity);
//                        moved++;
                    }

//                    if (!GeofencingModule.Recent.getWithinGeofence().contains(task)) {
//                        Log.d(TAG, "Adding task " + task.getParseObject().getObjectId());
//                        GeofencingModule.Recent.addWithinGeofence(task);
//                        task.getGeofenceStrategy().enterGeofence(context, task, usingGPS);
//                    }

//                    if (BuildConfig.DEBUG) {
//                            GSTask.EVENT_TYPE event_type = (usingGPS) ? GSTask.EVENT_TYPE.GEOFENCE_ENTER_GPS : GSTask.EVENT_TYPE.GEOFENCE_ENTER;
//                            new EventLog.Builder(context)
//                                    .taskPointer(task, event_type)
//                                    .event(context.getString(R.string.event_geofence_enter))
//                                    .automatic(true)
//                                    .eventCode(EventLog.EventCodes.AUTOMATIC_GEOFENCE_ENTER).saveAsync();
//                    }

//                    GSTask.getActivityStrategy().handleActivityInsideGeofence(context, ActivityDetectionModule.Recent.getDetectedActivity());
                }

                if (!listTask.getResult().isEmpty()) {
                    EventBusController.postUIUpdate();
                }

//                Log.d(TAG, "-- onEnter " + moved + " " + Arrays.toString(strings));


                return null;
            }
        });
//        }

    }

    public void onExitedGeofences(final String[] strings, final boolean usingGPS) {

        if (strings.length == 0)
            return;


//        synchronized (exitLock) {
//            Log.d(TAG, "-- !onExit! " + Arrays.toString(strings));
//        final DetectedActivity latestDetectedActivity = ActivityDetectionModule.Recent.getDetectedActivity();

        findTasksMatching(strings).onSuccess(new Continuation<List<GSTask>, Object>() {
            @Override
            public Object then(Task<List<GSTask>> listTask) throws Exception {

//                int moved = 0;
                for (GSTask task : listTask.getResult()) {

                    Log.e(TAG, "exitGeofence: " + task.getClientName());

                    task.getGeofenceStrategy().exitGeofence();

                    if (tasksCache.moveOutsideGeofence(task)) {
                        Log.e(TAG, "moved outside");
//                        task.getActivityStrategy().handleActivityOutsideGeofence(latestDetectedActivity);
//                        moved++;
                    }

//                    if (GeofencingModule.Recent.removeWithinGeofence(task)) {
//                        Log.d(TAG, "Removing task " + task.getParseObject().getObjectId());
//                        task.getGeofenceStrategy().exitGeofence(context, task, usingGPS);
//                    }
//
//                    if (BuildConfig.DEBUG) {
//                            GSTask.EVENT_TYPE event_type = (usingGPS) ? GSTask.EVENT_TYPE.GEOFENCE_EXIT_GPS : GSTask.EVENT_TYPE.GEOFENCE_EXIT;
//                            new EventLog.Builder(context)
//                                    .taskPointer(task, event_type)
//                                    .event(context.getString(R.string.event_geofence_exit))
//                                    .automatic(true)
//                                    .eventCode(EventLog.EventCodes.AUTOMATIC_GEOFENCE_EXIT).saveAsync();
//                    }

//                    GSTask.getActivityStrategy().handleActivityOutsideGeofence(context, ActivityDetectionModule.Recent.getDetectedActivity());
                }

                if (!listTask.getResult().isEmpty()) {
                    EventBusController.postUIUpdate();
                }

//                Log.d(TAG, "-- onExit " + moved + " " + Arrays.toString(strings));

                return null;
            }
        });
//        }
    }


    public void matchGeofencedWithDetectedActivity(DetectedActivity detectedActivity) {

        Log.d(TAG, "matchGeofencedWithDetectedActivity: " + ActivityDetectionModule.getNameFromType(detectedActivity.getType()));

        Set<GSTask> allWithinGeofence = tasksCache.getWithinGeofence();
        Log.d(TAG, "matchGeofencedWithDetectedActivity within geofence: " + allWithinGeofence);
        for (GSTask task : allWithinGeofence) {
            task.getActivityStrategy().handleActivityInsideGeofence(detectedActivity);
        }

        Set<GSTask> allOutsideGeofence = tasksCache.getOutsideGeofence();
        Log.d(TAG, "matchGeofencedWithDetectedActivity outside geofence: " + allOutsideGeofence);
        for (GSTask task : allOutsideGeofence) {
            task.getActivityStrategy().handleActivityOutsideGeofence(detectedActivity);
        }

    }

    public Task<List<GSTask>> queryAllGeofenceTasks(int withinKm) {

        Log.d(TAG, "queryAllGeofenceTasks withinKm: " + withinKm);

        final Task<List<GSTask>>.TaskCompletionSource successful = Task.create();

        final List<GSTask> geofenceResults = Lists.newArrayList();

        ArrayList<Task<List<BaseTask>>> queryGeofenceTasks = new ArrayList<>();

        List<BaseTask> allGSTasks = new TaskFactory().getTasks();

        for (final BaseTask gsTask : allGSTasks) {
            Task<List<BaseTask>> geofencedTasks = gsTask.getGeofenceStrategy().queryGeofencedTasks(withinKm);
            geofencedTasks.onSuccess(new Continuation<List<BaseTask>, Object>() {
                @Override
                public Object then(Task<List<BaseTask>> listTask) throws Exception {
                    Log.d(TAG, "Found " + listTask.getResult() + " " + gsTask.getTaskType());
                    for (BaseTask taskObject : listTask.getResult()) {
                        geofenceResults.add(taskObject);
                    }
                    return null;
                }
            });


            queryGeofenceTasks.add(geofencedTasks);
        }

        Task.whenAll(queryGeofenceTasks).continueWith(new Continuation<Void, Void>() {

            @Override
            public Void then(Task<Void> result) throws Exception {
                if (result.isCancelled() || result.isFaulted()) {
                    successful.setError(result.getError());
                    new HandleException(TAG, " queryAllGeofenceTasks", result.getError());
                } else {
                    successful.setResult(geofenceResults);
                }
                return null;
            }
        });

        return successful.getTask();
    }


    /**
     * Locates all tasks matching the objectId's passed as param
     *
     * @param objectIds
     * @return
     */
    private Task<List<GSTask>> findTasksMatching(String[] objectIds) {
        final Task<List<GSTask>>.TaskCompletionSource successful = Task.create();

        final List<GSTask> geofenceResults = Lists.newArrayList();

        ArrayList<Task<List<BaseTask>>> tasks = new ArrayList<>();

        List<BaseTask> allGSTasks = new TaskFactory().getTasks();

        for (BaseTask gsTask : allGSTasks) {
            ParseQuery<BaseTask> geofencedQuery = gsTask.getQueryBuilder(true).matchingObjectIds(objectIds).build();
            Task<List<BaseTask>> geofencedTask = geofencedQuery.findInBackground();
            geofencedTask.onSuccess(new Continuation<List<BaseTask>, Object>() {
                @Override
                public Object then(Task<List<BaseTask>> listTask) throws Exception {
                    for (BaseTask taskObject : listTask.getResult()) {
                        geofenceResults.add((GSTask) taskObject);
                    }
                    return null;
                }
            });


            tasks.add(geofencedTask);
        }

        Task.whenAll(tasks).continueWith(new Continuation<Void, Void>() {

            @Override
            public Void then(Task<Void> result) throws Exception {
                if (result.isCancelled() || result.isFaulted()) {
                    successful.setError(result.getError());
                    new HandleException(TAG, " findTasksMatching", result.getError());
                } else {
                    successful.setResult(geofenceResults);
                }
                return null;
            }
        });

        return successful.getTask();
    }


//    public static class Recent {
//
//        private static Set<GSTask> allGSTasks = Sets.newConcurrentHashSet();
//        private static Set<GSTask> withinGeoFence = Sets.newConcurrentHashSet();
//
//        private static Set<GSTask> outsideGeoFence = Sets.newConcurrentHashSet();
//
//
//        public static void setAllGeofencedTasks(List<GSTask> tasks) {
//            clearAllGeofencedTasks();
//            allGSTasks.addAll(tasks);
//        }
//
//        public static Set<GSTask> getAllGeofencedTasks() {
//            return allGSTasks;
//        }
//
//        public static void clearAllGeofencedTasks() {
//            Log.e(TAG, "clearAllGeofenceTasks");
//            allGSTasks.clear();
//            withinGeoFence.clear();
//            outsideGeoFence.clear();
//        }
//
//        public static boolean moveWithinGeofence(GSTask task) {
//
//            boolean addWithin = withinGeoFence.addUnique(task);
//            boolean removeOutside = outsideGeoFence.remove(task);
//
//
//            if (addWithin) {
//                EventBusController.postUIUpdate(task, 1000);
//            }
//
//            return addWithin;
//        }
//
//
//        public static boolean moveOutsideGeofence(GSTask task) {
//            boolean removeWithin = withinGeoFence.remove(task);
//            boolean addOutside = outsideGeoFence.addUnique(task);
//
//            if (removeWithin) {
//                EventBusController.postUIUpdate(task, 1000);
//            }
//
//            return removeWithin;
//        }
//
//        public static void removeGeofence(GSTask GSTask) {
//            outsideGeoFence.remove(GSTask);
//        }
//
//
//        public static Set<GSTask> getWithinGeofence() {
//            return withinGeoFence;
//        }
//
//        public static Set<GSTask> getOutsideGeofence() {
//            return outsideGeoFence;
//        }
//
//
//    }


}
