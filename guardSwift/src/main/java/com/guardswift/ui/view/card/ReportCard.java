package com.guardswift.ui.view.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 4/19/15.
 */
public class ReportCard extends LinearLayout {



    @Bind(R.id.tv_date)
    TextView tvDate;

    @Bind(R.id.tv_guard_name)
    TextView tvGuardName;

    @Bind(R.id.layout_events)
    LinearLayout layoutEvents;


    public ReportCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.gs_card_eventlog, this, true);

        ButterKnife.bind(v);

    }

    public ReportCard(Context context, AttributeSet attrs, Report report) {
        this(context, attrs);
        setReport(report);
    }

    public ReportCard(Context context, Report report) {
        this(context, null, report);
    }


    public void setReport(Report report) {
        if (report == null) {
            return;
        }

        tvGuardName.setText(report.getGuardName());
        tvDate.setText(DateUtils.formatDateTime(getContext(), report.getCreatedAt().getTime(), DateUtils.FORMAT_SHOW_DATE));

    }






}
