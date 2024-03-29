package com.guardswift.ui.preferences;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import android.text.TextUtils;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.query.GuardQueryBuilder;
import com.guardswift.rest.GuardSwiftServer;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.ToastHelper;
import com.parse.ParseUser;
import com.takisoft.preferencex.PreferenceCategory;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.List;
import java.util.Objects;

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

    private void createTestAlarmPreference() {


        final Preference testAlarm = new Preference(Objects.requireNonNull(getContext()));
        testAlarm.setTitle(R.string.test_alarm_create);
        testAlarm.setIcon(R.drawable.ic_add_alert_black_24dp);

        testAlarm.setEnabled(false);

        final String[] sendTo = {""};
        final List<String> sendToList = ParseUser.getCurrentUser().getList("sendTo");

        if (sendToList != null && !sendToList.isEmpty()) {
            sendTo[0] = sendToList.get(0).replace("+", "");

            testAlarm.setEnabled(true);
        }


        testAlarm.setOnPreferenceClickListener(preference -> {

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
        });

        PreferenceCategory cat = findPreference("alarm_test");
        cat.addPreference(testAlarm);
    }

    private void createAlarmNotificationPreferences() {
        new GuardQueryBuilder(false)
                .hasSession()
                .build()
                .findInBackground((guards, e) -> {

                    if (e != null) {
                        new HandleException(TAG, "Fetching guards", e);
                        return;
                    }
                    if (guards == null) {
                        return;
                    }

                    ((PreferenceCategory) Objects.requireNonNull(findPreference("alarm_notifications"))).removeAll();

                    for (Guard guard: guards) {
                        if (guard.canAccessAlarms()) {
                            createAlarmNotifyPreference(guards, guard);
                        }
                    }
                });
    }

    private String getLastInstallationText(Guard guard) {
        String installationName = guard.getInstallationName();
        String installationText = "";

        installationText += getString(R.string.device);
        installationText += ": ";

        if (!TextUtils.isEmpty(installationName)) {
            installationText += installationName;
        } else {
            installationText += getString(R.string.not_named);
        }

        return installationText;
    }

    private String getAlarmSMSText(Guard guard) {
        return getString(R.string.mobile) + ": " + guard.getAlarmMobile();
    }

    private String getonlineText(Guard guard) {
        String isOnlineText = guard.isOnline() ? getString(R.string.yes) : getString(R.string.no);

        return getString(R.string.online) + ": " + isOnlineText;
    }

    public void createAlarmNotifyPreference(final List<Guard> guards, final Guard guard) {

        if (getContext() == null || isDetached()) {
            return;
        }

        PreferenceCategory alarmNotifications = findPreference("alarm_notifications");



        String description = getonlineText(guard) + "\n" + getLastInstallationText(guard) + "\n" + getAlarmSMSText(guard);

        final CheckBoxPreference guardNotification = new CheckBoxPreference(getContext());
        guardNotification.setTitle(guard.getName());
        guardNotification.setSummary(description.trim());
        guardNotification.setIcon(R.drawable.ic_alarm_black_24dp);
        guardNotification.setChecked(guard.isAlarmSoundEnabled());
        guardNotification.setOnPreferenceChangeListener((preference, newValue) -> {


            final Boolean enable = (Boolean)newValue;

//                Guard.getQueryBuilder(true)
//                        .build().findInBackground(new FindCallback<Guard>() {
//                    @Override
//                    public void done(List<Guard> objects, ParseException e) {
                    int alarmNotifyCount = 0;
                    for (Guard g: guards) {
                        alarmNotifyCount += (g.isAlarmSoundEnabled()) ? 1 : 0;
                    }


                    if (!enable && alarmNotifyCount == 1) {
                        // do not allow 0 alarm receivers
                        new CommonDialogsBuilder.MaterialDialogs(getContext()).ok(R.string.not_performed, R.string.last_alarm_receiver_message, null).show();

                        guardNotification.setChecked(true);
                    } else {
                        showLoading();

                        guard.enableAlarm(enable);
                        guard.saveInBackground(e -> {
                            if (isAdded()) {
                                if (e != null) {
                                    ToastHelper.toast(getContext(), getString(R.string.error_an_error_occured));
                                }

                                guardNotification.setChecked(enable);
                                dismissLoading();
                            }
                        });
                    }
//                    }
//                });



            return false;
        });

        Objects.requireNonNull(alarmNotifications).addPreference(guardNotification);
    }

}
