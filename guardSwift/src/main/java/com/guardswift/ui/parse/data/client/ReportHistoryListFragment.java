package com.guardswift.ui.parse.data.client;

import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.documentation.report.edit.ReportSuggestionsAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ReportHistoryListFragment extends AbstractParseRecyclerFragment<Report, ReportSuggestionsAdapter.ReportViewHolder> {


    public static ReportHistoryListFragment newInstance(GSTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache().setSelected(task);

        ReportHistoryListFragment fragment = new ReportHistoryListFragment();
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
    protected ParseQueryAdapter.QueryFactory<Report> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Report>() {
            @Override
            public ParseQuery<Report> create() {
                return new Report.QueryBuilder(false).build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Report, ReportSuggestionsAdapter.ReportViewHolder> createRecycleAdapter() {
        return null;
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return false;
    }
}
