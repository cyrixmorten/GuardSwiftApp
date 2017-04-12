package com.guardswift.ui.preferences;

import android.content.SharedPreferences;
import android.support.v7.preference.Preference;

/**
 * Created by cyrixmorten on 12/04/2017.
 */

class PreferenceHelper {

    private static final String TAG = PreferenceHelper.class.getSimpleName();

    static void setPreferenceSummary(SharedPreferences sharedPreferences, Preference preference, String key, String emptyText) {
        setPreferenceSummary(sharedPreferences, preference, key, emptyText, null);
    }

    static void setPreferenceSummary(SharedPreferences sharedPreferences, Preference preference, String key, String emptyText, String description) {
        String summary = sharedPreferences.getString(key, "");
        if (summary.isEmpty()) {
            summary = emptyText;
        }

        description = (description != null && !description.isEmpty()) ? "\n\n" + description : "";

        preference.setSummary(summary + description);

    }


}
