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

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.util.AsyncExecutor;
import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

import static com.guardswift.util.rx.UnsubscribeIfPresent.dispose;


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

    private Disposable lastKnownLocationDisposable;
    private Disposable addGeofencesDisposable;
    private Disposable removeGeofencesDisposable;
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

        dispose(lastKnownLocationDisposable);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new HandleException(TAG, "Missing location permission", new IllegalStateException("Missing permission"));
            return;
        }

        lastKnownLocationDisposable = new ReactiveLocationProvider(getApplicationContext()).getLastKnownLocation().subscribe(location -> {
            AsyncExecutor.create().execute(() -> clearGeofence(e -> rebuildGeofenceForTasks(location)));
        });
    }

    private void rebuildGeofenceForTasks(final Location location) {

        Log.d(TAG, "rebuildGeofenceForTasks: " + location);

        mRebuildInProgress = true;

        int withinKm = 2;
        geofencingModule.queryAllGeofenceTasks(withinKm, location).onSuccess(taskObject -> {
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
                float radius = geofencedTask.getGeofenceStrategy().getGeofenceRadiusMeters();

                Geofence geofence = createGeofence(geofencedTask.getObjectId(), position, radius);
                geofences.add(geofence);

                geofencedTasks.add(geofencedTask);
            }

            addGeofences(geofences);

            tasksCache.setAllGeofencedTasks(geofencedTasks);


            return null;
        }).continueWithTask((Continuation<Object, Task<Void>>) task -> {
            if (task.isFaulted()) {
                new HandleException(getBaseContext(), TAG, "Failed to build geofences", task.getError());
            } else {
                EventBusController.postUIUpdate(location);
                mLastGeofenceRebuildLocation = location;
            }

            mRebuildInProgress = false;

            return null;
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

        dispose(lastKnownLocationDisposable);
        dispose(addGeofencesDisposable);
        dispose(removeGeofencesDisposable);

        super.onDestroy();
    }

    private void addGeofences(List<Geofence> geofences) {
        Log.i(TAG, "addGeofences " + geofences.size());

        if (geofences.isEmpty())
            return;

        ReactiveLocationProvider reactiveLocationProvider = new ReactiveLocationProvider(getApplicationContext());

        final GeofencingRequest geofencingRequest = new GeofencingRequest.Builder().addGeofences(geofences).build();
        addGeofencesDisposable = reactiveLocationProvider
                .removeGeofences(createRequestPendingIntent())
                .flatMap(status -> {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        throw new IllegalStateException("Missing permission");
                    }
                    return reactiveLocationProvider.addGeofences(createRequestPendingIntent(), geofencingRequest);
                }).subscribe(status -> {
                    Log.i(TAG, "AddGeofenceResult success!!");
                }, throwable -> {
                    new HandleException(getBaseContext(), TAG, "AddGeofenceResult subscription failed!", throwable);
                });

    }

    private void clearGeofence(final DeleteCallback callback) {
        removeGeofencesDisposable = new ReactiveLocationProvider(getApplicationContext()).removeGeofences(createRequestPendingIntent()).subscribe(status -> {
            if (callback != null) {
                callback.done(null);
            }
        }, throwable -> {
            if (callback != null) {
                callback.done(new ParseException(throwable));
            }

            new HandleException(getBaseContext(), TAG, "Error removing geofences", throwable);
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
