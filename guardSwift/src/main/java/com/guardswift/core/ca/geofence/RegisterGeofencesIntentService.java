package com.guardswift.core.ca.geofence;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.common.collect.Lists;
import com.guardswift.core.ca.GeofencingModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingIntentService;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.TaskFactory;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.util.AsyncExecutor;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class RegisterGeofencesIntentService extends InjectingIntentService {

    // TODO InjectingIntentService to inject geofenceModule -> fix queryGeofenceTasks below

    @Inject
    GeofencingModule geofencingModule;
    @Inject
    GSTasksCache tasksCache;

    private static final String TAG = RegisterGeofencesIntentService.class.getSimpleName();

//    private static final float RADIUS_METERS = 50;

//    public static final String TASK_TYPE = "com.guardswift.services.TASK_TYPE";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
//    public static final String ADD = "com.guardswift.services.action.ADD";
//    public static final String REMOVE = "com.guardswift.services.action.REMOVE";


    private ReactiveLocationProvider mReactiveLocationProvider;
    private Subscription mAddGeofencesSubscription;
//    private Subscription mRemoveGeofencesSubscription;
    private PendingIntent mGeofencePendingIntent;

//    private static GSTask task_type;

    public static void start(Context context) {
        // relying on previously set task_type
        Log.d(TAG, "START");
        context.startService(new Intent(context, RegisterGeofencesIntentService.class));
    }

    public static void stop(Context context) {
        // relying on previously set task_type
        Log.d(TAG, "STOP");
        context.startService(new Intent(context, RegisterGeofencesIntentService.class).putExtra("clear", true));
    }


//    public static void start(Context context, GSTask geofencedTask) {
////        Log.d(TAG, "RegisterGeofencesIntentService start 2 ");
//        task_type = geofencedTask;
//        context.startService(new Intent(context, RegisterGeofencesIntentService.class));
//    }

//    public static void stop(Context context) {
//        context.stopService(new Intent(context, RegisterGeofencesIntentService.class));
//    }


    public RegisterGeofencesIntentService() {
        super("RegisterGeofencesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent); // Inject to ObjectGraph

        Log.d(TAG, "onHandleIntent");

        if (intent.hasExtra("clear")) {
            clear();
            return;
        }
        new ReactiveLocationProvider(getApplicationContext()).getLastKnownLocation().subscribe(new Action1<Location>() {
            @Override
            public void call(final Location deviceLocation) {
                AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
//                        validateTaskState(deviceLocation);
                        rebuildGeofenceForTasks();
                    }
                });
            }
        });

    }


