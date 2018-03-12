package com.guardswift.persistence.parse.documentation.gps;

import android.content.Context;
import android.location.Location;
import android.text.format.DateFormat;

import com.google.gson.Gson;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Util;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ParseClassName("TrackerData")
public class TrackerData extends ExtendedParseObject {

    private static String TAG = TrackerData.class.getSimpleName();

    public static TrackerData create(Location location, Guard guard) {
        JSONObject json = locationToJSONObject(location);
        TrackerData td = new TrackerData();

        try {

            td.put(TrackerData._activityType, json.getInt(TrackerData._activityType));
            td.put(TrackerData._activityConfidence, json.getInt(TrackerData._activityConfidence));
            td.put(TrackerData._provider, json.getString(TrackerData._provider));
            td.put(TrackerData._speed, json.getLong(TrackerData._speed));
            td.put(TrackerData._accuracy, json.getInt(TrackerData._accuracy));
            td.put(TrackerData._bearing, json.getLong(TrackerData._bearing));
            td.put(TrackerData._altitude, json.getDouble(TrackerData._altitude));
            td.put(TrackerData._time, new Date(json.getLong(TrackerData._time)));

            td.put(TrackerData._clientTimeStamp, new Date());
            td.put(TrackerData._position, ParseModule.geoPointFromLocation(location));
            td.put(TrackerData._installation, com.parse.ParseInstallation.getCurrentInstallation());

            if (guard != null) {
                td.put(TrackerData._guard, guard);
            }

            TaskGroupStarted selectedTaskGroup = GuardSwiftApplication.getInstance().getCacheFactory().getTaskGroupStartedCache().getSelected();

            if (selectedTaskGroup != null) {
                td.put(TrackerData._taskGroupStarted, selectedTaskGroup);
                td.put(TrackerData._taskGroup, selectedTaskGroup.getTaskGroup());
            }

            td.put(TrackerData.owner, ParseUser.getCurrentUser());

            return td;

        } catch (JSONException e) {
            new HandleException(TAG, "Upload", e);
        }

        return null;
    }

    public static JSONObject locationToJSONObject(Location location) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(TrackerData._activityType, ActivityDetectionModule.Recent.getDetectedActivityType());
        map.put(TrackerData._activityConfidence, ActivityDetectionModule.Recent.getDetectedActivityConfidence());
        map.put(TrackerData._provider, location.getProvider());
        map.put(TrackerData._latitude, location.getLatitude());
        map.put(TrackerData._longitude, location.getLongitude());
        map.put(TrackerData._speed, location.getSpeed());
        map.put(TrackerData._accuracy, location.getAccuracy());
        map.put(TrackerData._bearing, location.getBearing());
        map.put(TrackerData._altitude, location.getAltitude());
        map.put(TrackerData._time, location.getTime());
        return new JSONObject(map);
    }

    public static TrackerData locationJSONtoTrackerData(String locationJSON) {
        return new Gson().fromJson(locationJSON, TrackerData.class);
    }

    public static TrackerData[] locationJSONArraytoTrackerData(String locationJSONArray) {
        return new Gson().fromJson(locationJSONArray, TrackerData[].class);
    }

    public static final String _time = "time";
    public static final String _bearing = "bearing";
    public static final String _altitude = "altitude";
    public static final String _accuracy = "accuracy";
    public static final String _speed = "speed";
    public static final String _activityType = "activityType";
    public static final String _activityConfidence = "activityConfidence";
    public static final String _provider = "provider";

    public static final String _latitude = "latitude";
    public static final String _longitude = "longitude";

    public static final String _guard = "guard";
    public static final String _position = "position";
    public static final String _clientTimeStamp = "clientTimeStamp";
    public static final String _installation = "installation";
    public static final String _taskGroup = "taskGroup";
    public static final String _taskGroupStarted = "taskGroupStarted";

    private long time;

    private float bearing;
    private double altitude;

    private float accuracy;
    private float speed;

    private double longitude;
    private double latitude;

    private int activityType;
    private int activityConfidence;

    private String provider;

    private Guard guard;

    public int getActivityConfidence() {
        return activityConfidence;
    }

    public long getTime() {
        return time;
    }

    public float getBearing() {
        return bearing;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float getSpeed() {
        return speed;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getActivityType() {
        return activityType;
    }

    @Override
    public String toString() {
        return "TrackerData{" +
                "time=" + time +
                ", bearing=" + bearing +
                ", altitude=" + altitude +
                ", accuracy=" + accuracy +
                ", speed=" + speed +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", activityType=" + activityType +
                ", activityConfidence=" + activityConfidence +
                ", provider='" + provider + '\'' +
                '}';
    }

    public int getSpeedKmH() {
        return Math.round(getSpeed() * 3.6f);
    }

    public String getHumanReadableLongDate(Context context) {
        return DateFormat.getLongDateFormat(context).format(getTime()) + " " + Util.dateFormatHourMinutes().format(new Date(getTime()));
    }

    public String getHumanReadableSpeed(Context context) {
        return getSpeedKmH() + " " + context.getString(R.string.km_h);
    }

    public String getHumanReadableAltitude(Context context) {
        return String.valueOf(getLatitude()) + " " + context.getString(R.string.meters);
    }


    @Override
    public <T extends ParseObject> ParseQuery<T> getAllNetworkQuery() {
        return null;
    }

}
