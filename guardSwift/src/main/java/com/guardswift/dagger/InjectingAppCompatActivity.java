/*
 * Copyright (c) 2014 Fizz Buzz LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2013 Fizz Buzz LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guardswift.dagger;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.events.MissingInternetEvent;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.ui.activity.RFIDRegisterActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.Analytics;
import com.guardswift.util.Device;
import com.parse.ParseAnalytics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;

/**
 * Manages an ObjectGraph on behalf of a FragmentActivity. This graph is created
 * by extending the application-scope graph with FragmentActivity-specific
 * module(s).
 */
public class InjectingAppCompatActivity extends AppCompatActivity implements
        Injector {

    private static final String TAG = InjectingAppCompatActivity.class
            .getSimpleName();

    @Inject
    Device device;
    @Inject
    EventBus eventBus;
    @Inject
    GuardCache guardCache;

    private ObjectGraph mObjectGraph;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    /**
     * Gets this FragmentActivity's object graph.
     *
     * @return the object graph
     */
    @Override
    public final ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    /**
     * Injects a target object using this FragmentActivity's object graph.
     *
     * @param target the target object
     */
    @Override
    public void inject(Object target) {
        mObjectGraph.inject(target);
    }

    // implement Injector interface

    /**
     * Creates an object graph for this FragmentActivity by extending the
     * application-scope object graph with the modules returned by
     * {@link #getModules()}.
     * <p/>
     * Injects this FragmentActivity using the created graph.
     */
    @Override
    @CallSuper
    protected void onCreate(android.os.Bundle savedInstanceState) {
        // expand the application graph with the activity-specific module(s)
        ObjectGraph appGraph = ((Injector) getApplication()).getObjectGraph();
        List<Object> activityModules = getModules();
        mObjectGraph = appGraph.plus(activityModules.toArray());

        // now we can inject ourselves
        inject(this);

        if (!BuildConfig.DEBUG)
            ParseAnalytics.trackAppOpenedInBackground(getIntent());

        // note: we do the graph setup and injection before calling
        // super.onCreate so that InjectingFragments
        // associated with this InjectingActivity can do their graph setup and
        // injection in their
        // onAttach override.

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, RFIDRegisterActivity.class), 0);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mAdapter == null) {
            // TODO show warning that device cannot read RFID tags
        }

        super.onCreate(savedInstanceState);


    }


    @Override
    @CallSuper
    protected void onResume() {
        eventBus.register(this);

//        registerReceivers();

        super.onResume();
    }

    @Override
    @CallSuper
    protected void onPause() {
        eventBus.unregister(this);

//        unregisterReceivers();

        super.onPause();
    }


    private MaterialDialog mDialog;

    private void registerReceivers() {
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showRFIDNotActiveDialog();
            } else {
                dismissRFIDNotActiveDialog();
                mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            }
        }
    }

    private void showRFIDNotActiveDialog() {
        if (mDialog == null) {
            mDialog = new CommonDialogsBuilder.MaterialDialogs(this).ok(R.string.nfc_disabled, getString(R.string.nfc_enable_description), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivityForResult(intent, 0);
                }
            }).build();

            mDialog.show();
        }
    }

    private void dismissRFIDNotActiveDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void unregisterReceivers() {
    }



    @Override
    @CallSuper
    protected void onStart() {
        try {
            super.onStart();

            Analytics.sendScreenName(getClass().getSimpleName());

            GoogleAnalytics.getInstance(this).reportActivityStart(this);
        } catch (Exception e) {
            new HandleException(TAG, "onStart", e);
        }
    }

    @Override
    @CallSuper
    protected void onStop() {
        try {
            super.onStop();
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        } catch (Exception e) {
            new HandleException(TAG, "onStop", e);
        }
    }

    // workaround from http://stackoverflow.com/questions/13418436/android-4-2-back-stack-behaviour-with-nested-fragments
//    @Override
//    public void onBackPressed() {
//        // if there is a fragment and the back stack of this fragment is not empty,
//        // then emulate 'onBackPressed' behaviour, because in default, it is not working
//        FragmentManager fm = getSupportFragmentManager();
//        if (fm != null && fm.getFragments() != null) {
//            for (Fragment frag : fm.getFragments()) {
//                if (frag != null && frag.isVisible()) {
//                    FragmentManager childFm = frag.getChildFragmentManager();
//                    if (childFm.getBackStackEntryCount() > 0) {
//                        childFm.popBackStack();
//                        return;
//                    }
//                }
//            }
//        }
//        super.onBackPressed();
//    }



    public void onEventMainThread(MissingInternetEvent ev) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.title_internet_missing))
                .setContentText(getString(R.string.message_no_internet_connection))
                .show();
    }


    @Override
    @CallSuper
    protected void onPostResume() {
        super.onPostResume();


    }



    @Override
    @CallSuper
    protected void onDestroy() {
        // Eagerly clear the reference to the activity graph to allow it to be
        // garbage collected as
        // soon as possible.
        mObjectGraph = null;

        unregisterReceivers();

        super.onDestroy();
    }



    /**
     * Returns the list of dagger modules to be included in this
     * FragmentActivity's object graph. Subclasses that override this method
     * should addUnique to the list returned by super.getModules().
     *
     * @return the list of modules
     */
    protected List<Object> getModules() {
        List<Object> result = new ArrayList<Object>();
        result.add(new InjectingActivityModule(this, this));
        return result;
    }
}
