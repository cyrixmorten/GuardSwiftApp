package com.guardswift.ui.parse.documentation.report.view;

import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.ReportQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ReportHistoryListFragment extends AbstractParseRecyclerFragment<Report, ReportHistoryAdapter.ReportViewHolder> {


    public static ReportHistoryListFragment newInstance(Client client) {
        return newInstance(client, null);
    }

    public ReportHistoryListFragment() {
    }

    public static ReportHistoryListFragment newInstance(Client client, ParseTask.TASK_TYPE task_type) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getClientCache().setSelected(client);

        ReportHistoryListFragment fragment = new ReportHistoryListFragment();
        Bundle args = new Bundle();
            args.putSerializable("taskType", task_type);
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ClientCache clientCache;

    @Override
    protected ParseQueryAdapter.QueryFactory<Report> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Report>() {
            @Override
            public ParseQuery<Report> create() {
                ParseTask.TASK_TYPE taskType = (ParseTask.TASK_TYPE) getArguments().getSerializable("taskType");
                return new ReportQueryBuilder(false)
                        .include(Report.eventLogs)
                        .matching(clientCache.getSelected())
                        .matching(taskType)
                        .build()
                        .setLimit(10)
                        .addDescendingOrder("createdAt");
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Report, ReportHistoryAdapter.ReportViewHolder> createRecycleAdapter() {
        return new ReportHistoryAdapter(getContext(), createNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return false;
    }
}
