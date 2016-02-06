package com.guardswift.ui.parse.execution.circuit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;

import java.util.Map;

import javax.inject.Inject;

public class CircuitViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = CircuitViewPagerFragment.class
            .getSimpleName();


    public static CircuitViewPagerFragment newInstance(Context context, CircuitStarted circuitStarted) {

        Log.e(TAG, "SHOW: " + circuitStarted.getName());
        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getCircuitStartedCache()
                .setSelected(circuitStarted);

        CircuitViewPagerFragment fragment = new CircuitViewPagerFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    CircuitStartedCache circuitStartedCache;

    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public CircuitViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_tasks_new), ActiveCircuitUnitsFragment.newInstance(getContext(), circuitStartedCache.getSelected()));
        fragmentMap.put(getString(R.string.title_tasks_old), FinishedCircuitUnitsFragment.newInstance(getContext(), circuitStartedCache.getSelected()));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

