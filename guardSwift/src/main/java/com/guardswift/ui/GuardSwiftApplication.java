package com.guardswift.ui;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityRecognitionService;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.parse.ParseObjectFactory;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.data.client.Person;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.documentation.gps.LocationTracker;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatch;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
import com.guardswift.persistence.parse.execution.task.regular.Circuit;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

public class GuardSwiftApplication extends InjectingApplication {

    private static final String TAG = GuardSwiftApplication.class.getSimpleName();

    @Inject
    ParseCacheFactory parseCacheFactory;


    private static GuardSwiftApplication instance;

    public static GuardSwiftApplication getInstance() {
        return instance;
    }

    private boolean parseObjectsBootstrapped;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        EventBus.builder().logNoSubscriberMessages(false).throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();

        // Android-Bootstrap https://github.com/Bearded-Hen/Android-Bootstrap
        TypefaceProvider.registerDefaultIconSets();

        JodaTimeAndroid.init(this);

        setupParse();
        setupFabric();

        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                Log.w("Error", e);
                Crashlytics.log("RxJavaError: " + e.getMessage());
                Crashlytics.logException(e);
            }
        });


        if (parseCacheFactory.getGuardCache().isLoggedIn()) {
            startServices();
        }
    }


    private void setupParse() {
        ParseObject.registerSubclass(Report.class);
        ParseObject.registerSubclass(StaticTask.class);
        ParseObject.registerSubclass(Circuit.class);
        ParseObject.registerSubclass(Client.class);
        ParseObject.registerSubclass(Person.class);
        ParseObject.registerSubclass(ClientContact.class);
        ParseObject.registerSubclass(ClientLocation.class);
        ParseObject.registerSubclass(CircuitUnit.class);
        ParseObject.registerSubclass(CircuitStarted.class);
        ParseObject.registerSubclass(DistrictWatch.class);
        ParseObject.registerSubclass(DistrictWatchStarted.class);
        ParseObject.registerSubclass(DistrictWatchClient.class);
        ParseObject.registerSubclass(EventLog.class);
        ParseObject.registerSubclass(EventType.class);
        ParseObject.registerSubclass(EventRemark.class);
        ParseObject.registerSubclass(Guard.class);

        ParseObject.registerSubclass(LocationTracker.class);

        String applicationId = "guardswift";

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DEVELOPMENT");
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        } else {
            Log.d(TAG, "RELEASE");
        }

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(applicationId)
                .clientKey(null)
                .server(BuildConfig.PARSE_SERVER_URL)
                .enableLocalDataStore()
                .build()
        );

        ParseUser.enableRevocableSessionInBackground();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicWriteAccess(false);
        defaultACL.setPublicReadAccess(false);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    private void setupFabric() {
        Fabric.with(this, new Crashlytics());
        logUser();
    }

    private void logUser() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            Crashlytics.setUserIdentifier(user.getObjectId());
            Crashlytics.setUserEmail(user.getEmail());
            Crashlytics.setUserName(user.getUsername());
        }
    }

    public void startServices() {
        ActivityRecognitionService.start(this);
        FusedLocationTrackerService.start(this);
        WiFiPositioningService.start(this);
    }

    public void stopServices() {
        ActivityRecognitionService.stop(this);
        FusedLocationTrackerService.stop(this);
        WiFiPositioningService.stop(this);
    }

    private Tracker tracker;

    public synchronized Tracker getTracker() {
        if (tracker == null) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            tracker = analytics.newTracker(R.xml.app_tracker);

            if (BuildConfig.DEBUG)
                analytics.setDryRun(true);
        }
        return tracker;
    }

    public ParseCacheFactory getCacheFactory() {
        return parseCacheFactory;
    }

    public Task<Void> bootstrapParseObjectsLocally(final Activity activity, Guard guard) {
        return bootstrapParseObjectsLocally(activity, guard, false);
    }

    public Task<Void> bootstrapParseObjectsLocally(final Activity activity, final Guard guard, boolean performInBackground) {

        if (parseObjectsBootstrapped) {
            return Task.forResult(null);
        }

        ParseObjectFactory parseObjectFactory = new ParseObjectFactory();

        final AtomicInteger updateClassProgress = new AtomicInteger(0);
        final AtomicInteger updateClassTotal = new AtomicInteger(0);

        // Prepare dialog showing progress
        final MaterialDialog updateDialog = new MaterialDialog.Builder(activity)
                .title(R.string.working)
                .content(R.string.please_wait)
                .progress(false, 0, true).build();

        if (!performInBackground) {
            updateDialog.show();
        }

        Continuation<List<ParseObject>, List<ParseObject>> updateClassSuccess = new Continuation<List<ParseObject>, List<ParseObject>>() {
            @Override
            public List<ParseObject> then(Task<List<ParseObject>> task) throws Exception {
                int currentProgress = updateClassProgress.incrementAndGet();
                int outOfTotal = updateClassTotal.get();

                int percentProgress = (currentProgress / outOfTotal) * 100;

                if (updateDialog != null) {
                    updateDialog.setMaxProgress(outOfTotal);
                    updateDialog.setProgress(currentProgress);
                }

                Log.d(TAG, String.format("update progress: %1d/%2d percent: %3d", currentProgress, outOfTotal, percentProgress));

                return null;
            }
        };


        ArrayList<Task<List<ParseObject>>> tasks = new ArrayList<>();


        Task<List<ParseObject>> eventTypes = parseObjectFactory.getEventType()
                .updateAllAsync();
        tasks.add(eventTypes.onSuccess(updateClassSuccess));

        if (guard.canAccessRegularTasks()) {
            Task<List<ParseObject>> circuitStartedTask = parseObjectFactory.getCircuitStarted()
                    .updateAllAsync();
            Task<List<ParseObject>> circuitUnit = parseObjectFactory
                    .getCircuitUnit().updateAllAsync();

            tasks.add(circuitStartedTask.onSuccess(updateClassSuccess));
            tasks.add(circuitUnit.onSuccess(updateClassSuccess));
        }

        if (guard.canAccessDistrictTasks()) {
            Task<List<ParseObject>> districtWatchStartedTask = parseObjectFactory
                    .getDistrictWatchStarted().updateAllAsync();
            Task<List<ParseObject>> districtWatchClient = parseObjectFactory
                    .getDistrictWatchClient().updateAllAsync();

            tasks.add(districtWatchStartedTask.onSuccess(updateClassSuccess));
            tasks.add(districtWatchClient.onSuccess(updateClassSuccess));
        }

        updateClassTotal.set(tasks.size());

        return Task.whenAll(tasks).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> result) throws Exception {
                // no matter what happens e.g. success/error, the dialog should be dismissed
                if (updateDialog != null) {
                    updateDialog.dismiss();
                }

                if (result.getError() == null) {
                    parseObjectsBootstrapped = true;
                } else {
                    new HandleException(TAG, "bootstrapParseObjectsLocally", result.getError());
                    new CommonDialogsBuilder.MaterialDialogs(activity).ok(R.string.title_internet_missing, getString(R.string.bootstrapping_parseobjects_failed), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                            bootstrapParseObjectsLocally(activity, guard);
                        }
                    }).cancelable(false).show();
                }

                return null;
            }
        });
    }

    public void teardownParseObjectsLocally() {
        // todo move last part of logout process here
        parseObjectsBootstrapped = false;
    }
}
