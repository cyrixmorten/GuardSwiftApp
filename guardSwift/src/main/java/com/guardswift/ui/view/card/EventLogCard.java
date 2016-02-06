package com.guardswift.ui.view.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapText;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.event.EventLog;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.beardedhen.androidbootstrap.font.FontAwesome.FA_REMOVE;

/**
 * Created by cyrix on 4/19/15.
 */
public class EventLogCard extends LinearLayout {


    @Bind(R.id.card_header)
    RelativeLayout cardHeader;

    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.layoutDelete)
    LinearLayout layoutDelete;

    @Bind(R.id.btnDelete)
    AwesomeTextView btnDelete;

    @Bind(R.id.btn_copy_to_report)
    Button btnCopyToReport;

    @Bind(R.id.tv_timestamp)
    TextView tvTimestamp;

    @Bind(R.id.layout_event)
    LinearLayout layoutEvent;

    CardView cardEvent;
    TextView tvEvent;

    @Bind(R.id.layout_amount)
    LinearLayout layoutAmount;

    CardView cardAmount;
    TextView tvAmount;

    @Bind(R.id.layout_people)
    LinearLayout layoutPeople;

    CardView cardPeople;
    TextView tvPeople;

    @Bind(R.id.layout_locations)
    LinearLayout layoutLocations;

    CardView cardLocations;
    TextView tvLocations;

    @Bind(R.id.layout_remarks)
    LinearLayout layoutRemarks;

    CardView cardRemarks;
    TextView tvRemarks;

    private EventLog eventLog;
    private boolean editable;
    private boolean deletable;
    private boolean timestamped;
    private boolean copyToReportEnabled;


    public EventLogCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.gs_card_eventlog, this, true);

        ButterKnife.bind(v);

        btnDelete.setBootstrapText(new BootstrapText.Builder(context)
                .addFontAwesomeIcon(FA_REMOVE)
                .build());
        btnDelete.setBootstrapBrand(DefaultBootstrapBrand.DANGER);

        cardEvent = ButterKnife.findById(v, R.id.tv_area_event);
        tvEvent = ButterKnife.findById(cardEvent, R.id.text);

        cardAmount = ButterKnife.findById(v, R.id.tv_area_amount);
        tvAmount = ButterKnife.findById(cardAmount, R.id.text);

        cardPeople = ButterKnife.findById(v, R.id.tv_area_people);
        tvPeople = ButterKnife.findById(cardPeople, R.id.text);

        cardLocations = ButterKnife.findById(v, R.id.tv_area_location);
        tvLocations = ButterKnife.findById(cardLocations, R.id.text);

        cardRemarks = ButterKnife.findById(v, R.id.tv_area_remarks);
        tvRemarks = ButterKnife.findById(cardRemarks, R.id.text);

    }

    public EventLogCard(Context context, AttributeSet attrs, EventLog eventLog) {
        this(context, attrs);
        setEventLog(eventLog);
    }

    public EventLogCard(Context context, EventLog eventLog) {
        this(context, null, eventLog);
    }

    public EventLogCard(Context context) {
        this(context, null, null);

        setEditable(true);
        setDeletable(true);
        setTimestamped(true);
        setCopyToReportEnabled(false);
    }

    public void setEventLog(EventLog eventLog) {
        if (eventLog == null)
            return;

        this.eventLog = eventLog;

        Date date = (eventLog.getDeviceTimestamp() != null) ? eventLog.getDeviceTimestamp() : new Date();
        String formattedDate = android.text.format.DateUtils.formatDateTime(getContext(),
                date.getTime(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
        tvTimestamp.setText(formattedDate);

        String eventType = getContext().getString(R.string.event);
        updateLayout(layoutEvent, cardEvent, tvEvent, eventLog.getEvent(), eventType);
        layoutEvent.setVisibility((editable) ? VISIBLE : GONE); // shown as title

        String amount = getContext().getString(R.string.amount);
        String amountText = (eventLog.getAmount() != 0) ? String.valueOf(eventLog.getAmount()) : "";
        updateLayout(layoutAmount, cardAmount, tvAmount, amountText, amount);

        String people = getContext().getString(R.string.people);
        updateLayout(layoutPeople, cardPeople, tvPeople, eventLog.getPeople(), people);

        String location = getContext().getString(R.string.location);
        updateLayout(layoutLocations, cardLocations, tvLocations, eventLog.getLocations(), location);

        String remarks = getContext().getString(R.string.remarks);
        updateLayout(layoutRemarks, cardRemarks, tvRemarks, eventLog.getRemarks(), remarks);

        tvTitle.setText(eventLog.getEvent());

        btnDelete.setVisibility((deletable) ? VISIBLE : GONE);
        tvTimestamp.setVisibility((timestamped) ? VISIBLE : GONE);
        btnCopyToReport.setVisibility((copyToReportEnabled) ? VISIBLE : GONE);


    }

    public EventLog getEventLog() {
        return this.eventLog;
    }

    @SuppressLint("PrivateResource")
    private void updateLayout(ViewGroup layout, CardView cardView, TextView textView, String value, String entryName) {
//        Log.w("EventLogCard", "updateLayout: " + value);
//        cardView.setEnabled(editable);
        layout.setVisibility(VISIBLE);
        if (value == null || value.trim().isEmpty()) {
            layout.setVisibility((editable) ? VISIBLE : GONE);

            String click_to_add_msg = getContext().getString(R.string.click_to_add_x, entryName);
            textView.setText(click_to_add_msg);
            textView.setTextAppearance(getContext(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body1);
//            Log.w("EventLogCard", entryName + " : " + click_to_add_msg);

            // indicator that a value is missing
            textView.setTag(null);
        } else {
            textView.setText(value);
            textView.setTextAppearance(getContext(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
//            Log.w("EventLogCard", entryName + " : " + value);

            // indicator that a value has been set
            textView.setTag(value);

        }

    }

    public void onEventClickListener(final OnClickListener clickListener) {
        layoutEvent.setOnClickListener(new EventClickListener(clickListener, tvEvent));
    }

    public void onAmountClickListener(OnClickListener clickListener) {
        layoutAmount.setOnClickListener(new EventClickListener(clickListener, tvAmount));
    }

    public void onPeopleClickListener(OnClickListener clickListener) {
        layoutPeople.setOnClickListener(new EventClickListener(clickListener, tvPeople));
    }

    public void onLocationsClickListener(OnClickListener clickListener) {
        layoutLocations.setOnClickListener(new EventClickListener(clickListener, tvLocations));
    }

    public void onRemarksClickListener(OnClickListener clickListener) {
        layoutRemarks.setOnClickListener(new EventClickListener(clickListener, tvRemarks));
    }

    public void onDeleteClickListener(OnClickListener clickListener) {
        btnDelete.setOnClickListener(clickListener);
    }

    public void onCopyToReportClickListener(OnClickListener clickListener) {
        btnCopyToReport.setOnClickListener(clickListener);
    }

    public void onTimestampClickListener(OnClickListener clickListener) {
        tvTimestamp.setOnClickListener(clickListener);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isTimestamped() {
        return timestamped;
    }

    public void setTimestamped(boolean timestamped) {
        this.timestamped = timestamped;
    }

    public boolean isCopyToReportEnabled() {
        return copyToReportEnabled;
    }

    public void setCopyToReportEnabled(boolean copyToReportEnabled) {
        this.copyToReportEnabled = copyToReportEnabled;
    }

    public void setHeaderVisibility(int visibility) {
        this.cardHeader.setVisibility(visibility);
    }

    public void setRemarksVisibility(int visibility) {
        this.layoutRemarks.setVisibility(visibility);
    }

    public boolean hasRemarks() {
        return tvRemarks.getTag() != null;
    }

    private class EventClickListener implements OnClickListener {

        private final OnClickListener parentClickListener;
        private final View childView;

        public EventClickListener(OnClickListener parentClickListener, View childView) {
            this.parentClickListener = parentClickListener;
            this.childView = childView;
        }
        @Override
        public void onClick(View view) {
            parentClickListener.onClick(childView);
        }
    }



}
