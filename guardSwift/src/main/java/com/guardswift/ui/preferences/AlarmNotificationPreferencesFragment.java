package com.guardswift.ui.preferences;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.rest.GuardSwiftServer;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.ToastHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.takisoft.fix.support.v7.preference.PreferenceCategory;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// https://github.com/Gericop/Android-Support-Preference-V7-Fix
public class AlarmNotificationPreferencesFragment extends PreferenceFragmentCompat {

    private static String TAG = AlarmNotificationPreferencesFragment.class.getSimpleName();

    public static AlarmNotificationPreferencesFragment newInstance() {
        return new AlarmNotificationPreferencesFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private Dialog loadingDialog;

    private void showLoading() {
        dismissLoading();
        loadingDialog = new CommonDialogsBuilder.MaterialDialogs(getContext()).indeterminate().build();
        loadingDialog.show();
    }

    private void dismissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_alarm, rootKey);

        createTestAlarmPreference();
        createAlarmNotificationPreferences();
    }

    public void createTestAlarmPreference() {


        final Preference testAlarm = new Preference(getContext());
        testAlarm.setTitle(R.string.test_alarm_create);
        testAlarm.setIcon(R.drawable.ic_add_alert_black_24dp);

        testAlarm.setEnabled(false);

        final String[] sendTo = {""};
        final List<String> sendToList = ParseUser.getCurrentUser().getList("sendTo");

        if (sendToList != null && !sendToList.isEmpty()) {
            sendTo[0] = sendToList.get(0).replace("+", "");

            testAlarm.setEnabled(true);
        }


        testAlarm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                testAlarm.setEnabled(false);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(GuardSwiftServer.API_URL)
                        .build();

                GuardSwiftServer.API guardSwift = retrofit.create(GuardSwiftServer.API.class);

                showLoading();

                guardSwift.testAlarm(sendTo[0]).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            ToastHelper.toast(getContext(), getString(R.string.test_alarm_created));
                        } else {
                            ToastHelper.toast(getContext(), getString(R.string.error_an_error_occured));
                        }

                        dismissLoading();
                        testAlarm.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        ToastHelper.toast(getContext(), getString(R.string.title_internet_missing));

                        dismissLoading();
                        testAlarm.setEnabled(true);
                    }
                });

                return false;
            }
        });

        PreferenceCategory cat = (PreferenceCategory)findPreference("alarm_test");
        cat.addPreference(testAlarm);
    }

    private void createAlarmNotificationPreferences() {
        new Guard.QueryBuilder(false)
                .hasSession()
                .build()
                .findInBackground(new FindCallback<Guard>() {

            @Override
            public void done(List<Guard> guards, ParseException e) {

                ((PreferenceCategory)findPreference("alarm_notifications")).removeAll();

                for (Guard guard: guards) {
                    if (guard.canAccessAlarms()) {
                        createAlarmNotifyPreference(guards, guard);
                    }
                }
            }
        });
    }

    public void createAlarmNotifyPreference(final List<Guard> guards, final Guard guard) {

        PreferenceCategory alarmNotifications = (PreferenceCategory)findPreference("alarm_notifications");

        final CheckBoxPreference guardNotification = new CheckBoxPreference(getContext());
        guardNotification.setTitle(guard.getName());
        guardNotification.setIcon(R.drawable.ic_alarm_black_24dp);
        guardNotification.setChecked(guard.isAlarmSoundEnabled());
        guardNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {


                final Boolean enable = (Boolean)newValue;

                Guard.getQueryBuilder(true)
                        .build().findInBackground(new FindCallback<Guard>() {
                    @Override
                    public void done(List<Guard> objects, ParseException e) {
                        int alarmNotifyCount = 0;
                        for (Guard g: guards) {
                            alarmNotifyCount += (g.isAlarmSoundEnabled()) ? 1 : 0;
                        }


                        if (!enable && alarmNotifyCount == 1) {
                            // do not allow 0 alarm receivers
                            new CommonDialogsBuilder.MaterialDialogs(getContext()).ok(R.string.not_performed, R.string.last_alarm_receiver_message, null).show();

                            guardNotification.setChecked(true);
                        } else {
                            guard.enableAlarmSound(enable);
                            guard.enableAlarmSMS(enable);
                            guard.pinThenSaveEventually();

                            guardNotification.setChecked(enable);

                            // if current guard, update last active entry
                            if (guard.equals(GuardSwiftApplication.getLoggedIn())) {
                                GuardSwiftApplication.saveCurrentGuardAsLastActive();
                            }
                        }
                    }
                });



                return false;
            }
        });

        alarmNotifications.addPreference(guardNotification);
    }

}
