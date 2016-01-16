package com.guardswift.core.documentation.eventlog.context;

import android.location.Location;

import com.guardswift.core.ca.LocationModule;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogCurrentLocationStrategy implements LogContextStrategy {

    public static final String provider = "provider";
    public static final String position = "position";
    public static final String accuracy = "accuracy";
    public static final String altitude = "altitude";
    public static final String bearing = "bearing";
    public static final String speed = "speed";

    @Override
    public void log(ParseObject toParseObject) {

        Location location = LocationModule.Recent.getLastKnownLocation();

        if (location != null) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(
                    location.getLatitude(), location.getLongitude());

            toParseObject.put(LogCurrentLocationStrategy.position, parseGeoPoint);

            toParseObject.put(LogCurrentLocationStrategy.accuracy, location.getAccuracy());
            toParseObject.put(LogCurrentLocationStrategy.altitude, location.getAltitude());
            toParseObject.put(LogCurrentLocationStrategy.bearing, location.getBearing());
            toParseObject.put(LogCurrentLocationStrategy.provider, location.getProvider());
            toParseObject.put(LogCurrentLocationStrategy.speed, location.getSpeed());
        }
    }
}