//    /**
//     * If the device has been moved while guardswift has been shut down or
//     * the guard has been logged out, then it may happen that tasks are left 'hanging'
//     * in an arrived state
//     */
//    private void validateTaskState(final Location deviceLocation) {
//        List<BaseTask> tasks = new TaskFactory().getTasks();
//        for (final BaseTask task: tasks) {
//                task.getQueryBuilder(true).buildNoIncludes().findInBackground(new FindCallback<BaseTask>() {
//                    @Override
//                    public void done(List<BaseTask> tasks, ParseException e) {
//                        if (e != null) {
//                            new HandleException(TAG, "validateTaskState", e);
//                            return;
//                        }
//                        for (BaseTask task : tasks) {
//                            double distanceMeters = ParseModule.distanceBetweenMeters(deviceLocation, task.getPosition());
//                            int geofenceRadius = task.getGeofenceStrategy().getGeofenceRadius();
//                            switch (task.getTaskState()) {
//                                case ARRIVED:
//                                    // abort if outside radius
//                                    if (distanceMeters > geofenceRadius) {
//                                        task.getAutomationStrategy().automaticDeparture();
//                                        Log.w(TAG, "validateTaskState: ARRIVED -> Departure " + task.getTaskType() + " " + task.getClientName());
//                                    }
//                                    break;
//                            }
//                        }
//                    }
//
//                });
//        }
//    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void rebuildGeofenceForTasks() {

        Log.d(TAG, "rebuildGeofenceForTasks");

        int withinKm = 2;
        geofencingModule.queryAllGeofenceTasks(withinKm).onSuccess(new Continuation<Set<ParseObject>, Object>() {
            @Override
            public Object then(Task<Set<ParseObject>> taskObject) throws Exception {
                List<ParseObject> tasks = Lists.newCopyOnWriteArrayList(taskObject.getResult());

                Log.d(TAG, "All tasks in geofence: " + tasks.size());

                // weed away finished tasks
                for (ParseObject parseObjectTask: tasks) {
                    GSTask task = (GSTask)parseObjectTask;
                    if (task.isFinished()) {
                        tasks.remove(parseObjectTask);
                    }
                }

                Log.d(TAG, "Found tasks scheduled for geofencing: " + tasks.size());
                if (tasks.size() > 100) {
                    String message = "Geofence task size limit reached for user " + ParseUser.getCurrentUser().getUsername() + " at " + LocationModule.Recent.getLastKnownLocation().toString() + " with " + tasks.size() + " tasks";
                    new HandleException(getBaseContext(), TAG, "100+ geofences", new IllegalStateException(message));
                    Crashlytics.log(message);
                    tasks = tasks.subList(0, 100);
                }

                List<GSTask> geofencedTasks = Lists.newArrayList();
                List<Geofence> geofences = Lists.newArrayList();
                for (ParseObject geofencedTask : tasks) {
                    GSTask gsTask = (GSTask)geofencedTask;
                    ParseGeoPoint position = gsTask.getPosition();
                    float radius = gsTask.getGeofenceStrategy().getGeofenceRadius();
                    Geofence geofence = createGeofence(gsTask.getGeofenceId(), position, radius);
                    geofences.add(geofence);

                    geofencedTasks.add(gsTask);


                }

                addGeofences(geofences);

                tasksCache.setAllGeofencedTasks(geofencedTasks);


                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Object> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Query geofences", task.getError());
                }
                return null;
            }
        });



