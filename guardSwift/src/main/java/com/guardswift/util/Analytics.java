package com.guardswift.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by cyrix on 3/29/15.
 */
public class Analytics {


    private static final String TAG = Analytics.class.getSimpleName();

    public enum UserProperty {COMPANY_NAME}

    private FirebaseAnalytics mFirebaseAnalytics;

    public Analytics(Activity context) {
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void sendScreenName(Activity activity, String screenName) {

        Log.i(TAG, "sendScreenName: " + screenName);

        this.mFirebaseAnalytics.setCurrentScreen(activity, screenName, null);
    }

    public void setUserProperty(UserProperty key, String value) {
        this.mFirebaseAnalytics.setUserProperty(key.toString().toLowerCase(), value);
    }


    public void sendEvent(String name, Bundle properties) {
        mFirebaseAnalytics.logEvent(name, properties);
    }


}
