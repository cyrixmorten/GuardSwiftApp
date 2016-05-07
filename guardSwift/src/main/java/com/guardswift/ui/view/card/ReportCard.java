package com.guardswift.ui.view.card;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.view.EventLogView;
import com.guardswift.ui.web.WebViewFragment;
import com.guardswift.util.ToastHelper;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 4/19/15.
 */
public class ReportCard extends LinearLayout {


    @Bind(R.id.tv_taskType)
    TextView tvTaskType;

    @Bind(R.id.tv_date)
    TextView tvDate;

    @Bind(R.id.tv_guard_name)
    TextView tvGuardName;

//    @Bind(R.id.layout_events)
//    LinearLayout layoutEvents;

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

        if (report.has("circuitUnit")) {
            tvTaskType.setText(R.string.task_circuit);
        }
        if (report.has("staticTask")) {
            tvTaskType.setText(R.string.task_static);
        }
        if (report.has("districtWatch")) {
            tvTaskType.setText(R.string.task_districtwatch);
        }
        if (report.has("alarm")) {
            tvTaskType.setText(R.string.task_alarm);
        }

        tvGuardName.setText(report.getGuardName());
        tvDate.setText(DateUtils.formatDateTime(
                getContext(),
                report.getDeviceTimestamp().getTime(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));


        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (report.has("pdf")) {
                    String pdfUrl = report.getParseFile("pdf").getUrl();
                    viewReport(report, pdfUrl);
                } else {
                    final Dialog dialog = new CommonDialogsBuilder.MaterialDialogs(getContext()).indeterminate(R.string.fetching_report).show();

                    createPdf(report, new ReportCreatedCallback() {
                        @Override
                        public void done(String pdfUrl) {
                            viewReport(report, pdfUrl);
                        }

                        @Override
                        public void fail(ParseException e) {
                            new HandleException(ReportCard.class.getSimpleName(), "Create PDF failed", e);
                            ToastHelper.toast(getContext(), "Beklager, kunne ikke hente rapporten");
                        }

                        @Override
                        public void any() {
                            if (dialog != null) {
                                dialog.cancel();
                            }
                        }


                    });
                }
            }
        });

    }

    private interface ReportCreatedCallback {
        void done(String pdfUrl);
        void fail(ParseException e);
        void any();
    }
    private void createPdf(final Report report, final ReportCreatedCallback callback) {
        Map<String, String> params = Maps.newHashMap();
        params.put("reportId", report.getObjectId());
        ParseCloud.callFunctionInBackground("reportToPDF", params, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> response, ParseException e) {
                if (e == null) {
                    callback.done(response.get("pdfUrl").toString());
                    callback.any();
                } else {
                    callback.fail(e);
                    callback.any();
                }
            }
        });
    }

    private void viewReport(Report report, String pdfUrl) {
        String googleDocServiceUrl = "http://docs.google.com/gview?embedded=true&url=";
        GenericToolbarActivity.start(
                getContext(),
                "Rapport",
                report.getString("clientFullAddress"),
                WebViewFragment.newInstance(googleDocServiceUrl+pdfUrl));
    }


}
