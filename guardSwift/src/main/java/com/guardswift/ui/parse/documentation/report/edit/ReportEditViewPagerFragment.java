package com.guardswift.ui.parse.documentation.report.edit;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.parse.documentation.report.view.ReportHistoryListFragment;

import java.util.Map;

import javax.inject.Inject;

public class ReportEditViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = ReportEditViewPagerFragment.class
            .getSimpleName();


    public static ReportEditViewPagerFragment newInstance(ParseTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache().setSelected(task);

        return new ReportEditViewPagerFragment();
    }

    @Inject
    ParseTasksCache ParseTasksCache;

    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public ReportEditViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ParseTask task = ParseTasksCache.getLastSelected();

        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_report), ReportEditListFragment.newInstance(task));

        if (task.isStaticTask()) {
            fragmentMap.put(getString(R.string.title_send), ReportSummaryFragment.newInstance(task));
            fragmentMap.put(getString(R.string.title_history), ReportHistoryListFragment.newInstance(task.getClient(), task.getTaskType()));
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

