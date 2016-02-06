package com.guardswift.core.ca.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.guardswift.BuildConfig;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.GeofencingModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

//import com.guardswift.persistence.parse.documentation.activity.ActivityRecognition;

public class ActivityRecognitionService extends InjectingService {
    private static final String TAG = ActivityRecognitionService.class.getSimpleName();

    private Subscription filteredActivitySubscription;
    private Subscription loggingActivitySubscription;

    private LinkedHashMap<Date, JSONArray> savedJSONActivities;
    private LinkedHashMap<Date, JSONObject> savedJSONMostProbableActivities;
    private LinkedHashMap<Date, List<DetectedActivity>> savedMostProbableActivities;

//    private Preferences preferences;

//    private CircuitUnit circuitUnit;

    @Inject
    GeofencingModule geofencingModule;
    @Inject
    ParseModule parseModule;

    private DectectedActivityInactivityTimer dectectedActivityInactivityTimerTask;

    public ActivityRecognitionService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.dectectedActivityInactivityTimerTask = new DectectedActivityInactivityTimer(getApplicationContext());
    }

    private static boolean mIsRunning;
    private static boolean mJustStarted;

    private TextToSpeech ttobj;

    public static void start(Context context) {
        mJustStarted = true;
        Log.i(TAG, "STARTING - already started: " + mIsRunning);
//        if (!mIsRunning)
        context.startService(new Intent(context, ActivityRecognitionService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ActivityRecognitionService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting " + TAG);

        unsubscribeActivityUpdates();

        if (hasGooglePlayServices()) {
            savedJSONActivities = new LinkedHashMap<>();
            savedJSONMostProbableActivities = new LinkedHashMap<>();
            savedMostProbableActivities = new LinkedHashMap<>();

            mIsRunning = true;

            releaseTextToSpeech();
            ttobj = new TextToSpeech(getApplicationContext(),
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                ttobj.setLanguage(Locale.UK);
//                                speakText("GuardSwift ready");
                            }
                        }
                    });

            requestFilteredActivityUpdates();

        } else {
            Log.e(TAG, "Missing Google Play Services");
            return Service.START_NOT_STICKY;
        }
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void speakText(String toSpeak) {
        Log.d(TAG, "Speak: " + toSpeak);
//        ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping " + TAG);

        mIsRunning = false;

        releaseTextToSpeech();
        unsubscribeActivityUpdates();
//        uploadResults();

        savedJSONActivities = null;
        savedJSONMostProbableActivities = null;
        savedMostProbableActivities = null;
//        preferences = null;

        super.onDestroy();
    }

    private void releaseTextToSpeech() {
        if (ttobj != null) {
            ttobj.stop();
            ttobj.shutdown();
        }
    }

    private void requestFilteredActivityUpdates() {
        Log.d(TAG, "requestFilteredActivityUpdates");

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());
        filteredActivitySubscription = locationProvider.getDetectedActivity(0)
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        String message = "Error on activitySubscription: " + throwable.getMessage();
                        Log.e(TAG, message, throwable);
                        Crashlytics.logException(throwable);
                    }
                }).onErrorReturn(new Func1<Throwable, ActivityRecognitionResult>() {
                    @Override
                    public ActivityRecognitionResult call(Throwable throwable) {
                        List<DetectedActivity> list = new ArrayList<DetectedActivity>();
                        list.add(new DetectedActivity(DetectedActivity.UNKNOWN, 0));
                        return new ActivityRecognitionResult(list, System.currentTimeMillis(), SystemClock.elapsedRealtime());
                    }
                }).filter(new Func1<ActivityRecognitionResult, Boolean>() {
                    @Override
                    public Boolean call(ActivityRecognitionResult activityRecognitionResult) {

                        DetectedActivity detectedActivity = activityRecognitionResult.getMostProbableActivity();

                        Location locationWithSpeed = LocationModule.Recent.getLastKnownLocationWithSpeed();
                        boolean hasSpeed = locationWithSpeed != null;
                        float walkingSpeed = 1.4f;

                        int requiredConfidence = 75;
                        boolean acceptableSpeed = true;
                        if (detectedActivity.getType() == DetectedActivity.IN_VEHICLE) {
                            // driving has reduced confidence requirement but needs a trustworthy GPS speed estimate
                            requiredConfidence = 50;
                            acceptableSpeed = (!hasSpeed) || locationWithSpeed.getSpeed() > walkingSpeed;
                        }
                        if (detectedActivity.getType() == DetectedActivity.STILL) {
                            requiredConfidence = 100;
                        }

                        boolean highConfidence = detectedActivity.getConfidence() >= requiredConfidence;
                        boolean notUnknown = detectedActivity.getType() != DetectedActivity.UNKNOWN;


                        DetectedActivity previousActivity = ActivityDetectionModule.Recent.getDetectedActivity();

                        boolean isNewActivity = detectedActivity.getType() != previousActivity.getType();
//                        boolean hasHigherConfidence = detectedActivity.getConfidence() > previousActivity.getConfidence();

//                        if (!acceptableSpeed) {
//                            Log.e(TAG, "GPS speed blocking activity: " + ActivityDetectionModule.getNameFromType(detectedActivity.getType()) + " " + locationWithSpeed.getSpeed());
//                        }
//                        Log.w(TAG, "Filter: " + ActivityDetectionModule.getNameFromType(detectedActivity.getType()) + " confidence: " + detectedActivity.getConfidence() + " speed: " + ((hasSpeed) ? locationWithSpeed.getSpeed() : "?"));
//                        boolean passedFilter = (mJustStarted || (isNewActivity && highConfidence && notUnknown && acceptableSpeed));
//                        Log.w(TAG, "Filter passed: " + passedFilter);
//                        if (!passedFilter) {
//                            Log.w(TAG, "isNewActivity: " + isNewActivity);
//                            Log.w(TAG, "highConfidence: " + highConfidence);
//                            Log.w(TAG, "notUnknown: " + notUnknown);
//                            Log.w(TAG, "acceptableSpeed: " + acceptableSpeed);
//                        }

                        return mJustStarted || (isNewActivity && highConfidence && notUnknown && acceptableSpeed);
                    }
                }).subscribe(new Action1<ActivityRecognitionResult>() {
                    @Override
                    public void call(ActivityRecognitionResult activityRecognitionResult) {

                        DetectedActivity previousDetectedActivity = ActivityDetectionModule.Recent.getDetectedActivity();
                        DetectedActivity currentDetectedActivity = activityRecognitionResult.getMostProbableActivity();
                        // TODO enable power saving feature
//                        handleInactivityTimer(previousDetectedActivity, currentDetectedActivity);

//                        Log.i(TAG, "Activity changed or increased in confidence:");
//                        Log.i(TAG, "Last: " + ActivityDetectionModule.getNameFromType(ActivityDetectionModule.Recent.getDetectedActivity().getType()) + " confidence: " + ActivityDetectionModule.Recent.getDetectedActivity().getConfidence());
//                        Log.i(TAG, "New: " + ActivityDetectionModule.getNameFromType(currentDetectedActivity.getType()) + " confidence: " + currentDetectedActivity.getConfidence());
                        ActivityDetectionModule.Recent.setDetectedActivity(currentDetectedActivity);

                        if (BuildConfig.DEBUG) {
                            speakText(ActivityDetectionModule.getNameFromType(currentDetectedActivity.getType()));
                        }

                        geofencingModule.matchGeofencedWithDetectedActivity(currentDetectedActivity);


                        mJustStarted = false;

//                        if (BuildConfig.DEBUG) {
//                            new EventLog.Builder(getApplicationContext()).
//                                    event("Activity").
//                                    activity(activityRecognitionResult).
//                                    remarks(ActivityDetectionModule.getNameFromType(ActivityDetectionModule.Recent.getDetectedActivity().getType())).
//                                    saveAsync();
//                        }

                    }
                });
    }

    private void handleInactivityTimer(DetectedActivity previousDetectedActivity, DetectedActivity currentDetectedActivity) {
        Log.d(TAG, "handleInactivityTimer");
        if (previousDetectedActivity.getType() != DetectedActivity.STILL && currentDetectedActivity.getType() == DetectedActivity.STILL) {
            Log.d(TAG, " - handleInactivityTimer start timer");
            // Turns off Location updates and WiFi scanning after a period of inactivity
            dectectedActivityInactivityTimerTask.start();
        }

        if (previousDetectedActivity.getType() == DetectedActivity.STILL && currentDetectedActivity.getType() != DetectedActivity.STILL){
            Log.d(TAG, " - handleInactivityTimer still->not still");
            // Moving from still to other
            if (dectectedActivityInactivityTimerTask.isTriggered()) {
                Log.w(TAG, " - handleInactivityTimer restart services");
                FusedLocationTrackerService.start(getApplicationContext(), LocationRequest.PRIORITY_HIGH_ACCURACY);
                WiFiPositioningService.start(getApplicationContext());
            }
        }

        if (currentDetectedActivity.getType() != DetectedActivity.STILL) {
            Log.d(TAG, " - handleInactivityTimer stop timer");
            // Other than still
            dectectedActivityInactivityTimerTask.stop();
        }
    }


    private void requestLoggingActivityUpdates() {
        Log.d(TAG, "requestLoggingActivityUpdates");

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());

        loggingActivitySubscription = locationProvider.getDetectedActivity(0).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                String message = "Error on activitySubscription: " + throwable.getMessage();
                Log.e(TAG, message, throwable);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Crashlytics.logException(throwable);
            }
        }).onErrorReturn(new Func1<Throwable, ActivityRecognitionResult>() {
            @Override
            public ActivityRecognitionResult call(Throwable throwable) {
                List<DetectedActivity> list = new ArrayList<DetectedActivity>();
                list.add(new DetectedActivity(DetectedActivity.UNKNOWN, 0));
                return new ActivityRecognitionResult(list, System.currentTimeMillis(), 0);
            }
        }).subscribe(new Action1<ActivityRecognitionResult>() {
            @Override
            public void call(ActivityRecognitionResult activityRecognitionResult) {

                if (activityRecognitionResult == null || activityRecognitionResult.getMostProbableActivity() == null)
                    return;

                if (savedMostProbableActivities == null || savedJSONActivities == null || savedJSONMostProbableActivities == null)
                    return;

                DetectedActivity mostProbableActivity = activityRecognitionResult.getMostProbableActivity();

                Date now = new Date();

                savedMostProbableActivities.put(now, activityRecognitionResult.getProbableActivities());
                savedJSONActivities.put(now, activityResultAsJSONArray(activityRecognitionResult));
                savedJSONMostProbableActivities.put(now, detectedActivityResultAsJSON(mostProbableActivity));


            }
        });
    }

    private JSONArray activityResultAsJSONArray(ActivityRecognitionResult activityResult) {
        JSONArray jsonArray = new JSONArray();
        List<DetectedActivity> activityOptions = activityResult.getProbableActivities();
        for (DetectedActivity activity : activityOptions) {
            jsonArray.put(detectedActivityResultAsJSON(activity));
        }
        return jsonArray;
    }

    private JSONObject detectedActivityResultAsJSON(DetectedActivity detectedActivity) {

        int type = detectedActivity.getType();
        int confidence = detectedActivity.getConfidence();
        String name = ActivityRecognitionService.getNameFromType(detectedActivity.getType());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("activityType", type);
        map.put("activityConfidence", confidence);
        map.put("activityName", name);


        JSONObject jsonObject = new JSONObject(map);

        return jsonObject;
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


//    private void uploadResults() {
//
////        EventBus.getDefault().post(new Status("Uploading results"));
//
//        if (timeStart == null) {
////            EventBus.getDefault().post(new Status("No results to upload"));
//            return;
//        }
//
////        String sessionName = preferences.getString(Session.NAME, "N/A");
//
//        List<Long> time_in_seconds = new ArrayList<>();
//        //DetectedActivity.UNKNOWN
//        List<Integer> activity_unknown = new ArrayList<>();
//        //DetectedActivity.TILTING
//        List<Integer> activity_tilting = new ArrayList<>();
//        //DetectedActivity.STILL
//        List<Integer> activity_still = new ArrayList<>();
//        //DetectedActivity.ON_FOOT
//        List<Integer> activity_on_foot = new ArrayList<>();
//        //DetectedActivity.WALKING
//        List<Integer> activity_walking = new ArrayList<>();
//        //DetectedActivity.RUNNING
//        List<Integer> activity_running = new ArrayList<>();
//        //DetectedActivity.ON_BICYCLE
//        List<Integer> activity_on_bicycle = new ArrayList<>();
//        //DetectedActivity.IN_VEHICLE
//        List<Integer> activity_in_vehicle = new ArrayList<>();
//
//        JSONArray finalArray = new JSONArray();
//        int index = 0;
//        int count = 1;
//        for (Date timeStamp : savedJSONActivities.keySet()) {
//
//
//            JSONArray activitiesJSON = savedJSONActivities.get(timeStamp);
//            JSONObject mostProbableActivityJSON = savedJSONMostProbableActivities.get(timeStamp);
//
//            long diffMilliseconds = timeStamp.getTime() - timeStart.getTime();
//            long diffSeconds = diffMilliseconds / 1000;
//            JSONObject finalObject = new JSONObject();
//            try {
//                finalObject.put("count", count);
//                finalObject.put("timeStamp", timeStamp.getTime());
//                finalObject.put("timeStampRelativeMilliseconds", diffMilliseconds);
//                finalObject.put("timeStampRelativeSeconds", diffSeconds);
//                finalObject.put("activities", activitiesJSON);
//                finalObject.put("mostProbableActivity", mostProbableActivityJSON);
//                finalArray.put(finalObject);
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Log.e(TAG, "uploadResults", e);
//            }
//
//            time_in_seconds.addUnique(index, diffSeconds);
//            List<DetectedActivity> activities = savedMostProbableActivities.get(timeStamp);
//            addActivitiesToLists(activities, index, activity_unknown, activity_tilting, activity_still, activity_on_foot, activity_walking, activity_running, activity_on_bicycle, activity_in_vehicle);
//
//
//            index++;
//            count++;
//        }
//
//        final int resultsCount = savedJSONActivities.keySet().size();
//
//
//        long durationMs = System.currentTimeMillis() - timeStart.getTime();
//        long durationSeconds = durationMs / 1000;
//
//
//        ParseObject activityEntry = new ParseObject("ActivityResult");
//
////        if (circuitUnit != null) {
////            activityEntry.put("circuitUnit", circuitUnit);
////            activityEntry.put("client", circuitUnit.getClient());
////        }
//        if (Guard.Recent.getSelected() != null) {
//            activityEntry.put("guard", Guard.Recent.getSelected());
//        }
//        activityEntry.put("deviceModel", Build.MODEL);
//        activityEntry.put("deviceManufacturer", Build.MANUFACTURER);
//        activityEntry.put("deviceStarted", timeStart);
////        activityEntry.put("sessionName", sessionName);
//        activityEntry.put("sessionActivities", finalArray);
//        activityEntry.put("sessionDurationSeconds", durationSeconds);
//        activityEntry.put("sessionDurationMilliseconds", durationMs);
//        activityEntry.put("totalActivities", count);
//        activityEntry.put("times_in_seconds", time_in_seconds);
//        activityEntry.put("activity_unknown", activity_unknown);
//        activityEntry.put("activity_still", activity_still);
//        activityEntry.put("activity_tilting", activity_tilting);
//        activityEntry.put("activity_on_foot", activity_on_foot);
//        activityEntry.put("activity_walking", activity_walking);
//        activityEntry.put("activity_running", activity_running);
//        activityEntry.put("activity_on_bicycle", activity_on_bicycle);
//        activityEntry.put("activity_in_vehicle", activity_in_vehicle);
//
//        StringBuilder b = new StringBuilder();
//        b.append("#\tt\\s\tunkn\tstill\ttilt\tfoot\twalk\trunn\tbicycle\tvehicle");
//        for (int i = 0; i < time_in_seconds.size(); i++) {
//            b.append(System.getProperty("line.separator"));
//
//            // the independent variable
//
//            b.append("\t");
//            b.append(time_in_seconds.get(i));
//
//            // the dependent variables
//
//            b.append("\t");
//            b.append(activity_unknown.get(i));
//
//            b.append("\t");
//            b.append(activity_still.get(i));
//
//            b.append("\t");
//            b.append(activity_tilting.get(i));
//
//            b.append("\t");
//            b.append(activity_on_foot.get(i));
//
//            b.append("\t");
//            b.append(activity_walking.get(i));
//
//            b.append("\t");
//            b.append(activity_running.get(i));
//
//            b.append("\t");
//            b.append(activity_on_bicycle.get(i));
//
//            b.append("\t");
//            b.append(activity_in_vehicle.get(i));
//        }
//
//        activityEntry.put("activity_all", b.toString());
//        activityEntry.saveInBackground();
//    }

    private void addActivitiesToLists(List<DetectedActivity> activities, int index, List<Integer> unknown, List<Integer> tilting, List<Integer> still, List<Integer> on_foot, List<Integer> walking, List<Integer> running, List<Integer> on_bicycle, List<Integer> in_vehicle) {
        // first addUnique 0 confidence to all lists
        unknown.add(index, 0);
        tilting.add(index, 0);
        still.add(index, 0);
        on_foot.add(index, 0);
        walking.add(index, 0);
        running.add(index, 0);
        on_bicycle.add(index, 0);
        in_vehicle.add(index, 0);

        // next addUnique based on activities
        for (DetectedActivity activity : activities) {
            switch (activity.getType()) {
                case DetectedActivity.WALKING:
                    walking.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.IN_VEHICLE:
                    in_vehicle.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.ON_BICYCLE:
                    on_bicycle.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.ON_FOOT:
                    on_foot.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.STILL:
                    still.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.UNKNOWN:
                    unknown.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.TILTING:
                    tilting.set(index, activity.getConfidence());
                    break;
                case DetectedActivity.RUNNING:
                    running.set(index, activity.getConfidence());
                    break;
            }
        }
    }


    public void unsubscribeActivityUpdates() {
        unsubscribe(filteredActivitySubscription);
        unsubscribe(loggingActivitySubscription);
    }

    private void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            Log.i(TAG, "Unsubscribe activity updates");
            try {
                subscription.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasGooglePlayServices() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (result != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }
}
