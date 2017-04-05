package com.guardswift.core.tasks.geofence;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;
import rx.functions.Func1;

public class AlarmGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = AlarmGeofenceStrategy.class.getSimpleName();

    public static TaskGeofenceStrategy getInstance(GSTask task) {
        return new AlarmGeofenceStrategy(task);
    }

    private AlarmGeofenceStrategy(GSTask task) {
        super(task);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public int getGeofenceRadius() {
        return 300;
    }

    @Override
    public void withinGeofence() {
        super.withinGeofence();

        float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getClient().getPosition());
        if (distanceToClient < task.getRadius()) {
            task.getAutomationStrategy().automaticArrival();
        }
    }

    @Override
    public void exitGeofence() {
        super.exitGeofence();

        if (!task.isArrived()) {
            return;
        }

        if (task.getAutomationStrategy() != null) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }

    @Override
    public void queryGeofencedTasks(final int withinKm, final FindCallback<ParseObject> callback) {

        Log.d(TAG, "queryGeofencedTasks");

        if (LocationModule.Recent.getLastKnownLocation() != null) {
            geofenceQuery(withinKm, LocationModule.Recent.getLastKnownLocation()).findInBackground(callback);
            return;
        }

        final Context context = GuardSwiftApplication.getInstance();

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        locationProvider.getLastKnownLocation()
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Crashlytics.logException(throwable);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Location>() {
                    @Override
                    public Location call(Throwable throwable) {
                        return LocationModule.Recent.getLastKnownLocation();
                    }
                })
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        if (location == null) {
                            callback.done(new ArrayList<ParseObject>(), null);
                            return;
                        }
                        geofenceQuery(withinKm, location)
                                .findInBackground(callback);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        new HandleException(context, TAG, "getLastKnownLocation", throwable);
                    }
                });
    }

    private ParseQuery<ParseObject> geofenceQuery(int withinKm, Location fromLocation) {
        return new ParseTask().getQueryBuilder(true)
                .whereStatus(ParseTask.STATUS.PENDING, ParseTask.STATUS.ACCEPTED)
                .within(withinKm, fromLocation)
                .buildAsParseObject();
    }
}
