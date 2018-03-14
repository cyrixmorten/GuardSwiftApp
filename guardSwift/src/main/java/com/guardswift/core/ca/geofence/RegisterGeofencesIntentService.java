package com.guardswift.core.ca.geofence;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.common.collect.Lists;
import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingIntentService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.DeleteCallback;
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

    @Inject
    GeofencingModule geofencingModule;
    @Inject
    ParseTasksCache tasksCache;

    private static final String TAG = RegisterGeofencesIntentService.class.getSimpleName();


    private static Location mLastGeofenceRebuildLocation;
    private static boolean mRebuildInProgress;

    private ReactiveLocationProvider mReactiveLocationProvider;
    private Subscription mAddGeofencesSubscription;
    private PendingIntent mGeofencePendingIntent;



    public static void start(Context context, boolean force) {
        // relying on previously set task_type
        Log.d(TAG, "START");


        if (force) {
            RegisterGeofencesIntentService.mRebuildInProgress = false;
        }

        if (RegisterGeofencesIntentService.isRebuildingGeofence()) {
            return;
        }

        context.startService(new Intent(context, RegisterGeofencesIntentService.class));
    }

    public static void stop(Context context) {
        // relying on previously set task_type
        Log.d(TAG, "STOP");
        context.startService(new Intent(context, RegisterGeofencesIntentService.class).putExtra("clear", true));
    }


    public RegisterGeofencesIntentService() {
        super("RegisterGeofencesIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent); // Inject to ObjectGraph


        if (intent.hasExtra("clear")) {
            clear();
            return;
        }

        rebuildGeofenceForTasks();
    }

    public static Location getLastRebuildLocation() {
        return mLastGeofenceRebuildLocation;
    }

    public static boolean isRebuildingGeofence() {
        return mRebuildInProgress;
    }


    private void rebuildGeofenceForTasks() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new HandleException(TAG, "Missing location permission", new IllegalStateException("Missing permission"));
            return;
        }

        new ReactiveLocationProvider(getApplicationContext()).getLastKnownLocation().subscribe(new Action1<Location>() {
            @Override
            public void call(final Location deviceLocation) {
                AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        clearGeofence(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                rebuildGeofenceForTasks(deviceLocation);
                            }
                        });
                    }
                });
            }
        });
    }

    private void rebuildGeofenceForTasks(final Location location) {

        Log.d(TAG, "rebuildGeofenceForTasks: " + location);

        mRebuildInProgress = true;

        int withinKm = 2;
        geofencingModule.queryAllGeofenceTasks(withinKm, location).onSuccess(new Continuation<Set<ParseTask>, Object>() {
            @Override
            public Object then(Task<Set<ParseTask>> taskObject) throws Exception {
                List<ParseTask> tasks = Lists.newCopyOnWriteArrayList(taskObject.getResult());

                Log.d(TAG, "All tasks in geofence: " + tasks.size());

                // weed away finished tasks
                for (ParseObject parseObjectTask : tasks) {
                    ParseTask task = (ParseTask) parseObjectTask;
                    if (task.isFinished()) {
                        tasks.remove(parseObjectTask);
                    }
                }

                Log.d(TAG, "Found tasks scheduled for geofencing: " + tasks.size());
                if (tasks.size() > 100) {
                    String message = "Geofence task size limit reached for user " + ParseUser.getCurrentUser().getUsername() + " at " + LocationModule.Recent.getLastKnownLocation().toString() + " with " + tasks.size() + " tasks";
                    new HandleException(getBaseContext(), TAG, "100+ geofences", new IllegalStateException(message));
                    Crashlytics.log(message);
                    tasks = tasks.subList(0, 99);
                }

                List<ParseTask> geofencedTasks = Lists.newArrayList();
                List<Geofence> geofences = Lists.newArrayList();
                for (ParseTask geofencedTask : tasks) {
                    ParseGeoPoint position = geofencedTask.getPosition();
                    float radius = geofencedTask.getGeofenceStrategy().getGeofenceRadius();

                    Geofence geofence = createGeofence(geofencedTask.getObjectId(), position, radius);
                    geofences.add(geofence);

                    geofencedTasks.add(geofencedTask);
                }

                addGeofences(geofences);

                tasksCache.setAllGeofencedTasks(geofencedTasks);


                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Object> task) throws Exception {
                if (task.isFaulted()) {
                    new HandleException(getBaseContext(), TAG, "Failed to build geofences", task.getError());
                } else {
                    EventBusController.postUIUpdate(location);
                    mLastGeofenceRebuildLocation = location;
                }

                mRebuildInProgress = false;

                return null;
            }
        });


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

        clearGeofence(null);

        geofencingModule.clearPinned();

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

        if (geofences.isEmpty())
            return;

        mReactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());

        final GeofencingRequest geofencingRequest = new GeofencingRequest.Builder().addGeofences(geofences).build();
        mAddGeofencesSubscription = mReactiveLocationProvider
                .removeGeofences(createRequestPendingIntent())
                .flatMap(new Func1<Status, Observable<Status>>() {
                    @Override
                    public Observable<Status> call(Status pendingIntentRemoveGeofenceResult) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            throw new IllegalStateException("Missing permission");
                        }
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
                        new HandleException(getBaseContext(), TAG, "AddGeofenceResult subscription failed!", throwable);
                    }
                });

    }

    private void clearGeofence(final DeleteCallback callback) {
        mReactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());
        mReactiveLocationProvider.removeGeofences(createRequestPendingIntent()).subscribe(new Action1<Status>() {
            @Override
            public void call(Status pendingIntentRemoveGeofenceResult) {
                if (callback != null) {
                    callback.done(null);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (callback != null) {
                    callback.done(new ParseException(throwable));
                }

                new HandleException(getBaseContext(), TAG, "Error removing geofences", throwable);
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


}
