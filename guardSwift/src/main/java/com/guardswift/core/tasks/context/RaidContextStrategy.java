package com.guardswift.core.tasks.context;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseGeoPoint;

import java.util.Queue;


public class RaidContextStrategy extends BaseContextStrategy {


    private static final String TAG = RaidContextStrategy.class.getSimpleName();


    public static ContextUpdateStrategy getInstance(ParseTask task) {
        return new RaidContextStrategy(task);
    }

    private RaidContextStrategy(ParseTask task) {
        super(task);
    }

    @Override
    boolean pendingTaskUpdate(Location A, Location B, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters) {

        ParseGeoPoint C = task.getPosition();

        // First check - if update happened within the inner geofenceRadius
        if (B != null) {
            float LBC = ParseModule.distanceBetweenMeters(B, C);
            if (LBC <= task.getRadius()) {
                return triggerArrival();
            }
        }

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
            double t = Dx*(Cx-Ax) + Dy*(Cy-Ay);


            // compute the coordinates of the point E on line and closest to C
            double Nx = t*Dx+Ax;
            double Ny = t*Dy+Ay;


            Location N = new Location("");
            N.setLatitude(Nx);
            N.setLongitude(Ny);

            // compute the euclidean distance from E to C
            double NLEC = ParseModule.distanceBetweenMeters(N, C); // convert to meters

            if (NLEC <= task.getRadius()) {
                return triggerArrival();
            }

            Float[] tcandidates = new Float[]{LAB/2, LAB/2-(LAB/4), LAB/2+(LAB/4)};
            double LEC = Double.MAX_VALUE;
            for (Float tC: tcandidates) {
                double Ex = tC*Dx+Ax;
                double Ey = tC*Dy+Ay;

                Location E = new Location("");
                E.setLatitude(Ex);
                E.setLongitude(Ey);
                float dist = ParseModule.distanceBetweenMeters(E, C);
                if (dist < LEC) {
                    LEC = dist;
                }
            }

            // segment intersects with geofence geofenceRadius
            if (LEC <= task.getRadius()) {
                return triggerArrival();
            }
        }

        return false;
    }

    private boolean triggerArrival() {
        boolean triggerArrival = task.isWithinScheduledTimeRelaxed() && task.matchesSelectedTaskGroupStarted();

        if (triggerArrival) {
            controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
        }

        return triggerArrival;
    }

    @Override
    boolean arrivedTaskUpdate(Location current, Location previous, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters) {

        boolean isWellOutsideRadius = distanceToClientMeters > (task.getRadius() * 4);

        if (isWellOutsideRadius) {
            task.getController().performAutomaticAction(TaskController.ACTION.PENDING, task);
        }

        return isWellOutsideRadius;
    }


}
