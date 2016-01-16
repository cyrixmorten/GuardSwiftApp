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

import android.app.Service;
import android.content.Context;

import com.guardswift.core.ca.activity.ActivityRecognitionService;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.core.ca.location.FusedLocationTrackerService;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Manages an ObjectGraph on behalf of an Service. This graph is created by
 * extending the application-scope graph with Service-specific module(s).
 */
public abstract class InjectingService extends Service implements Injector {

    private Context mContext;
    private ObjectGraph mObjectGraph;

    /**
     * Creates an object graph for this Service by extending the
     * application-scope object graph with the modules returned by
     * {@link #getModules()}.
     * <p/>
     * Injects this Service using the created graph.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // extend the application-scope object graph with the modules for this
        // service
        mObjectGraph = ((Injector) getApplication()).getObjectGraph().plus(
                getModules().toArray());

        // then inject ourselves
        mObjectGraph.inject(this);
    }

    /**
     * Gets this Service's object graph.
     *
     * @return the object graph
     */
    @Override
    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    /**
     * Injects a target object using this Service's object graph.
     *
     * @param target the target object
     */
    @Override
    public void inject(Object target) {
        mObjectGraph.inject(target);
    }

    /**
     * Returns the list of dagger modules to be included in this Service's
     * object graph. Subclasses that override this method should addUnique to the list
     * returned by super.getModules().
     *
     * @return the list of modules
     */
    protected List<Object> getModules() {
        List<Object> result = new ArrayList<Object>();
        result.add(new InjectingServiceModule(this, this));
        return result;
    }

    /**
     * The dagger module associated with {@link InjectingService}.
     */
    @Module(addsTo = InjectingApplication.InjectingApplicationModule.class, injects = {ActivityRecognitionService.class, FusedLocationTrackerService.class, WiFiPositioningService.class}, library = true)
    public static class InjectingServiceModule {
        private final android.app.Service mService;
        private final Injector mInjector;

        /**
         * Class constructor.
         *
         * @param service the Service with which this module is associated.
         */
        public InjectingServiceModule(android.app.Service service,
                                      Injector injector) {
            mService = service;
            mInjector = injector;
        }

//        @Provides
//        EventBus provideEventBus() {
//            return EventBus.getDefault();
//        }

        /**
         * Provides the Application Context
         *
         * @return the Application Context
         */
//        @Provides
//        @Singleton
//        @InjectingApplication.InjectingApplicationModule.ForApplication
//        public Context provideApplicationContext() {
//            return mService.getApplicationContext();
//        }

        @Provides
        @Singleton
        public android.app.Service provideService() {
            return mService;
        }

        @Provides
        @Singleton
        @Service
        public Injector provideServiceInjector() {
            return mInjector;
        }

        @Qualifier
        @Target({FIELD, PARAMETER, METHOD})
        @Documented
        @Retention(RUNTIME)
        public @interface Service {
        }
    }
}