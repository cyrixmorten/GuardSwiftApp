package com.guardswift.core.ca;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by cyrix on 3/12/15.
 */
public class ActivityDetectionModule {

    public static class Recent {

        private static String TAG = "ActivityDetection.Recent";

        private static DetectedActivity detectedActivity;

        public static DetectedActivity getDetectedActivity() {
            if (detectedActivity == null) {
                detectedActivity = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
            }
            return detectedActivity;
        }

        public static void setDetectedActivity(DetectedActivity activity) {
            detectedActivity = activity;
        }

        public static int getDetectedActivityType() {
            return getDetectedActivity().getType();
        }

        public static int getDetectedActivityConfidence() {
            return getDetectedActivity().getConfidence();
        }
    }

    /**
     * Determine if an activity means that the user is moving.
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    public static boolean isMoving(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
                return false;
            default:
                return true;
        }
    }


    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    public static String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on bicycle";
            case DetectedActivity.ON_FOOT:
                return "on foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}
