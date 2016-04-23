package com.guardswift.ui.parse.data.client;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.view.card.EventLogCard;
import com.parse.ParseQueryAdapter;

/**
 * Created by cyrix on 11/21/15.
 */
public class ReportHistoryAdapter extends ParseRecyclerQueryAdapter<Report, ReportHistoryAdapter.ReportViewHolder> {


    private static final String TAG = ReportHistoryAdapter.class.getSimpleName();

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        public EventLogCard eventLogCard;

        public ReportViewHolder(EventLogCard eventLogCard) {
            super(eventLogCard);

            this.eventLogCard = eventLogCard;
        }

    }


    private Context context;
    private GSTask task;

    public ReportHistoryAdapter(Context context, GSTask task, ParseQueryAdapter.QueryFactory<Report> queryFactory) {
        super(queryFactory);
        this.context = context;
        this.task = task;

    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EventLogCard eventLogCard = new EventLogCard(context);
        eventLogCard.setEditable(false);
        eventLogCard.setDeletable(false);
        eventLogCard.setTimestamped(false);
        eventLogCard.setCopyToReportEnabled(false);
        return new ReportViewHolder(eventLogCard);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        final Report eventLog = getItem(position);
        Log.d(TAG, "onBindViewHolder: " + eventLog);


    }
}
