package com.guardswift.persistence.parse.documentation.gps;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.util.FileIO;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import bolts.Task;

@ParseClassName("LocationTracker")
public class LocationTracker extends ExtendedParseObject {

    public static final String PIN = "LocationTracker";

    public static final String installation = "installation";
    public static final String circuit = "circuit";
    public static final String guard = "guard";
    public static final String sampleStart = "sampleStart";
    public static final String sampleEnd = "sampleEnd";
    public static final String minutesSampled = "minutesSampled";
    public static final String position = "position";
    public static final String clientTimestamp = "clientTimestamp";

    public static final String gpsData = "gpsData";
    public static final String gpsFile = "gpsFile";

    // Holds in-memory locations until either persisted as local file or uploaded
//    private JSONArray locations;

    public LocationTracker() {

//        locations = new JSONArray();
    }

    @Override
    public String getPin() {
        return PIN;
    }


//    public static GPSTracker create(ParseGeoPoint location, JSONArray locations, Circuit circuit, Guard guard, long minutesSampled) {
//
//
////        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
//
//        GPSTracker gpsTracker = new GPSTracker();
//        gpsTracker.put(GPSTracker.position, location);
//        gpsTracker.put(GPSTracker.gpsFile, locations);
//        gpsTracker.put(GPSTracker.gpsData, locations);
//        if (circuit != null) gpsTracker.put(GPSTracker.circuit, circuit);
//        if (guard != null) gpsTracker.put(GPSTracker.guard, guard);
//        gpsTracker.put(GPSTracker.minutesSampled, minutesSampled);
//
//        DateTime sampleStart = new DateTime().minusMinutes((int) minutesSampled);
//        gpsTracker.put(GPSTracker.sampleStart, sampleStart.toDate());
//        gpsTracker.put(GPSTracker.sampleEnd, new Date());
//        gpsTracker.put(GPSTracker.clientTimestamp, new Date());
//
////        gpsTracker.put(GPSTracker.installation, installation);
//        gpsTracker.put(owner, ParseUser.getCurrentUser());
//
//        return gpsTracker;
//    };

    private static LocationTracker create(final Guard guard) {

        final LocationTracker locationTracker = new LocationTracker();
        locationTracker.put(LocationTracker.sampleStart, guard.getLastLogin());
        locationTracker.put(LocationTracker.sampleEnd, new Date());
        locationTracker.put(LocationTracker.guard, guard);
        locationTracker.put(owner, ParseUser.getCurrentUser());
//        locationTracker.pinThenSaveEventually(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                callback.done(locationTracker, e);
//            }
//        });

        return locationTracker;

    }

//    private static void get(Guard guard, GetCallback<LocationTracker> callback) {
//
//        Log.d(PIN, "get existing");
//
//        new LocationTracker.QueryBuilder(true).matching(guard).build().getFirstInBackground(callback);
//    }
//
//	public static void newInstance(final Guard guard, final GetCallback<LocationTracker> callback) {
//
//
//		if (LocationTracker.getLocalFileForCurrentGuard(guard).exists()) {
//			// already exists
//			LocationTracker.get(guard, new GetCallback<LocationTracker>() {
//                @Override
//                public void done(LocationTracker object, ParseException e) {
//                    if (object != null) {
//                        callback.done(object, e);
//                    } else {
//                        create(guard, callback);
//                    }
//                }
//            });
//		} else {
//			// create new
//            create(guard, callback);
//		}
//	};

    /**
     * File contains a JSONArray of GPS locations and auxiliary information tied to guard
     *
     * @return
     */
    public static File getLocalFileForCurrentGuard(Guard guard) {
        return new File(LocationTracker.getLocalFileNameForCurrentGuard(guard));
    }

    public static String getLocalFileNameForCurrentGuard(Guard guard) throws IllegalStateException {
        if (!guard.isDataAvailable()) {
            return "";
        }
        return PIN + "." + guard.getGuardId() + guard.getName();
    }

