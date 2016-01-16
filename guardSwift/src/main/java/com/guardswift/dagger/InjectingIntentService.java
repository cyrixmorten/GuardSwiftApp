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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule;
import com.guardswift.core.tasks.alarm.AlarmDownloaderIntentService;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;

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
 * Manages an ObjectGraph on behalf of a BroadcastReceiver. This graph is
 * created by extending the application-scope graph with
 * BroadcastReceiver-specific module(s).
 */
public class InjectingIntentService extends IntentService implements
		Injector {

	private Context mContext;
	private ObjectGraph mObjectGraph;

	/**
	 *
	 * Required default constructor
	 *
	 * @param name
	 */
	public InjectingIntentService(String name) {
		super(name);
	}

	/**
	 * Creates an object graph for this IntentService by extending the
	 * application-scope object graph with the modules returned by
	 * {@link #getModules()}.
	 * <p/>
	 * Injects this IntentService using the created graph.
	 */

	@Override
	protected void onHandleIntent(Intent intent) {
		mContext = getApplicationContext();

		// extend the application-scope object graph with the modules for this
		// broadcast receiver
		mObjectGraph = ((Injector) mContext.getApplicationContext())
				.getObjectGraph().plus(getModules().toArray());

		// then inject ourselves
		mObjectGraph.inject(this);
	}

	/**
	 * Gets this IntentService's object graph.
	 * 
	 * @return the object graph
	 */
	@Override
	public ObjectGraph getObjectGraph() {
		return mObjectGraph;
	}

	/**
	 * Injects a target object using this IntentService's object graph.
	 * 
	 * @param target
	 *            the target object
	 */
	@Override
	public void inject(Object target) {
		mObjectGraph.inject(target);
	}

	/**
	 * Returns the list of dagger modules to be included in this
	 * IntentService's object graph. Subclasses that override this method
	 * should addUnique to the list returned by super.getModules().
	 * 
	 * @return the list of modules
	 */
	protected List<Object> getModules() {
		List<Object> result = new ArrayList<Object>();
		result.add(new InjectingIntentServiceModule(mContext, this, this));
		return result;
	}



	/**
	 * The dagger module associated with {@link InjectingIntentService}
	 */
	@Module(addsTo = InjectingApplicationModule.class, injects = {AlarmDownloaderIntentService.class, RegisterGeofencesIntentService.class}, library = true)
	public static class InjectingIntentServiceModule {
		Context mContext;
		android.app.IntentService mIntentService;
		Injector mInjector;

		/**
		 * Class constructor.
		 * 
		 * @param mIntentService
		 *            the InjectingIntentService with which this module is
		 *            associated.
		 */
		public InjectingIntentServiceModule(Context context,
											android.app.IntentService mIntentService, Injector injector) {
			mContext = context;
			this.mIntentService = mIntentService;
			mInjector = injector;
		}

		/**
		 * Provides the Context for the IntentService associated with this
		 * graph.
		 * 
		 * @return the IntentService Context
		 */
		@Provides
		@Singleton
		@IntentService
		public Context provideIntentServiceContext() {
			return mContext;
		}

		/**
		 * Provides the IntentService
		 * 
		 * @return the IntentService
		 */
		@Provides
		@Singleton
		public android.app.IntentService provideIntentService() {
			return mIntentService;
		}

		/**
		 * Provides the Injector for the IntentService-scope graph
		 * 
		 * @return the Injector
		 */
		@Provides
		@Singleton
		@IntentService
		public Injector provideIntentServiceInjector() {
			return mInjector;
		}

		/**
		 * Defines a qualifier annotation which can be used in conjunction with
		 * a type to identify dependencies within the object graph.
		 * 
		 * @see <a href="http://square.github.io/dagger/">the dagger
		 *      documentation</a>
		 */
		@Qualifier
		@Target({ FIELD, PARAMETER, METHOD })
		@Documented
		@Retention(RUNTIME)
		public @interface IntentService {
		}
	}
}