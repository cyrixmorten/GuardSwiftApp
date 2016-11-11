package com.guardswift.core.tasks.geofence;

import android.content.Context;
import android.location.Location;

import com.crashlytics.android.Crashlytics;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by cyrix on 6/7/15.
 */
public class DistrictWatchGeofenceStrategy<T extends BaseTask> extends BaseGeofenceStrategy<T> {


    private static final String TAG = DistrictWatchGeofenceStrategy.class.getSimpleName();


    public DistrictWatchGeofenceStrategy(GSTask task) {
        super(task);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void withinGeofence() {
        super.withinGeofence();

        if (task.getAutomationStrategy() == null)
            return;

        Location A = LocationModule.Recent.getPreviousKnownLocation();
        Location B = LocationModule.Recent.getLastKnownLocation();


        ParseGeoPoint C = task.getPosition();

        int radius = getGeofenceRadius();
        double innerRadius = radius*0.2;

        // First check - if update happened within the inner radius
        if (B != null) {
            float LBC = ParseModule.distanceBetweenMeters(B, C);
            if (LBC <= innerRadius) {
                task.getAutomationStrategy().automaticArrival();
                return;
            }
        }

//        Log.d(TAG, "Near geofence: " + task.getTaskTitle(context) + " " + task.getClient().getName());

        // Second check - see if projection to line has distance less than radius
        if (A != null && B != null) {
            double Ax,Ay,Bx,By,Cx,Cy;

            Ax = A.getLatitude();
            Ay = A.getLongitude();

            Bx = B.getLatitude();
            By = B.getLongitude();

            Cx = C.getLatitude();
            Cy = C.getLongitude();

            // compute the euclidean distance between A and B
            float LAB = A.distanceTo(B);

            // compute the direction vector D from A to B
            double Dx = (Bx-Ax)/LAB;
            double Dy = (By-Ay)/LAB;

            // Now the line equation is x = Dx*t + Ax, y = Dy*t + Ay with 0 <= t <= 1.


            // compute the value t of the closest point to the circle center (Cx, Cy)
//            double t = Dx*(Cx-Ax) + Dy*(Cy-Ay);


            // compute the coordinates of the point E on line and closest to C
//            double Ex = t*Dx+Ax;
//            double Ey = t*Dy+Ay;


//            Location E = new Location("");
//            E.setLatitude(Ex);
//            E.setLongitude(Ey);

            // compute the euclidean distance from E to C
//            double LEC = ParseModule.distanceBetweenMeters(E, C); // convert to meters

            Float[] tcandidates = new Float[]{LAB/2, LAB/2-(LAB/4), LAB/2+(LAB/4)};
            double LEC = Double.MAX_VALUE;
            for (Float t: tcandidates) {
                double Ex = t*Dx+Ax;
                double Ey = t*Dy+Ay;

                Location E = new Location("");
                E.setLatitude(Ex);
                E.setLongitude(Ey);
                float dist = ParseModule.distanceBetweenMeters(E, C);
                if (dist < LEC) {
                    LEC = dist;
                }
            }

            // segment intersects with geofence radius
            if (LEC < innerRadius) {
//                if (
                        task.getAutomationStrategy().automaticArrival();
//                        && BuildConfig.DEBUG) {
//                    new EventLog.Builder(context).
//                    taskPointer(task, GSTask.EVENT_TYPE.ARRIVE).
//                    event(context.getString(R.string.event_arriving)).
//                    remarks("LEC " + LEC).
//                    eventCode(EventLog.EventCodes.AUTOMATIC_ARRIVED).
//                    automatic(true).
//                    saveAsync();
//                }
            }

//            Log.e(TAG, "LEC: " + LEC + "  " + task.getTaskTitle(context) + " innerRadius: " + innerRadius);


        }
    }

    @Override
    public void enterGeofence() {
        super.enterGeofence();

        // do nothing, rely on withinGeofence

//        if (automationStrategy != null) {
//            automationStrategy.automaticArrival(context);
//        }
    }

    @Override
    public void exitGeofence() {
        super.exitGeofence();

        if (task.getAutomationStrategy() != null) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }

    @Override
    public int getGeofenceRadius() {
        return 200;
    }

    @Override
    public void queryGeofencedTasks(final int radiusKm, final FindCallback<ParseObject> callback) {
        if (LocationModule.Recent.getLastKnownLocation() != null) {
            geofenceQuery(radiusKm, LocationModule.Recent.getLastKnownLocation()).findInBackground(callback);
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
                        geofenceQuery(radiusKm, location).findInBackground(callback);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        new HandleException(context, TAG, "getLastKnownLocation",  throwable);
                    }
                });
    }

    private ParseQuery<ParseObject> geofenceQuery(int withinRadiusKm, Location fromLocation) {
        return new DistrictWatchClient.QueryBuilder(true)
                .isRunToday()
                .within(withinRadiusKm, fromLocation)
                .buildAsParseObject();//.setLimit(100);
//                .whereNear(DistrictWatchClient.clientPosition, ParseModule.geoPointFromLocation(fromLocation));
    }
}
