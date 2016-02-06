package com.guardswift.ui.parse.documentation.report.edit;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.common.UpdateFloatingActionButton;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ReportEditListFragment extends AbstractParseRecyclerFragment<EventLog, ReportEditRecycleAdapter.ReportViewHolder> implements UpdateFloatingActionButton {


    public static ReportEditListFragment newInstance(GSTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache().setSelected(task);

        ReportEditListFragment fragment = new ReportEditListFragment();
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
        final String reportId = (gsTasksCache.getLastSelected() != null) ? gsTasksCache.getLastSelected().getReportId() : "";
        return new ParseQueryAdapter.QueryFactory<EventLog>() {
            @Override
            public ParseQuery<EventLog> create() {
                return new EventLog.QueryBuilder(false).matchingReportId(reportId).orderByDescendingTimestamp().whereIsReportEntry().build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<EventLog, ReportEditRecycleAdapter.ReportViewHolder> createRecycleAdapter() {
        Client client = gsTasksCache.getLastSelected().getClient();
        return new ReportEditRecycleAdapter(getActivity(), client, createNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Log.w(TAG, "isRelevantUIEvent: " + ev.getObject() + " -> " + (ev.getObject() instanceof EventLog));
        return ev.getObject() instanceof EventLog;
    }


    @Override
    public void updateFloatingActionButton(final Context context, FloatingActionButton floatingActionButton) {
        floatingActionButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_note_add_white_18dp));
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GSTask task = gsTasksCache.getLastSelected();
                if (task instanceof StaticTask) {
                    final MaterialDialog dialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).intermediateProgress().show();
                    ((StaticTask)task).addReportEntry(context, "", new GetCallback<EventLog>() {
                        @Override
                        public void done(EventLog object, ParseException e) {
                            if (e != null) {
                                new HandleException(TAG, "create new static report entry", e);
                            }
                            dialog.dismiss();
                        }
                    });
                } else {
                    CreateEventHandlerActivity.start(getContext(), gsTasksCache.getLastSelected());
                }
            }
        });
        floatingActionButton.show();
    }
}
