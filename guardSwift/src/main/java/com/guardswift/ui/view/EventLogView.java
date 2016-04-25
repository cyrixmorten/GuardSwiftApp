package com.guardswift.ui.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.util.ToastHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 4/19/15.
 */
public class EventLogView extends LinearLayout {


    @Bind(R.id.event_timestamp)
    TextView tvTimetamp;

    @Bind(R.id.event_type)
    TextView tvEventtype;

    @Bind(R.id.event_amount)
    TextView tvAmount;

    @Bind(R.id.event_people)
    TextView tvPeople;

    @Bind(R.id.event_locations)
    TextView tvLocations;

    @Bind(R.id.event_remarks)
    TextView tvRemarks;


    public EventLog(Context context, EventLog eventLog) {
        super(context, null);

        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.gs_view_report_entry, this, true);

        ButterKnife.bind(v);

        setText(this.tvEventtype, eventLog.getEve);

    }

    private void setText(TextView tv, String text) {
        if (text == null) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(text);
        }
    }

}





}
