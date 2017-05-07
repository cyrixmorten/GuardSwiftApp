package com.guardswift.ui.view;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.event.EventLog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 4/19/15.
 */
public class EventLogView extends LinearLayout {


    @BindView(R.id.event_timestamp)
    TextView tvTimetamp;

    @BindView(R.id.event_type)
    TextView tvEventtype;

    @BindView(R.id.event_amount)
    TextView tvAmount;

    @BindView(R.id.event_people)
    TextView tvPeople;

    @BindView(R.id.event_locations)
    TextView tvLocations;

    @BindView(R.id.event_remarks)
    TextView tvRemarks;


    public EventLogView(Context context, EventLog eventLog) {
        super(context, null);

        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.gs_view_report_entry, this, true);

        ButterKnife.bind(v);

        setText(this.tvEventtype, eventLog.getEvent());
        setText(this.tvTimetamp, DateUtils.formatDateTime(getContext(), eventLog.getDeviceTimestamp().getTime(), DateUtils.FORMAT_SHOW_TIME));
        setText(this.tvAmount, String.valueOf(eventLog.getAmount()));
        setText(this.tvPeople, eventLog.getPeople());
        setText(this.tvLocations, eventLog.getLocations());
        setText(this.tvRemarks, eventLog.getRemarks());

    }

    private void setText(TextView tv, String text) {
        if (text == null) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(text);
        }
    }

}




