package com.guardswift.ui.view.card;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.documentation.eventlog.task.TaskTypeLogStrategy;
import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.Circuit;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.documentation.report.view.DownloadReport;
import com.guardswift.util.ToastHelper;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 4/19/15.
 */
public class ReportCard extends LinearLayout {


    @Bind(R.id.layout_events)
    LinearLayout layoutEvents;

    @Bind(R.id.tv_date)
    TextView tvDate;

    @Bind(R.id.tv_guard_name)
    TextView tvGuardName;

    @Bind(R.id.btn_fetch_pdf)
    Button btnFetchReport;

//    @Bind(R.id.btn_copy)
//    Button btnCopy;


    @Bind(R.id.card)
    CardView card;


    public ReportCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.gs_card_report, this, true);

        ButterKnife.bind(v);

    }

    public ReportCard(Context context, AttributeSet attrs, Report report) {
        this(context, attrs);
        setReport(report);
    }

    public ReportCard(Context context, Report report) {
        this(context, null, report);
    }

    public ReportCard(Context context) {
        this(context, null, null);
    }


    public void setReport(final Report report) {
        if (report == null) {
            return;
        }

        layoutEvents.removeAllViews();
        for (EventLog eventLog : report.getEventLogs()) {
            if (!eventLog.isReportEvent()) {
                continue;
            }

            TextView tv = new TextView(getContext());

            if (eventLog.getEventCode() == EventLog.EventCodes.STATIC_OTHER) {
                tv.setText(eventLog.getRemarks());
            } else {
                tv.setText(
                        eventLog.getEvent() + " " +
                                eventLog.getAmount() + " " +
                                eventLog.getPeople() + " " +
                                eventLog.getLocations() + " " +
                                eventLog.getRemarks()
                );
            }
            layoutEvents.addView(tv);

        }

        tvGuardName.setText(report.getGuardName());
        tvDate.setText(DateUtils.formatDateTime(
                getContext(),
                report.getDeviceTimestamp().getTime(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

//        String taskTypeName = report.getString(TaskTypeLogStrategy.taskTypeName);
//        if (taskTypeName != null && taskTypeName.equals(GSTask.TASK_TYPE.REGULAR.toString())) {
//            btnCopy.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    new CommonDialogsBuilder.MaterialDialogs(getContext()).okCancel(R.string.copy_report,
//                            getContext().getString(R.string.confirm_copy_report), new MaterialDialog.SingleButtonCallback() {
//                                @Override
//                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//
//                                    final List<EventLog> reportLogs = Lists.newArrayList();
//                                    for (EventLog eventLog: report.getEventLogs()) {
//                                        if (eventLog.isReportEvent()) {
//                                            reportLogs.add(eventLog);
//                                        }
//                                    }
//
//                                    GSTask pointer = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().getLastSelected();
//                                    final int[] pinned = {0};
//                                    for (EventLog reportLog: reportLogs) {
//                                        new EventLog.Builder(getContext()).from(reportLog, pointer).saveAsync(new GetCallback<EventLog>() {
//                                            @Override
//                                            public void done(EventLog object, ParseException e) {
//                                                report.add(object);
//                                                pinned[0]++;
//
//                                                if (pinned[0] == reportLogs.size()) {
//                                                    report.pinThenSaveEventually(new SaveCallback() {
//                                                        @Override
//                                                        public void done(ParseException e) {
//                                                        }
//                                                    }, new SaveCallback() {
//                                                        @Override
//                                                        public void done(ParseException e) {
//                                                            ToastHelper.toast(getContext(), getContext().getString(R.string.successfully_copied_report_events, reportLogs.size()));
//                                                        }
//                                                    }, true);
//                                                }
//                                            }
//                                        });
//                                    }
//                                }
//                            }).show();
//
//                }
//            });
//        } else {
//            btnCopy.setVisibility(GONE);
//        }

        btnFetchReport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.working)
                        .content(R.string.fetching_report)
                        .progress(true, 0)
                        .build();

                dialog.show();

                new DownloadReport(report, new DownloadReport.CompletedCallback() {
                    @Override
                    public void done(File file, Error e) {

                        dialog.dismiss();

                        if (getContext() == null) {
                            return;
                        }

                        if (e != null || file == null) {
                            ToastHelper.toast(getContext(), getContext().getString(R.string.error_downloading_file));
                        }
                        Intent target = new Intent(Intent.ACTION_VIEW);
                        target.setDataAndType(Uri.fromFile(file), "application/pdf");
                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        Intent intent = Intent.createChooser(target, "Open File");
                        try {
                            getContext().startActivity(intent);
                        } catch (ActivityNotFoundException e1) {
                            // Instruct the user to install a PDF reader here, or something
                            ToastHelper.toast(getContext(), "Please install a PDF viewer app");
                        }
                    }
                }).execute();
            }
        });

    }


}
