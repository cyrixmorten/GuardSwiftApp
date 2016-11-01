package com.guardswift.ui.parse.documentation.report.edit;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;
import java.util.Map;

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
        ParseRecyclerQueryAdapter<EventLog, ReportSuggestionsAdapter.ReportViewHolder> adapter = new ReportSuggestionsAdapter(getActivity(), gsTasksCache.getLastSelected(), coordinatorLayout, createNetworkQueryFactory());
        adapter.setPostProcessor(new PostProcessAdapterResults<EventLog>() {
            @Override
            public List<EventLog> postProcess(List<EventLog> queriedItems) {
                Map<String, EventLog> uniqueEvents = Maps.newHashMap();

                for (EventLog eventLog: queriedItems) {
                    if (!eventLog.getEvent().isEmpty()) {
                        uniqueEvents.put(eventLog.getEvent(), eventLog);
                    }
                }

                return Lists.newArrayList(uniqueEvents.values());
            }
        });

        return adapter;
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Log.w(TAG, "isRelevantUIEvent: " + ev.getObject() + " -> " + (ev.getObject() instanceof EventLog));
        return ev.getObject() instanceof EventLog;
    }
}
