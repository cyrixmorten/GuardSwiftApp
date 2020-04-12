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

import android.app.ActionBar.TabListener;
import android.os.Handler;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuInflater;

import com.guardswift.ui.activity.RFIDRegisterActivity;
import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule;
import com.guardswift.ui.activity.AbstractToolbarActivity;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.activity.GuardLoginActivity;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;
import com.guardswift.ui.dialog.activity.AbstractDialogActivity;
import com.guardswift.ui.dialog.activity.AlarmDialogActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandlerActivity;
import com.guardswift.util.Analytics;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The dagger module associated with {@link InjectingAppCompatActivity},
 */
@Module(addsTo = InjectingApplicationModule.class, injects = {
        // Abstract
        AbstractToolbarActivity.class,
        // Login Guard
        GuardLoginActivity.class,
        // Main
        MainActivity.class,
        // Alarm
        AlarmDialogActivity.class,
        // Data
        GenericToolbarActivity.class,
        // Dialog Activities
        AbstractDialogActivity.class,
        // Create report
        ParseTaskCreateReportActivity.class,
        // Create Event
        UpdateEventHandlerActivity.class,
        CreateEventHandlerActivity.class,
        // RFID
        RFIDRegisterActivity.class}, library = true)
public class InjectingActivityModule {
    private final AppCompatActivity mActivity;
    private final Injector mInjector;

    /**
     * Class constructor.
     *
     * @param activity the Activity with which this module is associated.
     * @param injector the Injector for the Application-scope graph
     */
    public InjectingActivityModule(AppCompatActivity activity,
                                   Injector injector) {
        mActivity = activity;
        mInjector = injector;
    }

    /**
     * Provides the Activity Context
     *
     * @return the Activity Context
     */
    @Provides
    @Singleton
    @ForActivity
    public android.content.Context provideActivityContext() {
        return mActivity;
    }

    /**
     * Provides the Activity
     *
     * @return the Activity
     */
    @Provides
    public AppCompatActivity provideActivity() {
        return mActivity;
    }

    @Provides
    @Singleton
    ActionBar provideActionBar() {
        return mActivity.getSupportActionBar();
    }

    @Provides
    @Singleton
    MenuInflater provideMenuInflater() {
        return mActivity.getMenuInflater();
    }

    @Provides
    @Singleton
    LayoutInflater provideLayoutInflater() {
        return mActivity.getLayoutInflater();
    }

    @Provides
    @Singleton
    FragmentManager provideFragmentManager() {
        return mActivity.getSupportFragmentManager();
    }

    @Provides
    @Singleton
    TabListener provideTabListener() {
        if (mActivity instanceof TabListener) {
            return (TabListener) mActivity;
        }
        return null;
    }

    @Provides
    @Singleton
    Handler provideHandler() {
        return new Handler(mActivity.getMainLooper());
    }

    @Provides
    @Singleton
    Analytics provideAnalytics() {
        return new Analytics(mActivity);
    }

    /**
     * Provides the Injector for the Activity-scope graph
     *
     * @return the Injector
     */
    @Provides
    @ForActivity
    public Injector provideActivityInjector() {
        return mInjector;
    }

    /**
     * Defines an qualifier annotation which can be used in conjunction with a
     * type to identify dependencies within the object graph.
     *
     * @see <a href="http://square.github.io/dagger/">the dagger
     * documentation</a>
     */
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface ForActivity {
    }
}
