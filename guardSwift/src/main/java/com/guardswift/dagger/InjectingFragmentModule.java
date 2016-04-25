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

import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.documentation.report.view.ReportHistoryListFragment;
import com.guardswift.ui.parse.data.guard.GuardListFragment;
import com.guardswift.ui.parse.documentation.eventlog.AbstractEventFragment;
import com.guardswift.ui.parse.documentation.eventlog.CircuitUnitEventFragment;
import com.guardswift.ui.parse.documentation.eventlog.DistrictWatchClientEventFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventLocationFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventPeopleFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventRemarkFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventSummaryFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventTypeFragment;
import com.guardswift.ui.parse.documentation.report.create.fragment.AddEventViewPagerFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditListFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportEditViewPagerFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportSuggestionsListFragment;
import com.guardswift.ui.parse.documentation.report.edit.ReportSummaryFragment;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.guardswift.ui.parse.execution.circuit.ActiveCircuitUnitsFragment;
import com.guardswift.ui.parse.execution.circuit.CircuitUnitCheckpointsFragment;
import com.guardswift.ui.parse.execution.circuit.CircuitUnitDescriptionWebViewFragment;
import com.guardswift.ui.parse.execution.circuit.CircuitViewPagerFragment;
import com.guardswift.ui.parse.execution.circuit.FinishedCircuitUnitsFragment;
import com.guardswift.ui.parse.execution.districtwatch.ActiveDistrictWatchClientsFragment;
import com.guardswift.ui.parse.execution.districtwatch.DistrictwatchViewPagerFragment;
import com.guardswift.ui.parse.execution.districtwatch.FinishedDistrictWatchClientsFragment;
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
		GuardListFragment.class,
		ClientListFragment.class,
		// - Execution
		// -- StaticTask
		StaticTaskViewPagerFragment.class,
		PendingStaticTasksFragment.class,
		ActiveStaticTasksFragment.class,
		FinishedStaticTasksFragment.class,
		// -- CircuitUnit
        ActiveCircuitUnitsFragment.class,
		FinishedCircuitUnitsFragment.class,
		// -- DistrictWatch
		DistrictwatchViewPagerFragment.class,
		ActiveDistrictWatchClientsFragment.class,
		FinishedDistrictWatchClientsFragment.class,
		// Add event
		AddEventViewPagerFragment.class,
		AddEventTypeFragment.class,
		AddEventPeopleFragment.class,
		AddEventLocationFragment.class,
		AddEventRemarkFragment.class,
		AddEventSummaryFragment.class,
		AbstractEventFragment.class,
		CircuitUnitEventFragment.class, DistrictWatchClientEventFragment.class,
		CircuitViewPagerFragment.class, AbstractTasksRecycleFragment.class,
		CircuitUnitCheckpointsFragment.class,
		CircuitUnitDescriptionWebViewFragment.class,
		// Report
		ReportEditViewPagerFragment.class,
		ReportEditListFragment.class,
		ReportSuggestionsListFragment.class,
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
