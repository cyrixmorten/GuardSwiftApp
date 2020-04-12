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

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Analytics;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;

/**
 * Manages an ObjectGraph on behalf of an ListFragment. This graph is created by
 * extending the hosting Activity's graph with Fragment-specific module(s).
 */
public class InjectingListFragment extends ListFragment implements Injector {

	@Inject EventBus eventBus;

	@Inject Analytics mAnalytics;

	private ObjectGraph mObjectGraph;
	private boolean mFirstAttach = true;

	/**
	 * Creates an object graph for this Fragment by extending the hosting
	 * Activity's object graph with the modules returned by
	 * {@link #getModules()}.
	 * <p/>
	 * Injects this Fragment using the created graph.
	 */
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		// expand the activity graph with the fragment-specific module(s)
		ObjectGraph appGraph = ((Injector) activity).getObjectGraph();
		List<Object> fragmentModules = getModules();
		mObjectGraph = appGraph.plus(fragmentModules.toArray());

		// make sure it's the first time through; we don't want to re-inject a
		// retained fragment that is going
		// through a detach/attach sequence.
		if (mFirstAttach) {
			inject(this);
			mFirstAttach = false;
		}

		eventBus.register(this);
	}

	@Override public void onDestroyView() {
		super.onDestroyView();
		RefWatcher refWatcher = GuardSwiftApplication.getRefWatcher(getActivity());
		refWatcher.watch(this);
	}

	// subclasses create their own method if needed
	public void onEventMainThread(UpdateUIEvent ev) {
		// to make EventBus happy
	}

	@Override
	public void onDetach() {
		eventBus.unregister(this);
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		// Eagerly clear the reference to the fragment graph to allow it to be
		// garbage collected as
		// soon as possible.
		mObjectGraph = null;

		super.onDestroy();
	}

	/**
	 * Gets this ListFragment's object graph.
	 * 
	 * @return the object graph
	 */
	@Override
	public final ObjectGraph getObjectGraph() {
		return mObjectGraph;
	}

	/**
	 * Injects a target object using this ListFragment's object graph.
	 * 
	 * @param target
	 *            the target object
	 */
	@Override
	public void inject(Object target) {
		mObjectGraph.inject(target);
	}

	/**
	 * Returns the list of dagger modules to be included in this ListFragment's
	 * object graph. Subclasses that override this method should addUnique to the list
	 * returned by super.getModules().
	 * 
	 * @return the list of modules
	 */
	protected List<Object> getModules() {
		List<Object> result = new ArrayList<Object>();
		result.add(new InjectingFragmentModule(this, this));
		return result;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAnalytics.sendScreenName(getActivity(), getClass().getSimpleName());
	}

}
