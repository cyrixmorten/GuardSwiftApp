package com.guardswift.ui.parse.documentation.report.edit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.common.UpdateFloatingActionButton;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.documentation.report.create.FragmentVisibilityListener;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.view.DownloadReport;
import com.guardswift.util.GSIntents;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class ReportEditListFragment extends AbstractParseRecyclerFragment<EventLog, ReportEditRecycleAdapter.ReportViewHolder> implements UpdateFloatingActionButton, FragmentVisibilityListener {


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

    private FloatingActionButton fab;
    private boolean loading;

    private String reportId;
    private MenuItem pdfMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();

        pdfMenu = null;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.task_report, menu);

        pdfMenu = menu.findItem(R.id.menu_pdf);
        pdfMenu.setEnabled(false);
        pdfMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new DownloadReport(getContext()).execute(reportId, new DownloadReport.CompletedCallback() {
                    @Override
                    public void done(File file, Error e) {
                        GSIntents.openPDF(getContext(), file);
                    }
                });
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new EventLog();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<EventLog> createNetworkQueryFactory() {
        reportId = (gsTasksCache.getLastSelected() != null) ? gsTasksCache.getLastSelected().getReportId() : "";
        return new ParseQueryAdapter.QueryFactory<EventLog>() {
            @Override
            public ParseQuery<EventLog> create() {
                return new EventLog.QueryBuilder(false).matchingReportId(reportId).orderByDescendingTimestamp().whereIsReportEntry().build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<EventLog, ReportEditRecycleAdapter.ReportViewHolder> createRecycleAdapter() {
        ReportEditRecycleAdapter adaper = new ReportEditRecycleAdapter(getActivity(), createNetworkQueryFactory());

        adaper.addOnQueryLoadListener(new ParseRecyclerQueryAdapter.OnQueryLoadListener<EventLog>() {
            @Override
            public void onLoaded(List<EventLog> objects, Exception e) {
                showFloadingActionButton(1000);
                loading = false;
                if (pdfMenu != null) {
                    pdfMenu.setEnabled(!objects.isEmpty());
                }
            }

            @Override
            public void onLoading() {
                loading = true;
            }
        });
        return adaper;
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Log.w(TAG, "isRelevantUIEvent: " + ev.getObject() + " -> " + (ev.getObject() instanceof EventLog));
        return ev.getObject() instanceof EventLog;
    }


    @Override
    public void updateFloatingActionButton(final Context context, FloatingActionButton floatingActionButton) {
        this.fab = floatingActionButton;

        fab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_note_add_white_18dp));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GSTask task = gsTasksCache.getLastSelected();

                if (task instanceof StaticTask) {
                    final MaterialDialog dialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).indeterminate().show();
                    ((StaticTask) task).addReportEntry(context, "", new GetCallback<EventLog>() {
                        @Override
                        public void done(EventLog eventLog, ParseException e) {
                            if (e != null) {
                                new HandleException(TAG, "create new static report entry", e);
                            }

//                          Does not work as intended, not updating list entry when returning
//                          UpdateEventHandlerActivity.newInstance(getContext(), eventLog, UpdateEventHandler.REQUEST_EVENT_REMARKS);

                            dialog.dismiss();
                        }
                    });
                } else {
                    CreateEventHandlerActivity.start(getContext(), task);
                }
            }
        });
    }

    private void showFloadingActionButton(long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fab != null && fragmentVisible) {
                    fab.show();
                }
            }
        }, delay);
    }

    private boolean fragmentVisible = true;

    @Override
    public void fragmentBecameVisible() {
        if (!loading) {
            showFloadingActionButton(1000);
        }
        fragmentVisible = true;
    }

    @Override
    public void fragmentBecameInvisible() {
        fragmentVisible = false;
    }
}
