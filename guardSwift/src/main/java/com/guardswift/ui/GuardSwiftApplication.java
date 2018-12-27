package com.guardswift.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.util.Pair;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityRecognitionService;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.eventbus.events.BootstrapCompleted;
import com.guardswift.jobs.GSJobCreator;
import com.guardswift.jobs.oneoff.RebuildGeofencesJob;
import com.guardswift.jobs.periodic.TrackerUploadJob;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.data.client.Person;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.documentation.gps.TrackerData;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.misc.Update;
import com.guardswift.persistence.parse.query.TaskGroupStartedQueryBuilder;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.ToastHelper;
import com.parse.LiveQueryException;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseLiveQueryClientCallbacks;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;


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

    private ParseLiveQueryClient parseLiveQueryClient;
    private boolean liveQueryConnected = false;

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
        setupJobs();

        // Global capture of RxJava errors
        RxJavaPlugins.setErrorHandler(throwable -> {
            new HandleException(TAG, "RxJava", throwable);
        });

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        if (BuildConfig.DEBUG) {
            // Force crash with log for leaking db objects
            // https://stackoverflow.com/a/28155638/1501613
            StrictMode.setVmPolicy(builder
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        } else {
            // Fixes android.os.FileUriExposedException
            // https://stackoverflow.com/a/40674771/1501613
            StrictMode.setVmPolicy(builder.build());
        }
    }

    private void setupJobs() {
        JobManager.create(this).addJobCreator(new GSJobCreator());
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
        ParseObject.registerSubclass(Client.class);
        ParseObject.registerSubclass(Person.class);
        ParseObject.registerSubclass(ClientContact.class);
        ParseObject.registerSubclass(ClientLocation.class);
        ParseObject.registerSubclass(ParseTask.class);
        ParseObject.registerSubclass(TaskGroup.class);
        ParseObject.registerSubclass(TaskGroupStarted.class);
        ParseObject.registerSubclass(EventLog.class);
        ParseObject.registerSubclass(EventType.class);
        ParseObject.registerSubclass(EventRemark.class);
        ParseObject.registerSubclass(Guard.class);
        ParseObject.registerSubclass(Update.class);
        ParseObject.registerSubclass(Tracker.class);
        ParseObject.registerSubclass(TrackerData.class);

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


        // https://github.com/square/okhttp/issues/3146#issuecomment-311158567
        new OkHttpClient.Builder()
                .connectTimeout(10_000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .readTimeout(10_000, TimeUnit.MILLISECONDS).connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS));
    }

    private void reconnectLiveQuery(final ParseLiveQueryClient client) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!liveQueryConnected) {
                    client.reconnect();
                    reconnectLiveQuery(client);
                }
            }
        }, 10000);
    }

    public ParseLiveQueryClient getLiveQueryClient() {
        if (parseLiveQueryClient == null) {

            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();

            parseLiveQueryClient.registerListener(new ParseLiveQueryClientCallbacks() {
                @Override
                public void onLiveQueryClientConnected(ParseLiveQueryClient client) {
                    Log.d(TAG, "onLiveQueryClientConnected");
                }

                @Override
                public void onLiveQueryClientDisconnected(ParseLiveQueryClient client, boolean userInitiated) {
                    Log.d(TAG, "onLiveQueryClientDisconnected by user: " + userInitiated);
                }

                @Override
                public void onLiveQueryError(ParseLiveQueryClient client, LiveQueryException reason) {
                    new HandleException(TAG, "onLiveQueryError", reason);
                }

                @Override
                public void onSocketError(ParseLiveQueryClient client, Throwable reason) {
                    new HandleException(TAG, "onSocketError", reason);
                }
            });

        }

        return parseLiveQueryClient;
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

        RebuildGeofencesJob.scheduleJob(false);
        TrackerUploadJob.scheduleJob();
    }

    public void stopServices() {
        ActivityRecognitionService.stop(this);
        FusedLocationTrackerService.stop(this);
        RegisterGeofencesIntentService.stop(this);

        RebuildGeofencesJob.cancelJob();
        TrackerUploadJob.cancelJob();
    }

    private com.google.android.gms.analytics.Tracker tracker;

    public synchronized com.google.android.gms.analytics.Tracker getGoogleAnalyticsTracker() {
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


    public boolean isBootstrapInProgress() {
        return bootstrapInProgress;
    }



    private MaterialDialog retryBootstrapDialog;
    private MaterialDialog updateDialog;

    public Task<Void> bootstrapParseObjectsLocally(final Activity activity, final Guard guard) {

        if (parseObjectsBootstrapped || bootstrapInProgress) {
            Log.i(TAG, "bootstrapParseObjectsLocally aborting");
            Log.i(TAG, "parseObjectsBootstrapped: " + parseObjectsBootstrapped);
            Log.i(TAG, "bootstrapInProgress: " + bootstrapInProgress);
            return Task.forResult(null);
        }

        saveInstallation();
        bootstrapInProgress = true;


        final AtomicInteger updateClassProgress = new AtomicInteger(0);
        final AtomicInteger updateClassTotal = new AtomicInteger(0);

        // Prepare dialog showing progress

        if (activity != null) {
            updateDialog = new MaterialDialog.Builder(activity)
                    .title(R.string.working)
                    .content(R.string.please_wait)
                    .progress(false, 0, true).show();

        }

        final Continuation<List<ParseObject>, Void> updateClassSuccess = new Continuation<List<ParseObject>, Void>() {
            @Override
            public Void then(Task<List<ParseObject>> task) {

                int currentProgress = updateClassProgress.incrementAndGet();
                int outOfTotal = updateClassTotal.get();

                int percentProgress = (currentProgress / outOfTotal) * 100;

                if (updateDialog != null) {
                    updateDialog.setMaxProgress(outOfTotal);
                    updateDialog.setProgress(currentProgress);
                }


                Log.i(TAG, String.format("update progress: %1d/%2d percent: %3d", currentProgress, outOfTotal, percentProgress));

                return null;
            }
        };


        ArrayList<Pair<ExtendedParseObject, ParseQuery>> updateQueries = Lists.newArrayList();

        EventType eventType = new EventType();
        updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(eventType, eventType.getAllNetworkQuery()));

        TaskGroupStarted taskGroupStarted = new TaskGroupStarted();

        if (guard.canAccessRegularTasks()) {
            updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(taskGroupStarted, new TaskGroupStartedQueryBuilder(false).whereActive().build()));
        }

        updateClassTotal.set(updateQueries.size());

        Task<Void> resultTask = Task.forResult(null);

        for (final Pair<ExtendedParseObject,ParseQuery> objectQueryPair: updateQueries) {

            final ExtendedParseObject parseObject = objectQueryPair.first;
            final ParseQuery<ParseObject> query = objectQueryPair.second;

            resultTask = resultTask.onSuccessTask(new Continuation<Void, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Void> task) {
                    return parseObject.updateAll(query, 1000).onSuccess(updateClassSuccess);
                }
            });
        }

        return resultTask
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) {

                        Log.i(TAG, "Bootstrap done");

                        // no matter what happens e.g. success/error, the dialog should be dismissed
                        if (updateDialog != null) {
                            updateDialog.dismiss();
                            updateDialog = null;
                        }

                        bootstrapInProgress = false;

                        if (task.isFaulted()) {
                            Log.i(TAG, "Bootstrap failed");
                            new HandleException(TAG, "bootstrapParseObjectsLocally", task.getError());


                            if (activity != null) {
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
                            }


                            return Task.forError(task.getError());
                        }


                        return Task.forResult(null);
                    }
                })
                .onSuccess(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) {

                        Log.i(TAG, "Bootstrap success");

                        EventBusController.post(new BootstrapCompleted());

                        parseObjectsBootstrapped = true;

                        startServices();

                        return null;
                    }
                });
    }

    public void teardownParseObjectsLocally(boolean unpinParseObjects) {
        parseObjectsBootstrapped = false;

        if (retryBootstrapDialog != null) {
            retryBootstrapDialog.dismiss();
            retryBootstrapDialog = null;
        }

        if (parseLiveQueryClient != null) {
            parseLiveQueryClient.disconnect();
            parseLiveQueryClient = null;
        }

        if (unpinParseObjects) {
            ParseObject.unpinAllInBackground();
        }
    }
}
