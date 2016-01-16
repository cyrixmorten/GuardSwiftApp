package com.guardswift.util;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.guardswift.BuildConfig;
import com.guardswift.ui.GuardSwiftApplication;

/**
 * Created by cyrix on 3/29/15.
 */
public class Analytics {


    private static final String TAG = Analytics.class.getSimpleName();


    public static enum EventLabelGuess {Correct, Incorrect, Missed};
    public static enum EventLabelTask {Overview, Details};
    public static enum CreateEventlogAction {New, Copy, Edit, Filter, Autocomplete};

    public static enum EventAction {Arrival, Departure, CreateEvent};

    public static void sendScreenName(String screenName) {

        Log.i(TAG, "sendScreenName: " + screenName);

        Tracker t = GuardSwiftApplication.getInstance().getTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private static void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, 1);
    }

    private static void sendEvent(String category, String action, String label, long value) {

        if (BuildConfig.DEBUG)
            category = "Debug " + category;

        Log.i(TAG, "sendEvent cat:" + category + " action: " + action + " label: " + label);

        GuardSwiftApplication.getInstance().getTracker().send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());

    }

    public static void eventCheckpointAutomation(EventAction action, EventLabelGuess label) {
        sendEvent("Automation indoor", action.toString(), label.toString());
    }

    public static void eventTaskAutomation(EventAction action, EventLabelGuess label) {
        sendEvent("Automation outdoor", action.toString(), label.toString());
    }

    public static void eventTaskTrend(EventAction action, EventLabelTask label) {
        sendEvent("Task trends", action.toString(), label.toString());
    }

    public static void eventEventLogTrend(CreateEventlogAction action) {
        eventEventLogTrend(action, null, 1);
    }

    public static void eventEventLogTrend(CreateEventlogAction action, String label, long value) {
        sendEvent("Eventlog trends", action.toString(), label, value);
    }


}
