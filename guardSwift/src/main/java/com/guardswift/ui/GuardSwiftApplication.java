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
import com.google.common.collect.Sets;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityRecognitionService;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.ParseObjectFactory;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.data.client.Person;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatch;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
import com.guardswift.persistence.parse.execution.task.regular.Circuit;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.persistence.parse.misc.Message;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.ToastHelper;
import com.parse.DeleteCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    // mark messages as read for groups
    public static final Set<String> hasReadGroups = Sets.newConcurrentHashSet();

    private static GuardSwiftApplication instance;

    public static GuardSwiftApplication getInstance() {
        return instance;
    }

    private boolean parseObjectsBootstrapped;
    private boolean bootstrapInProgress;
//    private boolean triggerNewGeofence = false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        GuardSwiftApplication application = (GuardSwiftApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...

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
                new HandleException(TAG, "RxJava", e);
            }
        });


    }


    public static void saveCurrentGuardAsLastActive() {
        ParseObject.unpinAllInBackground(GuardCache.LAST_ACTIVE, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                GuardSwiftApplication.getLoggedIn().pinInBackground(GuardCache.LAST_ACTIVE);
            }
        });
    }

    public static Guard getLastActiveGuard() {
        ParseQuery<Guard> query = ParseQuery.getQuery(Guard.class);
        query.fromLocalDatastore();
        query.fromPin(GuardCache.LAST_ACTIVE);
        Guard guard = null;
        try {
            guard = query.getFirst();
        } catch (ParseException e) {
            new HandleException(TAG, "Getting last active guard", e);
        }
        return guard;
    }


    public static Guard getLoggedIn() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().getLoggedIn();
    }


    private void setupParse() {
        ParseObject.registerSubclass(Report.class);
        ParseObject.registerSubclass(StaticTask.class);
        ParseObject.registerSubclass(Circuit.class);
        ParseObject.registerSubclass(Client.class);
        ParseObject.registerSubclass(Person.class);
        ParseObject.registerSubclass(ClientContact.class);
        ParseObject.registerSubclass(ClientLocation.class);
        ParseObject.registerSubclass(ParseTask.class);
        ParseObject.registerSubclass(CircuitUnit.class);
        ParseObject.registerSubclass(CircuitStarted.class);
        ParseObject.registerSubclass(DistrictWatch.class);
        ParseObject.registerSubclass(DistrictWatchStarted.class);
        ParseObject.registerSubclass(DistrictWatchClient.class);
        ParseObject.registerSubclass(EventLog.class);
        ParseObject.registerSubclass(EventType.class);
        ParseObject.registerSubclass(EventRemark.class);
        ParseObject.registerSubclass(Guard.class);
        ParseObject.registerSubclass(Message.class);

        ParseObject.registerSubclass(Tracker.class);

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


    private void saveInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            installation.put("owner", user);
            user.fetchInBackground();
        }
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParsePush.subscribeInBackground("alarm");
            }
        });
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

    private void startServices() {
        Log.d(TAG, "startServices");

        ActivityRecognitionService.start(this);
        FusedLocationTrackerService.start(this);
//        WiFiPositioningService.start(this);
        RegisterGeofencesIntentService.start(this);
    }

    public void stopServices() {
        ActivityRecognitionService.stop(this);
        FusedLocationTrackerService.stop(this);
        WiFiPositioningService.stop(this);
        RegisterGeofencesIntentService.stop(this);
    }

    private com.google.android.gms.analytics.Tracker tracker;

    public synchronized com.google.android.gms.analytics.Tracker getTracker() {
        if (tracker == null) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            tracker = analytics.newTracker(R.xml.app_tracker);

            analytics.setDryRun(BuildConfig.DEBUG);
        }
        return tracker;
    }

    public ParseCacheFactory getCacheFactory() {
        return parseCacheFactory;
    }

    public Task<Void> bootstrapParseObjectsLocally(final Activity activity, Guard guard) {
        return bootstrapParseObjectsLocally(activity, guard, false);
    }

    private MaterialDialog retryBootstrapDialog;
    public Task<Void> bootstrapParseObjectsLocally(final Activity activity, final Guard guard, boolean performInBackground) {

        if (parseObjectsBootstrapped || bootstrapInProgress) {
            return Task.forResult(null);
        }

        saveInstallation();
        bootstrapInProgress = true;

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

                if (task.getResult() != null && !task.getResult().isEmpty()) {
                    Log.d(TAG, task.getResult().get(0).getClassName());
                }

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


        Task<List<ParseObject>> eventTypes = parseObjectFactory.getEventType().updateAllAsync();
        tasks.add(eventTypes.onSuccess(updateClassSuccess));

        Task<List<ParseObject>> clients = parseObjectFactory.getClient().updateAllAsync();
        tasks.add(clients.onSuccess(updateClassSuccess));

        Task<List<ParseObject>> guards = parseObjectFactory.getGuard().updateAllAsync();
        tasks.add(guards.onSuccess(updateClassSuccess));

        if (guard.canAccessRegularTasks()) {
            Task<List<ParseObject>> circuitStartedTask = parseObjectFactory.getCircuitStarted().updateAllAsync();
            Task<List<ParseObject>> circuitUnit = parseObjectFactory.getCircuitUnit().updateAllAsync();

            tasks.add(circuitStartedTask.onSuccess(updateClassSuccess));
            tasks.add(circuitUnit.onSuccess(updateClassSuccess));

            Task<List<ParseObject>> message = parseObjectFactory.getMessage().updateAllAsync();
            tasks.add(message.onSuccess(updateClassSuccess));
        }

//        if (guard.canAccessDistrictTasks()) {
//            Task<List<ParseObject>> districtWatchStartedTask = parseObjectFactory.getDistrictWatchStarted().updateAllAsync();
//            Task<List<ParseObject>> districtWatchClient = parseObjectFactory.getDistrictWatchClient().updateAllAsync();
//
//
//            tasks.add(districtWatchStartedTask.onSuccess(updateClassSuccess));
//            tasks.add(districtWatchClient.onSuccess(updateClassSuccess));
//        }

        if (guard.canAccessAlarms()) {
            Task<List<ParseObject>> alarms = parseObjectFactory.getTask().updateAllAsync();

            tasks.add(alarms.onSuccess(updateClassSuccess));
        }

        updateClassTotal.set(tasks.size());

        Task<List<ParseObject>> resultTask = Task.forResult(null);

        for (final Task<List<ParseObject>> syncTask: tasks) {
            resultTask = resultTask.onSuccessTask(new Continuation<List<ParseObject>, Task<List<ParseObject>>>() {
                @Override
                public Task<List<ParseObject>> then(Task<List<ParseObject>> task) throws Exception {
                    return syncTask;
                }
            });
        }
        

//        return Task.whenAll(tasks)
        return resultTask
                .continueWithTask(new Continuation<List<ParseObject>, Task<List<ParseObject>>>() {
                    @Override
                    public Task<List<ParseObject>> then(Task<List<ParseObject>> task) throws Exception {

                        // no matter what happens e.g. success/error, the dialog should be dismissed
                        if (updateDialog != null) {
                            updateDialog.dismiss();
                        }

                        bootstrapInProgress = false;

                        if (task.isFaulted()) {
                            Log.d(TAG, "Bootstrap failed");
                            new HandleException(TAG, "bootstrapParseObjectsLocally", task.getError());


                            if (getLoggedIn() != null) {
                                retryBootstrapDialog = new CommonDialogsBuilder.MaterialDialogs(activity).ok(R.string.title_internet_missing, getString(R.string.bootstrapping_parseobjects_failed), new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                        bootstrapParseObjectsLocally(activity, guard);
                                    }
                                }).cancelable(false).show();
                            } else {
                                ToastHelper.toast(activity, getString(R.string.error_an_error_occured));
                            }


//                            throw task.getError();
                        }


                        return task;
                    }
                })
                .onSuccess(new Continuation<List<ParseObject>, Void>() {
                    @Override
                    public Void then(Task<List<ParseObject>> task) throws Exception {

                        Log.d(TAG, "Bootstrap success");

                        parseObjectsBootstrapped = true;
                        startServices();

                        return null;
                    }
                });
    }

    public void teardownParseObjectsLocally() {
        // todo move last part of logout process here
        parseObjectsBootstrapped = false;

        if (retryBootstrapDialog != null) {
            retryBootstrapDialog.dismiss();
            retryBootstrapDialog = null;
        }
    }
}
