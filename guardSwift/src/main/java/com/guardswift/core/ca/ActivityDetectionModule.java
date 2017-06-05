package com.guardswift.core.ca;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.R;

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

    public static String getHumanReadableNameFromType(Context context, int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return context.getString(R.string.activity_in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return context.getString(R.string.activity_on_bicycle);
            case DetectedActivity.ON_FOOT:
                return context.getString(R.string.activity_on_foot);
            case DetectedActivity.STILL:
                return context.getString(R.string.activity_still);
            case DetectedActivity.UNKNOWN:
                return context.getString(R.string.activity_unknown);
            case DetectedActivity.TILTING:
                return context.getString(R.string.activity_tilting);
        }
        return context.getString(R.string.activity_unknown);
    }

    public static int getColorFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return Color.HSVToColor(new float[]{0, 100, 100}); // RED
            case DetectedActivity.ON_BICYCLE:
                return Color.HSVToColor(new float[]{27, 100, 100}); // ORANGE
            case DetectedActivity.ON_FOOT:
                return Color.HSVToColor(new float[]{215, 100, 100}); // BLUE
            case DetectedActivity.STILL:
                return Color.HSVToColor(new float[]{0, 0, 0}); // BLACK
            case DetectedActivity.TILTING:
                return Color.HSVToColor(new float[]{300, 100, 100}); // PURPLE
        }
        return Color.HSVToColor(new float[]{0, 0, 0}); // BLACK;
    }
}
