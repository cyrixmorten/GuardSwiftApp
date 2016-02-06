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

import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.eventbus.events.MissingInternetEvent;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.util.Analytics;
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
    EventBus eventBus;
    @Inject
    GuardCache guardCache;

    private ObjectGraph mObjectGraph;

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

        registerReceivers();

        super.onCreate(savedInstanceState);


    }



    private void registerReceivers() {
    }

    private void unregisterReceivers() {
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();

        Analytics.sendScreenName(getClass().getSimpleName());

        GoogleAnalytics.getInstance(this).reportActivityStart(this);

    }

    @Override
    @CallSuper
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    // workaround from http://stackoverflow.com/questions/13418436/android-4-2-back-stack-behaviour-with-nested-fragments
    @Override
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        if (fm != null && fm.getFragments() != null) {
            for (Fragment frag : fm.getFragments()) {
                if (frag != null && frag.isVisible()) {
                    FragmentManager childFm = frag.getChildFragmentManager();
                    if (childFm.getBackStackEntryCount() > 0) {
                        childFm.popBackStack();
                        return;
                    }
                }
            }
        }
        super.onBackPressed();
    }

//    public void onEventMainThread(PowerConnected ev) {
//        wakeScreen.screenWakeup(true);
//    }
//
//    public void onEventMainThread(PowerDisconnected ev) {
//        wakeScreen.screenRelease();
//    }

    public void onEventMainThread(MissingInternetEvent ev) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.title_internet_missing))
                .setContentText(getString(R.string.message_no_internet_connection))
                .show();
    }

    @Override
    @CallSuper
    protected void onResume() {
//        wakeScreen.wakeIfPowerConnected();
        eventBus.register(this);

        super.onResume();
    }

    @Override
    @CallSuper
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    @CallSuper
    protected void onPostResume() {
        super.onPostResume();

//        if (!guardCache.isLoggedIn())
//            return;

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!alarmCache.isDialogShowing()) {
//                    AlarmDialogActivity.show(InjectingAppCompatActivity.this);
//                }
//            }
//        }, 1000);

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
