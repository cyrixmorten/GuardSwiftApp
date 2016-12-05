package com.guardswift.ui.parse.execution.alarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;

import java.util.Map;

public class AlarmsViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = AlarmsViewPagerFragment.class
            .getSimpleName();


    public static AlarmsViewPagerFragment newInstance() {
        return new AlarmsViewPagerFragment();
    }

    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public AlarmsViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_tasks_new), ActiveAlarmsFragment.newInstance());
        fragmentMap.put(getString(R.string.title_tasks_old), FinishedAlarmsFragment.newInstance());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