//        for (GSTask task: new TaskFactory().getTasks()) {
//            ParseTask<List<ParseObject>> getTasksWithinGeofence =  task.getGeofenceStrategy().queryGeofencedTasks(getApplicationContext(), new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> parseObjects, ParseException e) {
//                    if (e != null) {
//                        Log.e(TAG, "rebuildGeofenceForTasks", e);
//                        Crashlytics.logException(e);
//                        return;
//                    }
//
//                    List<GSTask> geofencedTasks = Lists.newArrayList();
//                    List<Geofence> geofences = Lists.newArrayList();
//                    for (ParseObject parseObject : parseObjects) {
//                        GSTask geofencedTask = (GSTask) parseObject;
//                        ParseGeoPoint clientPosition = geofencedTask.getPosition();
//                        float radius = geofencedTask.getGeofenceStrategy().getGeofenceRadius();
//                        Geofence geofence = createGeofence(parseObject.getObjectId(), clientPosition, radius);
//                        geofences.addUnique(geofence);
//
//                        geofencedTasks.addUnique(geofencedTask);
//
//                    }
//
//                    addGeofences(geofences);
//
//                    GeofencingModule.Recent.setAllGeofencedTasks(geofencedTasks);
//
//                }
//            });
//        }

    }

    private Geofence createGeofence(String id, ParseGeoPoint position, float radius) {
        Log.d(TAG, "createGeofence: " + id + " - " + position + " - " + radius);
        return new Geofence.Builder().
                setRequestId(id).
                setCircularRegion(position.getLatitude(), position.getLongitude(), radius).
                setExpirationDuration(Geofence.NEVER_EXPIRE).
                setNotificationResponsiveness(5).
                setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).
                build();
    }

    public void clear() {
        Log.d(TAG, "onDestroy");
//        if (mRemoveGeofencesSubscription != null && !mRemoveGeofencesSubscription.isUnsubscribed()) {
//            mRemoveGeofencesSubscription.unsubscribe();
//            mRemoveGeofencesSubscription = null;
//        }

        clearGeofence();

        if (mAddGeofencesSubscription != null && !mAddGeofencesSubscription.isUnsubscribed()) {
            mAddGeofencesSubscription.unsubscribe();
            mAddGeofencesSubscription = null;
        }

        if (mReactiveLocationProvider != null) {
            mReactiveLocationProvider = null;
        }
        super.onDestroy();
    }

    private void addGeofences(List<Geofence> geofences) {
        Log.i(TAG, "addGeofences " + geofences.size());

//        for (GeofencedTask task : GeofencingModule.Recent.getWithinGeofence()) {
//            task.exitGeofence(getApplicationContext());
//        }

        if (geofences.isEmpty())
            return;


        mReactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());

        final GeofencingRequest geofencingRequest = new GeofencingRequest.Builder().addGeofences(geofences).build();
        mAddGeofencesSubscription = mReactiveLocationProvider
                .removeGeofences(createRequestPendingIntent())
                .flatMap( new Func1<Status, Observable<Status>>() {
                    @Override
                    public Observable<Status> call(Status pendingIntentRemoveGeofenceResult) {
                        return mReactiveLocationProvider.addGeofences(createRequestPendingIntent(), geofencingRequest);
                    }
                }).subscribe(new Action1<Status>() {
                    @Override
                    public void call(Status addGeofenceResult) {
//                        EventBus.getDefault().postSticky(new GeofenceCompleteEvent());
                            Log.i(TAG, "AddGeofenceResult success!!");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "AddGeofenceResult subscription failed!");
                        Crashlytics.logException(throwable);
                    }
                });

    }

    private void clearGeofence() {
        mReactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());
        mReactiveLocationProvider.removeGeofences(createRequestPendingIntent()).subscribe(new Action1<Status>() {
            @Override
            public void call(Status pendingIntentRemoveGeofenceResult) {
                Log.d(TAG, "Geofences removed");
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, "Error removing geofences", throwable);
            }
        });
    }

    /**
     * Get a PendingIntent to send with the request to addUnique Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence
     * transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        } else {
            Intent intent = new Intent(getApplicationContext(), GeofenceBroadcastReceiver.class);
            mGeofencePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return mGeofencePendingIntent;
        }
    }

//    private void removeGeofences(final boolean explicit) {
//        Log.i(TAG, "removeGeofences");
//
//        mReactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());
//
//        Observable<RemoveGeofencesResult.PendingIntentRemoveGeofenceResult> requestPIntentRemoveGeofenceResultObservable = mReactiveLocationProvider.removeGeofences(createRequestPendingIntent());
//        mRemoveGeofencesSubscription = requestPIntentRemoveGeofenceResultObservable.doOnError(new Action1<Throwable>() {
//            @Override
//            public void call(Throwable throwable) {
//                Crashlytics.logException(throwable);
//                Log.e(TAG, "removeGeofences", throwable);
//            }
//        }).subscribe(new Action1<RemoveGeofencesResult.PendingIntentRemoveGeofenceResult>() {
//            @Override
//            public void call(RemoveGeofencesResult.PendingIntentRemoveGeofenceResult requestIdsRemoveGeofenceResult) {
//                if (requestIdsRemoveGeofenceResult.isSuccess()) {
//                    Log.i(TAG, "removeGeofences success!!");
//                } else {
//                    Log.e(TAG, "removeGeofences subscription failed with code " + requestIdsRemoveGeofenceResult.getStatusCode());
//                }
//            }
//        });
//    }

//    private void removeGeofences(List<String> requestIds) {
//        Log.i(TAG, "removeGeofences " + requestIds.size());
//
//        mReactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());
//
//        Observable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> requestIdsRemoveGeofenceResultObservable = mReactiveLocationProvider.removeGeofences(requestIds);
//        mRemoveGeofencesSubscription = requestIdsRemoveGeofenceResultObservable.doOnError(new Action1<Throwable>() {
//            @Override
//            public void call(Throwable throwable) {
//                Log.e(TAG, "removeGeofences", throwable);
//            }
//        }).subscribe(new Action1<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult>() {
//            @Override
//            public void call(RemoveGeofencesResult.RequestIdsRemoveGeofenceResult requestIdsRemoveGeofenceResult) {
//                if (requestIdsRemoveGeofenceResult.isSuccess()) {
//                    Log.i(TAG, "removeGeofences success!!");
//                } else {
//                    Log.e(TAG, "removeGeofences subscription failed with code " + requestIdsRemoveGeofenceResult.getStatusCode());
//                }
//                EventBus.getDefault().post(new GeofenceCompleteEvent(REMOVE));
//            }
//        });
//    }


}
