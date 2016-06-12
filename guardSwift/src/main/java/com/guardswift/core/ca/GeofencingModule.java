package com.guardswift.core.ca;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.tasks.geofence.NoGeofenceStrategy;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.TaskFactory;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

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

                    task.getGeofenceStrategy().enterGeofence();
                    if (tasksCache.moveWithinGeofence(task)) {
//                        Log.e(TAG, "enterGeofence: " + task.getClientName() + " from GPS: " + usingGPS);
                    }
                }


                return null;
            }
        });

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

//                    Log.e(TAG, "exitGeofence: " + task.getClientName());

                    task.getGeofenceStrategy().exitGeofence();

                    if (tasksCache.moveOutsideGeofence(task)) {
//                        Log.e(TAG, "moved outside");
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

//                if (!listTask.getResult().isEmpty()) {
//                    EventBusController.postUIUpdate();
//                }

//                Log.d(TAG, "-- onExit " + moved + " " + Arrays.toString(strings));

                return null;
            }
        });
//        }
    }


    public void matchGeofencedWithDetectedActivity(DetectedActivity detectedActivity) {

//        Log.d(TAG, "matchGeofencedWithDetectedActivity: " + ActivityDetectionModule.getNameFromType(detectedActivity.getType()));

        Set<GSTask> allWithinGeofence = tasksCache.getWithinGeofence();
//        Log.d(TAG, "matchGeofencedWithDetectedActivity within geofence: " + allWithinGeofence);
        for (GSTask task : allWithinGeofence) {
            task.getActivityStrategy().handleActivityInsideGeofence(detectedActivity);
        }

        Set<GSTask> allOutsideGeofence = tasksCache.getOutsideGeofence();
//        Log.d(TAG, "matchGeofencedWithDetectedActivity outside geofence: " + allOutsideGeofence);
        for (GSTask task : allOutsideGeofence) {
            task.getActivityStrategy().handleActivityOutsideGeofence(detectedActivity);
        }

    }

    public Task<List<BaseTask>> queryAllGeofenceTasks(int withinKm) {

//        Log.d(TAG, "queryAllGeofenceTasks withinKm: " + withinKm);

        final TaskCompletionSource<List<BaseTask>> promise = new TaskCompletionSource<>();

        final List<BaseTask> geofenceResults = Lists.newArrayList();

        ArrayList<Task<List<BaseTask>>> queryGeofenceTasks = new ArrayList<>();

        List<BaseTask> allGSTasks = new TaskFactory().getTasks();

        for (final BaseTask gsTask : allGSTasks) {
            Task<List<BaseTask>> geofencedTasks = gsTask.getGeofenceStrategy().queryGeofencedTasks(withinKm);
            geofencedTasks.onSuccess(new Continuation<List<BaseTask>, Object>() {
                @Override
                public Object then(Task<List<BaseTask>> listTask) throws Exception {
//                    Log.d(TAG, "Found " + listTask.getResult() + " " + gsTask.getTaskType());
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
                    promise.setError(result.getError());
//                    new HandleException(TAG, " queryAllGeofenceTasks", result.getError());
                } else {
                    promise.setResult(geofenceResults);
                }
                return null;
            }
        });

        return promise.getTask();
    }


    /**
     * Locates all tasks matching the geofenceIds passed as param
     *
     * @param geofenceIds
     * @return
     */
    private Task<List<GSTask>> findTasksMatching(final String[] geofenceIds) {
        final TaskCompletionSource<List<GSTask>> promise = new TaskCompletionSource<>();

        final List<GSTask> geofenceResults = Lists.newArrayList();

        ArrayList<Task<List<BaseTask>>> tasks = new ArrayList<>();

        List<BaseTask> allGSTasks = new TaskFactory().getTasks();
        Map<String, List<String>> objectIdsMap = mapGeofenceIdsToClassName(geofenceIds);

        for (final BaseTask gsTask : allGSTasks) {
            if (gsTask.getGeofenceStrategy() instanceof NoGeofenceStrategy) {
//                Log.d(TAG, "Skipping " + gsTask.getParseClassName());
                continue;
            }

            List<String> objectIdsList = objectIdsMap.get(gsTask.getParseClassName());
            if (objectIdsList == null) {
//                Log.w(TAG, "Did not found objectIdList for " + gsTask.getParseClassName());
                continue;
            }

            final String[] objectIdsArray = objectIdsList.toArray(new String[objectIdsList.size()]);
            final ParseQuery<BaseTask> geofencedQueryNetwork = gsTask.getQueryBuilder(false).matchingObjectIds(objectIdsArray).build();
            ParseQuery<BaseTask> geofencedQueryLDS = new ParseQuery<BaseTask>(geofencedQueryNetwork).fromLocalDatastore();
            Task<List<BaseTask>> geofencedTask = geofencedQueryLDS.findInBackground();
            geofencedTask.continueWith(new Continuation<List<BaseTask>, Object>() {
                @Override
                public Object then(Task<List<BaseTask>> task) throws Exception {
                    if (task.getError() != null) {
                        new HandleException(TAG, "findTasksMatching", task.getError());
//                        Log.e(TAG, "findTasksMatching failed for " + gsTask.getParseClassName());
//                        Log.e(TAG, Arrays.toString(objectIdsArray));


                        if (task.getError() instanceof ParseException && ((ParseException)task.getError()).getCode() == ParseException.OBJECT_NOT_FOUND) {
                            // attempt to recover
                            // might be deprecated due to GuardSwiftApplication bootstrapParseObjectsLocally
//                            Log.w(TAG, "Updating failed objectIds in LDS");
                            gsTask.updateAll(geofencedQueryNetwork, 100).continueWith(new Continuation<List<BaseTask>, Object>() {
                                @Override
                                public Object then(Task<List<BaseTask>> task) throws Exception {
                                    if (task.getError() != null) {
                                        new HandleException(TAG, "findTasksMatching recover", task.getError());
                                        return null;
                                    }
//                                    Log.w(TAG, "Successfully updated LDS for " + gsTask.getParseClassName() + " " + Arrays.toString(objectIdsArray));
                                    return null;
                                }
                            });
                        }

                    } else if (task.getResult() != null) {
                        geofenceResults.addAll(task.getResult());
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
                    promise.setError(result.getError());
                } else {
                    promise.setResult(geofenceResults);
                }
                return null;
            }
        });

        return promise.getTask();
    }

    private Map<String, List<String>> mapGeofenceIdsToClassName(String[] geofenceIds) {
        /**
         * Extract only those objectIds belonging to this task
         */
        Map<String, List<String>> objectIdsMap = Maps.newHashMap();
        for (String geofenceId : geofenceIds) {
            String[] parts = geofenceId.split(",");
            String parseClassName = parts[0];
            String objectId = parts[1];

            if (!objectIdsMap.containsKey(parseClassName)) {
                objectIdsMap.put(parseClassName, Lists.<String>newArrayList());
            }
            List<String> objectIds = objectIdsMap.get(parseClassName);
            objectIds.add(objectId);

            objectIdsMap.put(parseClassName, objectIds);
        }

//        Log.w(TAG, "mapGeofenceIdsToClassName");
//        for (String parseClassName : objectIdsMap.keySet()) {
//            Log.w(TAG, parseClassName + ": " + Arrays.toString(objectIdsMap.get(parseClassName).toArray()));
//        }

        return objectIdsMap;
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
