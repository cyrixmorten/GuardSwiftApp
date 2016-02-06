package com.guardswift.ui.parse.documentation.report.edit;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ReportSuggestionsListFragment extends AbstractParseRecyclerFragment<EventLog, ReportSuggestionsAdapter.ReportViewHolder> {


    public static ReportSuggestionsListFragment newInstance(GSTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache().setSelected(task);

        ReportSuggestionsListFragment fragment = new ReportSuggestionsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    GSTasksCache gsTasksCache;

    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new EventLog();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<EventLog> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<EventLog>() {
            @Override
            public ParseQuery<EventLog> create() {
                GSTask task = gsTasksCache.getLastSelected();
                return new EventLog.QueryBuilder(false)
                        .matching(task.getClient())
                        .matchingEventCode(task.getEventCode())
                        .notMatchingReportId(task.getReportId())
                        .orderByDescendingTimestamp()
                        .build().setLimit(10);
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<EventLog, ReportSuggestionsAdapter.ReportViewHolder> createRecycleAdapter() {
        CoordinatorLayout coordinatorLayout = ((AbstractTabsViewPagerFragment) getParentFragment()).getCoordinatorLayout();
        return new ReportSuggestionsAdapter(getActivity(), gsTasksCache.getLastSelected(), coordinatorLayout, createNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Log.w(TAG, "isRelevantUIEvent: " + ev.getObject() + " -> " + (ev.getObject() instanceof EventLog));
        return ev.getObject() instanceof EventLog;
    }
}
