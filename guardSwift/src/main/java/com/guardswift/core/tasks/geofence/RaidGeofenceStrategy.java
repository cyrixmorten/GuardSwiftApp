package com.guardswift.core.tasks.geofence;

import android.location.Location;

import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;


public class RaidGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = RaidGeofenceStrategy.class.getSimpleName();


    public static TaskGeofenceStrategy getInstance(ParseTask task) {
        return new RaidGeofenceStrategy(task);
    }

    private RaidGeofenceStrategy(ParseTask task) {
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

//        int geofenceRadius = getGeofenceRadius();
//        double innerRadius = geofenceRadius*0.2;

        // First check - if update happened within the inner geofenceRadius
        if (B != null) {
            float LBC = ParseModule.distanceBetweenMeters(B, C);
            if (LBC <= task.getRadius()) {
                task.getAutomationStrategy().automaticArrival();
                return;
            }
        }

//        Log.d(TAG, "Near geofence: " + task.getTaskTitle(context) + " " + task.getClient().getName());

        // Second check - see if projection to line has distance less than geofenceRadius
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

            // segment intersects with geofence geofenceRadius
            if (LEC < task.getRadius()) {
//                if (
                        task.getAutomationStrategy().automaticArrival();
//                        && BuildConfig.DEBUG) {
//                    new EventLog.Builder(context).
//                    taskPointer(task, ParseTask.EVENT_TYPE.ARRIVE).
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
        return 300;
    }

    @Override
    public void queryGeofencedTasks(final int radiusKm, Location fromLocation, final FindCallback<ParseTask> callback) {
        if (fromLocation != null) {
            new RegularRaidTaskQueryBuilder(false)
                    .isRunToday()
                    .within(radiusKm, fromLocation)
                    .isRaid(true)
                    .build()
                    .findInBackground(callback);
        } else {
            callback.done(null, new ParseException(ParseException.OTHER_CAUSE, "Missing location for raid task"));
        }
    }

}
