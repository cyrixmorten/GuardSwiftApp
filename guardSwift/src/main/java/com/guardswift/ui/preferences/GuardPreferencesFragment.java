package com.guardswift.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

import com.guardswift.R;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.ToastHelper;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceCategory;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

// https://github.com/Gericop/Android-Support-Preference-V7-Fix
public class GuardPreferencesFragment extends PreferenceFragmentCompat {

    private static String GUARD_NAME = "guard_name";
    private static String GUARD_MOBILE_NUMBER = "guard_mobile_number";
    private static String ALARM_NOTIFY = "notify_on_incoming_alarms";
    private static String ALARM_SOUND = "sound_on_incoming_alarms";
    private static String ALARM_SMS = "sms_on_incoming_alarms";

    Guard guard;
    SharedPreferences pref;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(GUARD_NAME)) {
                guard.setName(prefs.getString(key, ""));
            }
            if (key.equals(GUARD_MOBILE_NUMBER)) {
                String mobile = prefs.getString(key, "");
                boolean invalid = false;
                if (!mobile.startsWith("+")) {
                    ToastHelper.toast(getContext(), getContext().getString(R.string.mobile_must_include_countrycode));
                    invalid = true;
                }
                if (mobile.length() < 11) {
                    ToastHelper.toast(getContext(), getContext().getString(R.string.mobile_number_too_short));
                    invalid = true;
                }

                if (!invalid) {
                    guard.setMobile(mobile);
                }
            }
            if (key.equals(ALARM_NOTIFY)) {
                guard.enableAlarmNotification(prefs.getBoolean(key, false));
            }
            if (key.equals(ALARM_SOUND)) {
                guard.enableAlarmSound(prefs.getBoolean(key, false));
            }
            if (key.equals(ALARM_SMS)) {
                guard.enableAlarmSMS(prefs.getBoolean(key, false));
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
        guard.pinThenSaveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                GuardSwiftApplication.saveCurrentGuardAsLastActive();
            }
        });
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
                .putString(GUARD_MOBILE_NUMBER, guard.getMobile())
                .putBoolean(ALARM_NOTIFY, guard.isAlarmNotificationsEnabled())
                .putBoolean(ALARM_SOUND, guard.isAlarmSoundEnabled())
                .putBoolean(ALARM_SMS, !guard.getMobile().isEmpty() && guard.isAlarmSMSEnabled())
                .apply();

    }

    private void update() {
        EditTextPreference guardName = (EditTextPreference) findPreference(GUARD_NAME);
        EditTextPreference guardMobile = (EditTextPreference) findPreference(GUARD_MOBILE_NUMBER);
        guardName.setTitle(pref.getString(GUARD_NAME, ""));
        guardMobile.setTitle(pref.getString(GUARD_MOBILE_NUMBER, ""));

        PreferenceCategory alarmNotifications = (PreferenceCategory)findPreference("alarm_notifications");
        if (GuardSwiftApplication.getLoggedIn().canAccessAlarms()) {
            CheckBoxPreference alarmNotify = (CheckBoxPreference) findPreference(ALARM_NOTIFY);
            CheckBoxPreference alarmSound = (CheckBoxPreference) findPreference(ALARM_SOUND);
            CheckBoxPreference alarmSMS = (CheckBoxPreference) findPreference(ALARM_SMS);
            alarmNotify.setChecked(pref.getBoolean(ALARM_NOTIFY, false));
            alarmSound.setChecked(pref.getBoolean(ALARM_SOUND, false));
            alarmSMS.setChecked(pref.getBoolean(ALARM_SMS, false));
        } else {
            alarmNotifications.removeAll();
            alarmNotifications.setVisible(false);
        }
    }


}
