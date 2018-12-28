package com.guardswift.ui.parse.documentation.report.edit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.EventLogQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.helpers.UpdateFloatingActionButton;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.documentation.report.create.FragmentVisibilityListener;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandler;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandlerActivity;
import com.guardswift.ui.parse.documentation.report.view.DownloadReport;
import com.guardswift.util.GSIntents;
import com.guardswift.util.ToastHelper;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class ReportEditListFragment extends AbstractParseRecyclerFragment<EventLog, ReportEditRecycleAdapter.ReportViewHolder> implements UpdateFloatingActionButton, FragmentVisibilityListener {

    private static final int ADD_EVENT_RESULT_CODE = 200;

    public static ReportEditListFragment newInstance(ParseTask task) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTasksCache().setSelected(task);

        ReportEditListFragment fragment = new ReportEditListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ReportEditListFragment() {
    }

    @Inject
    ParseTasksCache ParseTasksCache;

    private ParseTask task;

    private FloatingActionButton fab;
    private boolean loading;

    private MenuItem pdfMenu;

    private MaterialDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        task = ParseTasksCache.getLastSelected();

        setHasOptionsMenu(true);
        setRetainInstance(true);

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

        menu.findItem(R.id.menu_add_arrival).setOnMenuItemClickListener((menuItem) -> {
            task.getController().performAction(TaskController.ACTION.ARRIVE, task);

            showProgressDialog();
            return true;
        });

        pdfMenu = menu.findItem(R.id.menu_pdf);
        pdfMenu.setOnMenuItemClickListener(menuItem -> {

            new DownloadReport(getContext()).execute(task, (file, e) -> GSIntents.openPDF(getContext(), file));

            return true;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<EventLog> createNetworkQueryFactory() {
        return () -> new EventLogQueryBuilder(false)
                .matching(task)
                .matching(task.getTaskGroupStarted())
                .orderByDescendingTimestamp()
                .whereIsReportEntry()
                .build();
    }

    @Override
    protected ParseRecyclerQueryAdapter<EventLog, ReportEditRecycleAdapter.ReportViewHolder> createRecycleAdapter() {
        ReportEditRecycleAdapter adapter = new ReportEditRecycleAdapter(getActivity(), createNetworkQueryFactory());

        adapter.addOnQueryLoadListener(new ParseRecyclerQueryAdapter.OnQueryLoadListener<EventLog>() {
            @Override
            public void onLoaded(List<EventLog> objects, Exception e) {
                if (e != null) {
                    new HandleException(TAG, "Load adapter", e);
                    return;
                }

                showFloatingActionButton(1000);
                loading = false;
                setPDFMenuEnabled(!objects.isEmpty());
            }

            @Override
            public void onLoading() {
                loading = true;
            }
        });
        return adapter;
    }

    private void setPDFMenuEnabled(boolean enabled) {
        if (pdfMenu != null) {
            pdfMenu.setEnabled(enabled);
        }
    }

    private void showProgressDialog() {
        progressDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).indeterminate().show();
    }

    private void dissmissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void updateArrivalTime(EventLog eventLog) {
        final DateTime timestamp = new DateTime();

        RadialTimePickerDialogFragment timePickerDialog = new RadialTimePickerDialogFragment()
                .setStartTime(timestamp.getHourOfDay(), timestamp.getMinuteOfHour())
                .setOnTimeSetListener((dialog, hourOfDay, minute) -> {
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);


                    eventLog.setDeviceTimestamp(cal.getTime());

                    eventLog.saveEventuallyAndNotify();
                })
                .setThemeDark()
                .setForced24hFormat();

        timePickerDialog.show(getChildFragmentManager(), "FRAG_TAG_ARRIVAL_TIME_PICKER");
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        boolean isEventLog = ev.getObject() instanceof EventLog;


        if (isEventLog) {
            dissmissProgressDialog();

            EventLog eventLog = (EventLog) ev.getObject();

            if (eventLog.getTask().equals(this.task)) {
                switch (ev.getAction()) {
                    case CREATE: {
                        getAdapter().addItem(eventLog);
                        setPDFMenuEnabled(true);

                        if (eventLog.isArrivalEvent()) {
                            updateArrivalTime(eventLog);
                        }
                        break;
                    }
                    case UPDATE: {
                        getAdapter().updateItem(eventLog);
                        break;
                    }
                    case DELETE: {
                        getAdapter().removeItem(eventLog);
                        break;
                    }
                }
                return false;
            }
        }

        // calls notifyDataSetChanged() if true
        return isEventLog;
    }


    @Override
    public void updateFloatingActionButton(final Context context, FloatingActionButton floatingActionButton) {
        this.fab = floatingActionButton;

        fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_note_add_white_18dp));
        fab.setOnClickListener(view -> {
            ParseTask task = ParseTasksCache.getLastSelected();

            if (task.isStaticTask()) {
                final MaterialDialog dialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).indeterminate().show();
                task.addReportEntry(context, "", null, (eventLog, e) -> {
                    if (e != null) {
                        new HandleException(TAG, "Create new static report entry", e);
                    }

                    UpdateEventHandlerActivity.newInstance(getContext(), eventLog, UpdateEventHandler.REQUEST_EVENT_REMARKS);

                    dialog.dismiss();
                });
            } else {
                CreateEventHandlerActivity.start(getContext(), task);
            }
        });
    }



    private void showFloatingActionButton(long delay) {
        new Handler().postDelayed(() -> {
            if (fab != null && fragmentVisible) {
                fab.show();
            }
        }, delay);
    }

    private boolean fragmentVisible = true;

    @Override
    public void fragmentBecameVisible() {
        if (!loading) {
            showFloatingActionButton(1000);
        }
        fragmentVisible = true;
    }

    @Override
    public void fragmentBecameInvisible() {
        fragmentVisible = false;
    }
}
