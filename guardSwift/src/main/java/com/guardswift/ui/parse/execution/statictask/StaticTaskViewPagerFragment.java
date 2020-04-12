package com.guardswift.ui.parse.execution.statictask;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;

import java.util.Map;

import javax.inject.Inject;

public class StaticTaskViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = StaticTaskViewPagerFragment.class
            .getSimpleName();


    public static StaticTaskViewPagerFragment newInstance() {
        StaticTaskViewPagerFragment fragment = new StaticTaskViewPagerFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ClientCache clientCache;
    @Inject
    GuardCache guardCache;



    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public StaticTaskViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_pending), PendingStaticTasksFragment.newInstance());
        fragmentMap.put(getString(R.string.title_active), ActiveStaticTasksFragment.newInstance());
        fragmentMap.put(getString(R.string.title_finished), FinishedStaticTasksFragment.newInstance());

        super.onCreate(savedInstanceState);
    }



    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

