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

import com.guardswift.ui.map.BaseMapFragment;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.data.client.details.ClientDataFragment;
import com.guardswift.ui.parse.data.client.details.ClientDetailsViewpagerFragment;
import com.guardswift.ui.parse.data.guard.GuardListFragment;
import com.guardswift.ui.parse.data.guard.GuardsMapFragment;
import com.guardswift.ui.parse.data.tracker.TrackerListFragment;
import com.guardswift.ui.parse.data.tracker.TrackerMapFragment;
import com.guardswift.ui.parse.documentation.eventlog.AbstractEventFragment;
import com.guardswift.ui.parse.documentation.eventlog.RegularEventFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventLocationFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventPeopleFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventRemarkFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventSummaryFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventTypeFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventViewPagerFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditListFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditViewPagerFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportSummaryFragment;
import com.guardswift.ui.parse.documentation.report.view.ReportHistoryListFragment;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.guardswift.ui.parse.execution.alarm.ActiveAlarmsFragment;
import com.guardswift.ui.parse.execution.alarm.AlarmsViewPagerFragment;
import com.guardswift.ui.parse.execution.alarm.FinishedAlarmsFragment;
import com.guardswift.ui.parse.execution.regular.ActiveRegularTasksFragment;
import com.guardswift.ui.parse.execution.regular.FinishedRegularTasksFragment;
import com.guardswift.ui.parse.execution.regular.RegularTaskViewPagerFragment;
import com.guardswift.ui.parse.execution.statictask.ActiveStaticTasksFragment;
import com.guardswift.ui.parse.execution.statictask.FinishedStaticTasksFragment;
import com.guardswift.ui.parse.execution.statictask.PendingStaticTasksFragment;
import com.guardswift.ui.parse.execution.statictask.StaticTaskViewPagerFragment;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The dagger module associated with {@link InjectingFragment} and
 * {@link InjectingListFragment}.
 */
@Module(addsTo = InjectingActivityModule.class, injects = {
		// Fragment abstraction
		AbstractTabsViewPagerFragment.class,
		// Parse
		AbstractParseRecyclerFragment.class,
		AbstractTasksRecycleFragment.class,
		// - Data
		BaseMapFragment.class,
		GuardListFragment.class,
		GuardsMapFragment.class,
		// -- Client
		ClientDetailsViewpagerFragment.class,
		ClientListFragment.class,
		ClientDataFragment.class,
		TrackerListFragment.class,
		TrackerMapFragment.class,
		// - Execution
		AbstractTasksRecycleFragment.class,
		// -- Alarm
		AlarmsViewPagerFragment.class,
		ActiveAlarmsFragment.class,
		FinishedAlarmsFragment.class,
		// -- Static
		StaticTaskViewPagerFragment.class,
		PendingStaticTasksFragment.class,
		ActiveStaticTasksFragment.class,
		FinishedStaticTasksFragment.class,
		// -- Regular
		RegularTaskViewPagerFragment.class,
        ActiveRegularTasksFragment.class,
		FinishedRegularTasksFragment.class,
		// Add event
		AddEventViewPagerFragment.class,
		AddEventTypeFragment.class,
		AddEventPeopleFragment.class,
		AddEventLocationFragment.class,
		AddEventRemarkFragment.class,
		AddEventSummaryFragment.class,
		AbstractEventFragment.class,
		RegularEventFragment.class,
		// Report
		ReportEditViewPagerFragment.class,
		ReportEditListFragment.class,
		ReportSummaryFragment.class,
		ReportHistoryListFragment.class
		}, library = true)
public class InjectingFragmentModule {
	private final android.support.v4.app.Fragment mFragment;
	private final Injector mInjector;

	/**
	 * Class constructor.
	 *
	 * @param fragment
	 *            the Fragment with which this module is associated.
	 */
	public InjectingFragmentModule(android.support.v4.app.Fragment fragment,
			Injector injector) {
		mFragment = fragment;
		mInjector = injector;
	}



	/**
	 * Provides the Fragment
	 * 
	 * @return the Fragment
	 */
	@Provides
	public android.support.v4.app.Fragment provideFragment() {
		return mFragment;
	}

	/**
	 * Provides the Injector for the Fragment-scope graph
	 * 
	 * @return the Injector
	 */
	@Provides
	@Fragment
	public Injector provideFragmentInjector() {
		return mInjector;
	}

	/**
	 * Defines an qualifier annotation which can be used in conjunction with a
	 * type to identify dependencies within the object graph.
	 * 
	 * @see <a href="http://square.github.io/dagger/">the dagger
	 *      documentation</a>
	 */
	@Qualifier
	@Target({ FIELD, PARAMETER, METHOD })
	@Documented
	@Retention(RUNTIME)
	public @interface Fragment {
	}
}
