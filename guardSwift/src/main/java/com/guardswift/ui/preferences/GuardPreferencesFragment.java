package com.guardswift.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.guardswift.R;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.ToastHelper;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

// https://github.com/Gericop/Android-Support-Preference-V7-Fix
public class GuardPreferencesFragment extends PreferenceFragmentCompat {

    private static String GUARD_NAME = "guard_name";
    private static String GUARD_MOBILE_NUMBER = "guard_mobile_number";

    Guard guard;
    SharedPreferences pref;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(GUARD_NAME)) {
                guard.setName(prefs.getString(key, ""));
            }
            if (key.equals(GUARD_MOBILE_NUMBER)) {

                // TODO: cleanup and make shareable
                String mobile = prefs.getString(key, "").replaceAll(" ", "").trim();
                if (mobile.isEmpty()) {
                    guard.setMobile(mobile);
                    pref.edit().putString(GUARD_MOBILE_NUMBER, "").apply();
                } else {
                    // TODO: Hardcoded danish country code
                    if (!mobile.startsWith("+45")) {
                        mobile = "+45" + mobile;
                        pref.edit().putString(GUARD_MOBILE_NUMBER, mobile).apply();
                    } else if (mobile.length() != 11) {
                        ToastHelper.toast(getContext(), getContext().getString(R.string.invalid_mobile_number));
                        pref.edit().putString(GUARD_MOBILE_NUMBER, "").apply();
                        return;
                    } else {
                        guard.setMobile(mobile);
                    }
                }
            }


            update();
            saveChange();
        }
    };

    public static GuardPreferencesFragment newInstance() {
        return new GuardPreferencesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        update();

        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();

        pref.unregisterOnSharedPreferenceChangeListener(listener);

    }

    private void saveChange() {
        guard.saveEventuallyAndNotify();
    }


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        initPreferences();

        setPreferencesFromResource(R.xml.preferences_guard, rootKey);
    }


    private void initPreferences() {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        guard = GuardSwiftApplication.getLoggedIn();
        pref.edit()
                .putString(GUARD_NAME, guard.getName())
                .putString(GUARD_MOBILE_NUMBER, guard.getMobileNumber())
                .apply();

    }

    private void update() {
        EditTextPreference guardName = findPreference(GUARD_NAME);
        EditTextPreference guardMobile = findPreference(GUARD_MOBILE_NUMBER);

        PreferenceHelper.setPreferenceSummary(pref, guardName, GUARD_NAME, getString(R.string.click_here_to_enter, getString(R.string.name)).toLowerCase());
        PreferenceHelper.setPreferenceSummary(pref, guardMobile, GUARD_MOBILE_NUMBER, getString(R.string.click_here_to_enter, getString(R.string.mobile_number).toLowerCase()), getString(R.string.guard_mobile_description));
    }


}
