package com.guardswift.ui;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityRecognitionService;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.parse.data.AlarmGroup;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.Message;
import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitEnding;
import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitStarting;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.data.client.Person;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.documentation.gps.GPSLog;
import com.guardswift.persistence.parse.documentation.gps.LocationTracker;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatch;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchStarted;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchUnit;
import com.guardswift.persistence.parse.execution.regular.Circuit;
import com.guardswift.persistence.parse.execution.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.regular.CircuitUnit;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

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
		ParseObject.registerSubclass(GPSLog.class);
		ParseObject.registerSubclass(Report.class);
		ParseObject.registerSubclass(Alarm.class);
		ParseObject.registerSubclass(AlarmGroup.class);
		ParseObject.registerSubclass(Circuit.class);
		ParseObject.registerSubclass(Client.class);
		ParseObject.registerSubclass(Person.class);
		ParseObject.registerSubclass(ClientContact.class);
		ParseObject.registerSubclass(Message.class);
		ParseObject.registerSubclass(ClientLocation.class);
		ParseObject.registerSubclass(CircuitUnit.class);
		ParseObject.registerSubclass(CircuitStarted.class);
		ParseObject.registerSubclass(ChecklistCircuitStarting.class);
		ParseObject.registerSubclass(ChecklistCircuitEnding.class);
		ParseObject.registerSubclass(DistrictWatch.class);
		ParseObject.registerSubclass(DistrictWatchStarted.class);
		ParseObject.registerSubclass(DistrictWatchUnit.class);
		ParseObject.registerSubclass(DistrictWatchClient.class);
		ParseObject.registerSubclass(EventLog.class);
		ParseObject.registerSubclass(EventType.class);
		ParseObject.registerSubclass(EventRemark.class);
		ParseObject.registerSubclass(Guard.class);

		ParseObject.registerSubclass(LocationTracker.class);


		Parse.enableLocalDatastore(this);

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "DEVELOPMENT");
			Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
			Parse.initialize(this, ParseModule.DEVApplicationID,
					ParseModule.DEVClientKey);
		} else {
			Log.d(TAG, "RELEASE");
			Parse.initialize(this, ParseModule.ApplicationID,
					ParseModule.ClientKey);
			Log.d(TAG, "Parse initialized");
		}

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


}