    public static Task<String> uploadForGuard(final Context context, final Guard guard, final ProgressCallback progressCallback) {

        LocationTracker locationTracker = LocationTracker.create(guard);
        return locationTracker.upload(context, guard, progressCallback);

    }
//    public void restoreLocationsFromFile(Context context) throws JSONException {
//        Log.d(PIN, "restoreLocationsFromFile");
//        String fileName = getLocalFileNameForCurrentGuard(getGuard());
//        String locationsStr = FileIO.readFromFile(context, fileName);
//        locations = new JSONArray(locationsStr);
//        Log.d(PIN, "restoreLocationsFromFile restored " + locations.length());
//    }

//    private JSONArray concatArray(JSONArray... arrs)
//            throws JSONException {
//        JSONArray result = new JSONArray();
//        for (JSONArray arr : arrs) {
//            for (int i = 0; i < arr.length(); i++) {
//                result.put(arr.get(i));
//            }
//        }
//        return result;
//    }

//    public void appendLocationsToFile(Context context, Guard guard) {
//        String fileName = getLocalFileNameForCurrentGuard(guard);
//        try {
//            Log.d(TAG, "appendLocationsToFile: " + fileName);
//            Log.d(TAG, Arrays.toString(context.fileList()));
//            if (Arrays.asList(context.fileList()).contains(fileName)) {
//                Log.d(PIN, "Has previous locations file named: " + fileName);
//                String prevLocStr = FileIO.readFromFile(context, fileName);
//                if (!prevLocStr.isEmpty()) {
//                    try {
//                        JSONArray prevLoc = new JSONArray(prevLocStr);
//                        locations = concatArray(prevLoc, locations);
//                    } catch (JSONException e) {
//                        new HandleException(context, PIN, "Get previous GPSTracker locations from file", e);
//                    }
//                }
//            } else {
//                Log.d(PIN, "No locations file found named: " + fileName);
//            }
//            FileIO.writeToFile(context, fileName, Context.MODE_PRIVATE, locations.toString());
//            locations = new JSONArray();
//        } catch (IOException e) {
//            new HandleException(context, TAG, "appendLocationsToFile", e);
//        }
//    }

    public Task<String> upload(final Context context, final Guard guard, ProgressCallback progressCallback) {

        final Task<String>.TaskCompletionSource taskResult = Task.create();

        Log.d(PIN, "upload");

        try {
            String string = FileIO.readFromFile(context, LocationTracker.getLocalFileNameForCurrentGuard(guard));
            String jsonObjectsLocationString = (string.contains("{")) ? string.substring(string.indexOf("{"), string.lastIndexOf("}") + 1) : "";
            String jsonArrayLocationString = "[" + jsonObjectsLocationString + "]";

            if (string.isEmpty()) {
                taskResult.setError(new IOException("Empty file"));
            }
            else {
                final ParseFile file = new ParseFile("gps.json", jsonArrayLocationString.getBytes());
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            new HandleException(context, PIN, "upload gpsFile", e);
                            taskResult.setError(e);
                        } else {
                            Log.d(PIN, "Associate gps file and submit GPSTracker");

                            put(LocationTracker.gpsFile, file);

                            saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d(PIN, "Save GPSTracker callback " + e);
                                    if (e != null) {
                                        new HandleException(context, PIN, "saveAsync GPSTracker", e);
                                    }

                                    boolean successfullyDeleted = getLocalFileForCurrentGuard(guard).delete();
                                    Log.d(PIN, "Delete local gpsFile - success: " + successfullyDeleted);

                                }
                            });

                            taskResult.setResult(file.getUrl());
                        }
                    }
                }, progressCallback);
            }
        } catch (IllegalStateException e) {
            Log.e(PIN, "Get local file name", e);
            taskResult.setError(e);
        } catch (IOException e) {
            Log.e(PIN, "Read local file", e);
            e.printStackTrace();
            taskResult.setError(e);
        }

        return taskResult.getTask();

    }

//    public Guard getGuard() {
//        return (Guard) getParseObject(guard);
//    }


    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public void appendLocation(Context context, Guard guard, @NonNull Location location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (location.isFromMockProvider()) {
                return;
            }
        }

        JSONObject jsonLocation = LocationModule.locationToJSONObject(location);
        try {
            FileIO.writeToFile(context, getLocalFileNameForCurrentGuard(guard), Context.MODE_APPEND, jsonLocation.toString());
            FileIO.writeToFile(context, getLocalFileNameForCurrentGuard(guard), Context.MODE_APPEND, ",");
        } catch (IOException e) {
            new HandleException(context, TAG, "appendLocation to file", e);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<LocationTracker> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }


    public static class QueryBuilder extends ParseQueryBuilder<LocationTracker> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(PIN, fromLocalDatastore, ParseQuery.getQuery(LocationTracker.class));
        }

        @Override
        public ParseQuery<LocationTracker> build() {
            return super.build();
        }

        public QueryBuilder matching(Guard guard) {
            query.whereEqualTo(LocationTracker.guard, guard);
            return this;
        }
    }


}
