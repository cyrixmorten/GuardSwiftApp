package com.guardswift.persistence.parse.documentation.gps;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.CloudFunctions;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.query.TrackerQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.FileIO;
import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("Tracker")
public class Tracker extends ExtendedParseObject {

    private static String TAG = Tracker.class.getSimpleName();

    public static final String guard = "guard";
    public static final String installation = "installation";
    public static final String sampleStart = "start";
    public static final String sampleEnd = "end";
    public static final String sampleMinutes = "minutes";
    public static final String clientTimestamp = "clientTimestamp";
    public static final String inProgress = "inProgress";

    private static final String gpsFile = "gpsFile";


    private static final String LOCAL_GPS_FILE_NAME = "gps_track.json";


    public Tracker() {
    }




    public static Task<Void> upload(final Context context, final ProgressCallback progressCallback, final boolean partialUpload) {

        final Guard guard = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().getLoggedIn();

        Log.d(TAG, "Tracker create: " + guard.getName());

        return new TrackerQueryBuilder(false).matching(guard).inProgress(true).build().getFirstInBackground().continueWithTask(new Continuation<Tracker, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Tracker> task) throws Exception {

                Tracker tracker = task.getResult();

                DateTime started = new DateTime(guard.getLastLogin());
                DateTime ended = DateTime.now();

                if (task.isFaulted()) {
                    Exception e = task.getError();

                    if (e instanceof ParseException && ((ParseException) e).getCode() == ParseException.OBJECT_NOT_FOUND) {
                        Log.d(TAG, "Creating new Tracker");
                        tracker = new Tracker();

                        tracker.put(Tracker.sampleStart, started.toDate());
                        tracker.put(Tracker.guard, ParseObject.createWithoutData(Guard.class, guard.getObjectId()));
                        tracker.put(Tracker.installation, ParseInstallation.getCurrentInstallation());
                        tracker.put(ExtendedParseObject.owner, ParseUser.getCurrentUser());
                    } else {
                        new HandleException(TAG, "Error finding existing Tracker", e);

                        throw e;
                    }
                }

                tracker.put(Tracker.sampleEnd, ended.toDate());
                tracker.put(Tracker.sampleMinutes, Minutes.minutesBetween(started, ended).getMinutes());
                tracker.put(Tracker.clientTimestamp, ended.toDate());

                return tracker.uploadData(context, progressCallback, partialUpload);
            }
        });

    }


    private String readGPSFileAsJSONArrayString(Context context) {
        String string = FileIO.readFromFile(context, LOCAL_GPS_FILE_NAME);

        String jsonObjectsLocationString = (string.contains("{")) ? string.substring(string.indexOf("{"), string.lastIndexOf("}") + 1) : "";
        return "[" + jsonObjectsLocationString + "]";
    }

    private Task<Void> deleteExistingGPSParseFile(final Context context) {
        ParseFile file = getParseFile(Tracker.gpsFile);

        if (file == null) {
            return Task.forResult(null);
        }

        return CloudFunctions.deleteFile(file);
    }

    private Task<Void> saveGPSParseFile(final Context context, final ParseFile file, final ProgressCallback progressCallback, final boolean partialUpload) {


        return deleteExistingGPSParseFile(context).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) {
                return file.saveInBackground(progressCallback);
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) {

                put(Tracker.gpsFile, file);
                put(Tracker.inProgress, partialUpload);

                return saveInBackground();
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) {
                if (task.isFaulted()) {
                    new HandleException(context, TAG, "Error saving GPS file", task.getError());
                    return null;
                }

                if (!partialUpload) {
                    context.deleteFile(LOCAL_GPS_FILE_NAME);
                }

                return null;
            }
        });


    }

    private Task<Void> uploadData(final Context context, ProgressCallback progressCallback, boolean partialUpload) {

        final TaskCompletionSource<Void> taskResult = new TaskCompletionSource<>();

        Log.d(TAG, "create");

        try {

            // Add location before saving (to mark ending postion + time in case of still period)
            appendLocation(context, LocationModule.Recent.getLastKnownLocation());

            String gpsJsonArrayString = readGPSFileAsJSONArrayString(context);

            if (gpsJsonArrayString.isEmpty()) {
                taskResult.setResult(null);
            } else {

                byte[] gzipped = FileIO.compress(gpsJsonArrayString);

                final ParseFile file = new ParseFile("gps.json.gzip", gzipped);

                return saveGPSParseFile(context, file, progressCallback, partialUpload);
            }
        } catch (IllegalStateException e) {
            new HandleException(context, TAG, "Get local file name", e);
            taskResult.setError(e);
        } catch (IOException e) {
            new HandleException(context, TAG, "Read local file", e);
            taskResult.setError(e);
        }

        return taskResult.getTask();

    }

    public void downloadTrackerData(final DownloadTrackerDataCallback callback, ProgressCallback progressCallback) {
        if (!has(Tracker.gpsFile)) {
            callback.done(null, new ParseException(ParseException.OBJECT_NOT_FOUND, "No gps data"));
            return;
        }

        getParseFile(Tracker.gpsFile).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e != null) {
                    callback.done(null, e);
                }

                try {
                    callback.done(TrackerData.locationJSONArraytoTrackerData(FileIO.decompress(data)), null);
                } catch (IOException e1) {
                    callback.done(null, new ParseException(ParseException.OTHER_CAUSE, "Unable to parse GPS data"));
                }
            }
        }, progressCallback);
    }


    // Assume still until proven otherwise
    private int previousActivityType = DetectedActivity.STILL;

    public void appendLocation(final Context context, Location location) {


        if (location == null || location.isFromMockProvider()) {
            return;
        }

        int currentActivityType = ActivityDetectionModule.Recent.getDetectedActivityType();

        // Only add one location while still
        if (previousActivityType == DetectedActivity.STILL && currentActivityType == DetectedActivity.STILL) {
            return;
        }


        JSONObject jsonLocation = TrackerData.locationToJSONObject(location);
        try {
            FileIO.writeToFile(context, LOCAL_GPS_FILE_NAME, Context.MODE_APPEND, jsonLocation.toString());
            FileIO.writeToFile(context, LOCAL_GPS_FILE_NAME, Context.MODE_APPEND, ",");

            previousActivityType = currentActivityType;

        } catch (IOException e) {
            new HandleException(context, TAG, "append location to local file", e);
        }


    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Tracker> getAllNetworkQuery() {
        return new TrackerQueryBuilder(false).build();
    }


    public Guard getGuard() {
        return (Guard) getLDSFallbackParseObject(Tracker.guard);
    }

    public String getGuardName() {
        return getGuard() != null ? getGuard().getName() : "";
    }

    public Date getDateStart() {
        return getDate(Tracker.sampleStart);
    }

    public Date getDateEnd() {
        return getDate(Tracker.sampleEnd);
    }

    public int getMinutes() {
        return getInt(Tracker.sampleMinutes);
    }

    public boolean inProgress() {
        return getBooleanSafe(Tracker.inProgress, false);
    }

    public interface DownloadTrackerDataCallback {
        void done(TrackerData[] trackerData, Exception e);
    }

}
