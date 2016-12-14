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

//    private LinkedHashMap<Date, JSONArray> savedJSONActivities;
//    private LinkedHashMap<Date, JSONObject> savedJSONMostProbableActivities;
//    private LinkedHashMap<Date, List<DetectedActivity>> savedMostProbableActivities;

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

            mIsRunning = true;

            releaseTextToSpeech();
            ttobj = new TextToSpeech(getApplicationContext(),
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                ttobj.setLanguage(Locale.UK);
//                                speakText("GuardSwiftWeb ready");
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
        ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping " + TAG);

        mIsRunning = false;

        releaseTextToSpeech();
        unsubscribeActivityUpdates();

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
//                        boolean acceptableSpeed = true;
                        if (detectedActivity.getType() == DetectedActivity.IN_VEHICLE) {
                            // driving has reduced confidence requirement but needs a trustworthy GPS speed estimate
                            requiredConfidence = 75;
//                            acceptableSpeed = (!hasSpeed) || locationWithSpeed.getSpeed() > walkingSpeed;
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

                        return mJustStarted || (isNewActivity && highConfidence && notUnknown);
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
