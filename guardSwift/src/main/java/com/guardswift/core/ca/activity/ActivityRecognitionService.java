package com.guardswift.core.ca.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.guardswift.BuildConfig;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.GeofencingModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingService;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.TriggerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class ActivityRecognitionService extends InjectingService {
    private static final String TAG = ActivityRecognitionService.class.getSimpleName();

    PowerManager.WakeLock wl;

    private static final int INACTIVITY_TIMEOUT_MINUTES = 60;

    private Subscription filteredActivitySubscription;
    private TriggerTask logoutOnStill;

    @Inject
    GeofencingModule geofencingModule;
    @Inject
    ParseModule parseModule;

    public ActivityRecognitionService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.logoutOnStill = new TriggerTask();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        wl.acquire();
    }


    private static boolean mJustStarted;

    private TextToSpeech ttobj;

    public static void start(Context context) {
        mJustStarted = true;
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

    /**
     * Manual restart of service
     * http://stackoverflow.com/questions/24077901/how-to-create-an-always-running-background-service
     */
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Intent restartService = new Intent(getApplicationContext(),
//                this.getClass());
//        restartService.setPackage(getPackageName());
//        PendingIntent restartServicePI = PendingIntent.getService(
//                getApplicationContext(), 1, restartService,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);
//
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void speakText(String toSpeak) {
        Log.d(TAG, "Speak: " + toSpeak);
        ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping " + TAG);

        releaseTextToSpeech();
        unsubscribeActivityUpdates();

        wl.release();

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

                        return mJustStarted || (isNewActivity && highConfidence && notUnknown);
                    }
                }).subscribe(new Action1<ActivityRecognitionResult>() {
                    @Override
                    public void call(ActivityRecognitionResult activityRecognitionResult) {

                        DetectedActivity previousDetectedActivity = ActivityDetectionModule.Recent.getDetectedActivity();
                        DetectedActivity currentDetectedActivity = activityRecognitionResult.getMostProbableActivity();

                        ActivityDetectionModule.Recent.setDetectedActivity(currentDetectedActivity);

                        if (BuildConfig.DEBUG) {
                            speakText(ActivityDetectionModule.getNameFromType(currentDetectedActivity.getType()));
                        }

                        geofencingModule.matchGeofencedWithDetectedActivity(currentDetectedActivity);


                        mJustStarted = false;


                        logoutOnInactivity(currentDetectedActivity);

                    }
                });
    }

    private void logoutOnInactivity(DetectedActivity activity) {
        Guard guard = GuardSwiftApplication.getLoggedIn();
        if (guard != null && guard.canAccessRegularTasks()) {
            if (activity.getType() == DetectedActivity.STILL) {
                logoutOnStill.start(new TimerTask() {
                    @Override
                    public void run() {
                        speakText("Inactivity logout complete");
                        parseModule.logoutDueToInactivity();
                    }
                }, INACTIVITY_TIMEOUT_MINUTES);
            } else {
                logoutOnStill.stop();
            }
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
