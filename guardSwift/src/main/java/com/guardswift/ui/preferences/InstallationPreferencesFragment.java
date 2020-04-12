package com.guardswift.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.data.Installation;
import com.guardswift.util.ToastHelper;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Objects;

import static com.mikepenz.iconics.Iconics.TAG;

// https://github.com/Gericop/Android-Support-Preference-V7-Fix
public class InstallationPreferencesFragment extends PreferenceFragmentCompat {

    private static final String PREF_DEVICE_NAME = "installation_name";
    private static final String PREF_DEVICE_MOBILE_NUMBER = "installation_mobile_number";

    Installation installation;

    SharedPreferences pref;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(PREF_DEVICE_NAME)) {
                installation.setName(prefs.getString(key, ""));
            }
            if (key.equals(PREF_DEVICE_MOBILE_NUMBER)) {

                // TODO: cleanup and make shareable
                String mobile = prefs.getString(key, "").replaceAll(" ", "").trim();
                if (mobile.isEmpty()) {
                    installation.setEmptyMobileNumber();
                    pref.edit().putString(PREF_DEVICE_MOBILE_NUMBER, "").apply();
                } else {
                    // TODO: Hardcoded danish country code
                    if (!mobile.startsWith("+45")) {
                        pref.edit().putString(PREF_DEVICE_MOBILE_NUMBER, "+45" + mobile).apply();
                        installation.setMobileNumber("45", mobile);
                    } else if (mobile.length() != 11) {
                        ToastHelper.toast(getContext(), getContext().getString(R.string.invalid_mobile_number));
                        pref.edit().putString(PREF_DEVICE_MOBILE_NUMBER, "").apply();
                        return;
                    } else {
                        installation.setMobileNumber(mobile);
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
        installation.getInstance().saveInBackground(e -> {
            if (e != null) {
                ToastHelper.toast(getContext(), Objects.requireNonNull(getContext()).getString(R.string.error_saving_changes_try_again_later));
                new HandleException(TAG, "Saving changes", e);
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
        installation = new Installation();
        pref.edit()
                .putString(PREF_DEVICE_NAME, installation.getName())
                .putString(PREF_DEVICE_MOBILE_NUMBER, installation.getMobileNumber())
                .apply();

    }


    private void update() {
        EditTextPreference deviceName = findPreference(PREF_DEVICE_NAME);
        EditTextPreference smsTo = findPreference(PREF_DEVICE_MOBILE_NUMBER);

        PreferenceHelper.setPreferenceSummary(pref, deviceName, PREF_DEVICE_NAME, getString(R.string.click_here_to_enter, getString(R.string.device_name)).toLowerCase());
        PreferenceHelper.setPreferenceSummary(pref, smsTo, PREF_DEVICE_MOBILE_NUMBER, getString(R.string.click_here_to_enter, getString(R.string.mobile_number).toLowerCase()), getString(R.string.installation_send_to_description));
    }


}
