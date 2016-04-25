package com.guardswift.ui.parse.documentation.report.view;

import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ReportHistoryListFragment extends AbstractParseRecyclerFragment<Report, ReportHistoryAdapter.ReportViewHolder> {


    public static ReportHistoryListFragment newInstance(Client client) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getClientCache().setSelected(client);

        ReportHistoryListFragment fragment = new ReportHistoryListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ClientCache clientCache;

    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new EventLog();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<Report> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Report>() {
            @Override
            public ParseQuery<Report> create() {
                return new Report.QueryBuilder(false).matching(clientCache.getSelected()).build();
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
