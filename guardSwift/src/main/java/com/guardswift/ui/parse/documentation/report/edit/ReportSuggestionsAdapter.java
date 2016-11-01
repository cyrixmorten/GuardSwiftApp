package com.guardswift.ui.parse.documentation.report.edit;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.view.card.EventLogCard;
import com.guardswift.util.ToastHelper;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQueryAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by cyrix on 11/21/15.
 */
public class ReportSuggestionsAdapter extends ParseRecyclerQueryAdapter<EventLog, ReportSuggestionsAdapter.ReportViewHolder> {


    private static final String TAG = ReportSuggestionsAdapter.class.getSimpleName();

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        public EventLogCard eventLogCard;

        public ReportViewHolder(EventLogCard eventLogCard) {
            super(eventLogCard);

            this.eventLogCard = eventLogCard;
        }

    }

    @Override
    protected List<EventLog> postProcessResults(List<EventLog> queriedItems) {
        Map<String, EventLog> uniqueEvents = Maps.newHashMap();

        for (EventLog eventLog: queriedItems) {
            if (!eventLog.getEvent().isEmpty()) {
                uniqueEvents.put(eventLog.getEvent(), eventLog);
            }
        }

        return Lists.newArrayList(uniqueEvents.values());
    }

    private Context context;
    private GSTask task;
    private final CoordinatorLayout snackBar;

    public ReportSuggestionsAdapter(Context context, GSTask task, CoordinatorLayout snackBar, ParseQueryAdapter.QueryFactory<EventLog> queryFactory) {
        super(queryFactory);
        this.context = context;
        this.task = task;
        this.snackBar = snackBar;
        Log.d(TAG, "SnackBar: " + snackBar);
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EventLogCard eventLogCard = new EventLogCard(context);
        eventLogCard.setEditable(false);
        eventLogCard.setDeletable(false);
        eventLogCard.setTimestamped(false);
        eventLogCard.setCopyToReportEnabled(true);
        return new ReportViewHolder(eventLogCard);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        final EventLog eventLog = getItem(position);
        Log.d(TAG, "onBindViewHolder: " + eventLog);
        holder.eventLogCard.setEventLog(eventLog);

        holder.eventLogCard.onCopyToReportClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastHelper.toast(context, context.getString(R.string.copying_to_report, eventLog.getEvent()));
                new EventLog.Builder(context).from(eventLog, task).saveAsync(new GetCallback<EventLog>() {
                    @Override
                    public void done(final EventLog copiedEventLog, ParseException e) {
                        Snackbar snackbar = Snackbar
                                .make(snackBar, context.getString(R.string.entity_copied, copiedEventLog.getEvent()), Snackbar.LENGTH_LONG);
                        snackbar.setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                copiedEventLog.deleteEventually();
                            }
                        });
                        snackbar.show();

                        EventBusController.postUIUpdate(eventLog, 1000);
                    }
                });
            }
        });
    }
}
