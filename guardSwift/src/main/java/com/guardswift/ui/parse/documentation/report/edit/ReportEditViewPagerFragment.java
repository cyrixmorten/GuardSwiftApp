package com.guardswift.ui.parse.documentation.report.edit;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;

import java.util.Map;

import javax.inject.Inject;

public class ReportEditViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = ReportEditViewPagerFragment.class
            .getSimpleName();


    public static ReportEditViewPagerFragment newInstance(GSTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache().setSelected(task);

        return new ReportEditViewPagerFragment();
    }

    @Inject
    GSTasksCache gsTasksCache;

    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public ReportEditViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_report), ReportEditListFragment.newInstance(gsTasksCache.getLastSelected()));
        fragmentMap.put(getString(R.string.title_history), ReportHistoryListFragment.newInstance(gsTasksCache.getLastSelected()));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

