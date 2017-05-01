package com.guardswift.persistence.parse.documentation.gps;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.util.FileIO;
import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("Tracker")
public class Tracker extends ExtendedParseObject {

    private static String TAG = Tracker.class.getSimpleName();

    private static final String guard = "guard";
    private static final String sampleStart = "start";
    private static final String sampleEnd = "end";
    private static final String sampleMinutes = "minutes";
    private static final String clientTimestamp = "clientTimestamp";

    private static final String gpsFile = "gpsFile";


    private static final String LOCAL_GPS_FILE_NAME = "gps_track.json";


    public Tracker() {
    }

    @Override
    public String getParseClassName() {
        return Tracker.class.getSimpleName();
    }


    public static Task<Void> upload(final Context context, final Guard guard, final ProgressCallback progressCallback) {

        final Tracker tracker = new Tracker();

        DateTime started = new DateTime(guard.getLastLogin());
        DateTime ended = DateTime.now();

        tracker.put(Tracker.sampleStart, started.toDate());
        tracker.put(Tracker.sampleEnd, ended.toDate());
        tracker.put(Tracker.sampleMinutes, Minutes.minutesBetween(started, ended).getMinutes());
        tracker.put(Tracker.guard, guard);
        tracker.put(Tracker.clientTimestamp, ended.toDate());
        tracker.put(owner, ParseUser.getCurrentUser());

        return tracker.upload(context, progressCallback);

    }

    public static boolean deleteLocalFile() {
        return new File(LOCAL_GPS_FILE_NAME).delete();
    }

    private String readGPSFileAsJSONArrayString(Context context) throws IOException {
        String string = FileIO.readFromFile(context, LOCAL_GPS_FILE_NAME);
        String jsonObjectsLocationString = (string.contains("{")) ? string.substring(string.indexOf("{"), string.lastIndexOf("}") + 1) : "";
        return "[" + jsonObjectsLocationString + "]";
    }

    private Task<Void> saveGPSParseFile(final Context context, final ParseFile file, ProgressCallback progressCallback) {

        Task<Void> saveFileTask = file.saveInBackground(progressCallback);

        return saveFileTask.onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {

                put(Tracker.gpsFile, file);

                return saveInBackground();
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    new HandleException(context, TAG, "Error saving GPS file", task.getError());
                    return null;
                }

                deleteLocalFile();

                return null;
            }
        });

    }

    private Task<Void> upload(final Context context, ProgressCallback progressCallback) {

        final TaskCompletionSource<Void> taskResult = new TaskCompletionSource<>();

        Log.d(TAG, "upload");

        try {

            String gpsJsonArrayString = readGPSFileAsJSONArrayString(context);

            if (gpsJsonArrayString.isEmpty()) {
                taskResult.setResult(null);
            } else {

                byte[] gzipped = FileIO.compress(gpsJsonArrayString);

                final ParseFile file = new ParseFile("gps.json.gzip", gzipped);

                return saveGPSParseFile(context, file, progressCallback);
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Get local file name", e);
            taskResult.setError(e);
        } catch (IOException e) {
            Log.e(TAG, "Read local file", e);
            taskResult.setError(e);
        }

        return taskResult.getTask();

    }



    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    private int previousActivityType = Integer.MAX_VALUE;

    public void appendLocation(final Context context, @NonNull final Location location) {
        if (location.isFromMockProvider()) {
            return;
        }

        int currentActivityType = ActivityDetectionModule.Recent.getDetectedActivityType();

        // Only add one location while still
        if (previousActivityType == DetectedActivity.STILL && currentActivityType == DetectedActivity.STILL) {
            return;
        }

        JSONObject jsonLocation = LocationModule.locationToJSONObject(location);
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
        return new QueryBuilder(false).build();
    }


    public static class QueryBuilder extends ParseQueryBuilder<Tracker> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Tracker.class));
        }

        @Override
        public ParseQuery<Tracker> build() {
            query.include(Tracker.guard);
            return super.build();
        }

        public QueryBuilder matching(Guard guard) {
            query.whereEqualTo(Tracker.guard, guard);
            return this;
        }

        public QueryBuilder matching(Date date) {
            query.whereEqualTo(Tracker.sampleStart, date);
            return this;
        }
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

    public interface DownloadTrackerDataCallback {
        void done(TrackerData[] trackerData, Exception e);
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
                    TrackerData[] trackerDataArray = new Gson().fromJson(FileIO.decompress(data), TrackerData[].class);
                    callback.done(trackerDataArray, null);
                } catch (IOException e1) {
                    callback.done(null, new ParseException(ParseException.OTHER_CAUSE, "Unable to parse GPS data"));
                }
            }
        }, progressCallback);
    }
}
