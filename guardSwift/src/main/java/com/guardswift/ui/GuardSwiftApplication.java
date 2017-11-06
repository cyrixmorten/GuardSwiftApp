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
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityRecognitionService;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.exceptions.LogError;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.eventbus.events.BootstrapCompleted;
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
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.misc.Message;
import com.guardswift.persistence.parse.misc.Update;
import com.guardswift.persistence.parse.query.AlarmTaskQueryBuilder;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.persistence.parse.query.StaticTaskQueryBuilder;
import com.guardswift.persistence.parse.query.TaskGroupStartedQueryBuilder;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.ToastHelper;
import com.parse.DeleteCallback;
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

        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                new HandleException(TAG, "RxJava", e);
            }
        });

        // Fixes android.os.FileUriExposedException
        // https://stackoverflow.com/a/40674771/1501613
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

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
            LogError.log(TAG, "Failed to get last active guard ", e);
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
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Update.class);

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
        RegisterGeofencesIntentService.start(getInstance(), true);
    }

    public void stopServices() {
        ActivityRecognitionService.stop(this);
        FusedLocationTrackerService.stop(this);
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

    public boolean isBootstrapInProgress() {
        return bootstrapInProgress;
    }



    private MaterialDialog retryBootstrapDialog;
    public Task<Void> bootstrapParseObjectsLocally(final Activity activity, final Guard guard, boolean performInBackground) {

        if (parseObjectsBootstrapped || bootstrapInProgress) {
            return Task.forResult(null);
        }

        saveInstallation();
        bootstrapInProgress = true;


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

        final Continuation<List<ParseObject>, List<ParseObject>> updateClassSuccess = new Continuation<List<ParseObject>, List<ParseObject>>() {
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


        ArrayList<Pair<ExtendedParseObject, ParseQuery>> updateQueries = Lists.newArrayList();

        EventType eventType = new EventType();
        updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(eventType, eventType.getAllNetworkQuery()));

        Client client = new Client();
        updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(client, client.getAllNetworkQuery()));

        updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(guard, guard.getAllNetworkQuery()));

        ParseTask task = new ParseTask();
        TaskGroupStarted taskGroupStarted = new TaskGroupStarted();
        Message message = new Message();

        if (guard.canAccessRegularTasks()) {
            updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(task, new RegularRaidTaskQueryBuilder(false).build()));
            updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(taskGroupStarted, new TaskGroupStartedQueryBuilder(false).whereActive().build()));
            updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(message, message.getAllNetworkQuery()));
        }
        if (guard.canAccessStaticTasks()) {
            updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(task, new AlarmTaskQueryBuilder(false).build()));
        }
        if (guard.canAccessAlarms()) {
            updateQueries.add(new Pair<ExtendedParseObject, ParseQuery>(task, new StaticTaskQueryBuilder(false).build()));
        }

        updateClassTotal.set(updateQueries.size());

        Task<List<ParseObject>> resultTask = Task.forResult(null);

        for (final Pair<ExtendedParseObject,ParseQuery> objectQueryPair: updateQueries) {

            final ExtendedParseObject parseObject = objectQueryPair.first;
            final ParseQuery<ParseObject> query = objectQueryPair.second;

            resultTask = resultTask.onSuccessTask(new Continuation<List<ParseObject>, Task<List<ParseObject>>>() {
                @Override
                public Task<List<ParseObject>> then(Task<List<ParseObject>> task) throws Exception {
                    return parseObject.updateAll(query, 1000).onSuccess(updateClassSuccess);
                }
            });
        }

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

                        EventBusController.post(new BootstrapCompleted());

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

        if (parseLiveQueryClient != null) {
            parseLiveQueryClient.disconnect();
            parseLiveQueryClient = null;
        }
    }
}
