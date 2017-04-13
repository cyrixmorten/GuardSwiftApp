package com.guardswift.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.data.Installation;
import com.guardswift.util.ToastHelper;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import static com.mikepenz.iconics.Iconics.TAG;

// https://github.com/Gericop/Android-Support-Preference-V7-Fix
public class InstallationPreferencesFragment extends PreferenceFragmentCompat {

    private static final String PREF_DEVICE_NAME = "installation_name";
    private static final String PREF_DEVICE_MOBILE_NUMBER = "installation_mobile_number";

    ParseInstallation installation;
    SharedPreferences pref;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(PREF_DEVICE_NAME)) {
                installation.put(Installation.NAME, prefs.getString(key, ""));
            }
            if (key.equals(PREF_DEVICE_MOBILE_NUMBER)) {

                // TODO: cleanup and make shareable
                String mobile = prefs.getString(key, "").replaceAll(" ", "").trim();
                if (mobile.isEmpty()) {
                    installation.put(Installation.MOBILE_NUMBER, mobile);
                    pref.edit().putString(PREF_DEVICE_MOBILE_NUMBER, "").apply();
                } else {
                    // TODO: Hardcoded danish country code
                    if (!mobile.startsWith("+45")) {
                        mobile = "+45" + mobile;
                        pref.edit().putString(PREF_DEVICE_MOBILE_NUMBER, mobile).apply();
                    } else if (mobile.length() != 11) {
                        ToastHelper.toast(getContext(), getContext().getString(R.string.invalid_mobile_number));
                        pref.edit().putString(PREF_DEVICE_MOBILE_NUMBER, "").apply();
                        return;
                    } else {
                        installation.put(Installation.MOBILE_NUMBER, mobile);
                    }
                }
            }

            update();
            saveChange();
        }
    };

    public static InstallationPreferencesFragment newInstance() {
        return new InstallationPreferencesFragment();
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
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ToastHelper.toast(getContext(), getContext().getString(R.string.error_saving_changes_try_again_later));
                    new HandleException(TAG, "Saving changes", e);
                }
            }
        });
    }


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        initPreferences();

        setPreferencesFromResource(R.xml.preferences_installation, rootKey);
    }


    private void initPreferences() {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        installation = ParseInstallation.getCurrentInstallation();
        pref.edit()
                .putString(PREF_DEVICE_NAME, installation.getString(Installation.NAME))
                .putString(PREF_DEVICE_MOBILE_NUMBER, installation.getString(Installation.MOBILE_NUMBER))
                .apply();

    }


    private void update() {
        EditTextPreference deviceName = (EditTextPreference) findPreference(PREF_DEVICE_NAME);
        EditTextPreference smsTo = (EditTextPreference) findPreference(PREF_DEVICE_MOBILE_NUMBER);

        PreferenceHelper.setPreferenceSummary(pref, deviceName, PREF_DEVICE_NAME, getString(R.string.click_here_to_enter, getString(R.string.device_name)).toLowerCase());
        PreferenceHelper.setPreferenceSummary(pref, smsTo, PREF_DEVICE_MOBILE_NUMBER, getString(R.string.click_here_to_enter, getString(R.string.mobile_number).toLowerCase()), getString(R.string.installation_send_to_description));
    }


}
