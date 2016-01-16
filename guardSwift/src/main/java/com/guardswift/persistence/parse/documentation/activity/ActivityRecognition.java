package com.guardswift.persistence.parse.documentation.activity;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

import org.json.JSONObject;

@ParseClassName("ChecklistCircuitEnding")
public class ActivityRecognition extends ExtendedParseObject {

	public static final String PIN = "ActivityRecognition";


	@Override
	public String getPin() {
		return PIN;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<ActivityRecognition> getAllNetworkQuery() {
		return new QueryBuilder(false).build();
	}

	@Override
	public void updateFromJSON(final Context context,
			final JSONObject jsonObject) {
		// TODO Auto-generated method stub
	}

	public static class QueryBuilder extends
            ParseQueryBuilder<ActivityRecognition> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(PIN, fromLocalDatastore, ParseQuery
					.getQuery(ActivityRecognition.class));
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
            case DetectedActivity.WALKING:
                return "walking";
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
            case DetectedActivity.RUNNING:
                return "running";
        }
        return "unknown";
    }

}
