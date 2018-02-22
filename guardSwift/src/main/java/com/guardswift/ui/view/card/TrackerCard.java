package com.guardswift.ui.view.card;

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
import com.guardswift.persistence.parse.documentation.gps.Tracker;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrackerCard extends LinearLayout {

    @BindView(R.id.tv_in_progress)
    TextView tvActive;

    @BindView(R.id.tv_date_start)
    TextView tvDateStart;

    @BindView(R.id.tv_date_end)
    TextView tvDateEnd;

    @BindView(R.id.tv_guard_name)
    TextView tvGuardName;

    @BindView(R.id.tv_minutes)
    TextView tvMinutes;



    @BindView(R.id.card)
    CardView card;


    public TrackerCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.gs_card_tracker, this, true);

        ButterKnife.bind(v);
    }

    public TrackerCard(Context context, AttributeSet attrs, Tracker tracker) {
        this(context, attrs);
        setTracker(tracker);
    }

    public TrackerCard(Context context, Tracker tracker) {
        this(context, null, tracker);
    }

    public TrackerCard(Context context) {
        this(context, null, null);
    }

    public void setOnClick(OnClickListener clickListener) {
        this.card.setOnClickListener(clickListener);
    }

    public void setTracker(final Tracker tracker) {
        if (tracker == null) {
            return;
        }

        Date dateStart = tracker.getDateStart();
        Date dateEnd = tracker.getDateEnd();

        tvDateStart.setText(DateUtils.formatDateTime(
                getContext(),
                dateStart.getTime(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));

        tvDateEnd.setText(DateUtils.formatDateTime(
                getContext(),
                dateEnd.getTime(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));


        tvGuardName.setText(tracker.getGuardName());
        tvMinutes.setText(getDurationText(dateStart, dateEnd));
        tvActive.setVisibility(tracker.inProgress() ? VISIBLE : INVISIBLE);
    }

    private String getDurationText(Date from, Date to) {

        Duration duration = new Duration(from.getTime(), to.getTime());

        PeriodFormatterBuilder formatterBuilder = new PeriodFormatterBuilder();

        if (duration.getStandardHours() > 0) {

            formatterBuilder
                    .appendHours()
                    .appendSuffix(" ")
                    .appendSuffix(getContext().getString(R.string.hour), getContext().getString(R.string.hours))
                    ;
        }

        if (duration.getStandardMinutes() > 0) {

            formatterBuilder
                    .appendPrefix(" ")
                    .appendMinutes()
                    .appendSuffix(" ")
                    .appendSuffix(getContext().getString(R.string.minute), getContext().getString(R.string.minutes))
                    ;
        }

        if (duration.getStandardHours() == 0 && duration.getStandardMinutes() == 0) {
            formatterBuilder
                    .appendSeconds()
                    .appendSuffix(" ")
                    .appendSuffix(getContext().getString(R.string.seconds));
        }

        return formatterBuilder.toFormatter().print(duration.toPeriod());
    }


}
