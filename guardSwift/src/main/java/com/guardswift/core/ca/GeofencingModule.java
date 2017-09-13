package com.guardswift.core.ca;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.tasks.geofence.AlarmGeofenceStrategy;
import com.guardswift.core.tasks.geofence.RaidGeofenceStrategy;
import com.guardswift.core.tasks.geofence.RegularGeofenceStrategy;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.TaskQueryBuilder;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class GeofencingModule {

    private static final String TAG = GeofencingModule.class.getSimpleName();

    @Inject
    ParseTasksCache tasksCache;

    @Inject
    public GeofencingModule() {}


    public void onWithinGeofences(String[] strings) {

        if (strings.length == 0)
            return;

//        Log.d(TAG, "-- onWithin " + Arrays.toString(strings));


        findTasksMatching(strings).onSuccess(new Continuation<List<ParseTask>, Object>() {
            @Override
            public Object then(Task<List<ParseTask>> listTask) throws Exception {


                for (ParseTask task : listTask.getResult()) {
                    task.getGeofenceStrategy().withinGeofence();
                }


                return null;
            }
        });

    }

    public void onEnteredGeofences(final String[] strings, final boolean usingGPS) {

        if (strings.length == 0)
            return;

//            Log.w(TAG, "-- !onEnter! " + Arrays.toString(strings));

        findTasksMatching(strings).onSuccess(new Continuation<List<ParseTask>, Object>() {
            @Override
            public Object then(Task<List<ParseTask>> listTask) throws Exception {

//                Log.w(TAG, "enterGeofence tasks: " + listTask.getResult().size());
                for (ParseTask task : listTask.getResult()) {
                    task.getGeofenceStrategy().enterGeofence();
                    tasksCache.moveWithinGeofence(task);
//                    Log.w(TAG, "enterGeofence: " + task.getClientName() + " " + tasksCache.isWithinGeofence(task));
                }


                return null;
            }
        });

    }

    public void onExitedGeofences(final String[] strings, final boolean usingGPS) {

        if (strings.length == 0)
            return;

//            Log.w(TAG, "-- !onExit! " + Arrays.toString(strings));

        findTasksMatching(strings).onSuccess(new Continuation<List<ParseTask>, Object>() {
            @Override
            public Object then(Task<List<ParseTask>> listTask) throws Exception {

//                Log.w(TAG, "exitGeofence tasks: " + listTask.getResult().size());
                for (ParseTask task : listTask.getResult()) {
                    task.getGeofenceStrategy().exitGeofence();
                    tasksCache.moveOutsideGeofence(task);
//                    Log.w(TAG, "exitGeofence: " + task.getClientName() + "  " + tasksCache.isMovedOutsideGeofence(task));
                }

                return null;
            }
        });
//        }
    }


    public void matchGeofencedWithDetectedActivity(DetectedActivity detectedActivity) {

//        Log.d(TAG, "matchGeofencedWithDetectedActivity: " + ActivityDetectionModule.getNameFromType(detectedActivity.getType()));

        Set<ParseTask> allWithinGeofence = tasksCache.getWithinGeofence();
        for (ParseTask task : allWithinGeofence) {
            task.getActivityStrategy().handleActivityInsideGeofence(detectedActivity);
        }

        Set<ParseTask> allOutsideGeofence = tasksCache.getOutsideGeofence();
//        Log.d(TAG, "matchGeofencedWithDetectedActivity outside geofence: " + allOutsideGeofence);
        for (ParseTask task : allOutsideGeofence) {
            task.getActivityStrategy().handleActivityOutsideGeofence(detectedActivity);
        }

    }

    public Task<Set<ParseTask>> queryAllGeofenceTasks(int withinKm, Location fromLocation) {

//        Log.d(TAG, "queryAllGeofenceTasks withinKm: " + withinKm);

        final TaskCompletionSource<Set<ParseTask>> promise = new TaskCompletionSource<>();

        final Set<ParseTask> geofenceResults = Sets.newConcurrentHashSet();

        ArrayList<Task<List<ParseTask>>> queryGeofenceTasks = new ArrayList<>();

        List<TaskGeofenceStrategy> geofenceStrategies = Lists.newArrayList(
                RegularGeofenceStrategy.getInstance(null),
                RaidGeofenceStrategy.getInstance(null),
                AlarmGeofenceStrategy.getInstance(null)
        );


        for (final TaskGeofenceStrategy geofenceStrategy : geofenceStrategies) {
            Task<List<ParseTask>> geofencedTasks = geofenceStrategy.queryGeofencedTasks(withinKm, fromLocation);
            geofencedTasks.onSuccess(new Continuation<List<ParseTask>, Object>() {
                @Override
                public Object then(Task<List<ParseTask>> listTask) throws Exception {
                    for (ParseTask taskObject : listTask.getResult()) {
                        geofenceResults.add(taskObject);
                    }
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    Exception e = task.getError();
                    if (e != null) {
                        new HandleException(TAG, "Failed to query geofence for: " + geofenceStrategy.getName(), e);
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
    private Task<List<ParseTask>> findTasksMatching(final String[] geofenceIds) {
        final TaskCompletionSource<List<ParseTask>> promise = new TaskCompletionSource<>();

        final List<ParseTask> geofenceResults = Lists.newArrayList();

        ArrayList<Task<List<ParseTask>>> tasks = new ArrayList<>();


        final ParseQuery<ParseTask> geofencedQueryNetwork = new TaskQueryBuilder(false).matchingObjectIds(geofenceIds).build();
        ParseQuery<ParseTask> geofencedQueryLDS = new ParseQuery<>(geofencedQueryNetwork).fromLocalDatastore();
        Task<List<ParseTask>> geofencedTask = geofencedQueryLDS.findInBackground();
        geofencedTask.continueWith(new Continuation<List<ParseTask>, Object>() {
            @Override
            public Object then(Task<List<ParseTask>> task) throws Exception {
                if (task.getError() != null) {
                    new HandleException(TAG, "findTasksMatching", task.getError());
//                        Log.e(TAG, "findTasksMatching failed for " + ParseTask.getParseClassName());
//                        Log.e(TAG, Arrays.toString(objectIdsArray));


                    if (task.getError() instanceof ParseException && ((ParseException) task.getError()).getCode() == ParseException.OBJECT_NOT_FOUND) {
                        // attempt to recover
                        // might be deprecated due to GuardSwiftApplication bootstrapParseObjectsLocally
//                            Log.w(TAG, "Updating failed objectIds in LDS");
                        new ParseTask().updateAll(geofencedQueryNetwork, 100).continueWith(new Continuation<List<ParseTask>, Object>() {
                            @Override
                            public Object then(Task<List<ParseTask>> task) throws Exception {
                                if (task.getError() != null) {
                                    new HandleException(TAG, "findTasksMatching recover", task.getError());
                                    return null;
                                }
//                                    Log.w(TAG, "Successfully updated LDS for " + ParseTask.getParseClassName() + " " + Arrays.toString(objectIdsArray));
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




}
