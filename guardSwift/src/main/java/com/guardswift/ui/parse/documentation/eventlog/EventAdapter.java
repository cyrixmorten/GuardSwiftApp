package com.guardswift.ui.parse.documentation.eventlog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.util.Util;
import com.parse.ParseQueryAdapter;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventAdapter extends ParseQueryAdapter<EventLog> {

	private static final String TAG = EventAdapter.class.getSimpleName();

	public EventAdapter(Context context,
			ParseQueryAdapter.QueryFactory<EventLog> queryfactory) {
		super(context, queryfactory);

	}

	@BindView(R.id.event) TextView eventTextView;
	@BindView(R.id.location) TextView locationTextView;
	@BindView(R.id.time) TextView timeTextView;
    @BindView(R.id.amountLayout) LinearLayout amountLayout;
	@BindView(R.id.amount) TextView amountTextView;
	@BindView(R.id.guard) TextView guard;
	@BindView(R.id.remarks) TextView remarks;
	@BindView(R.id.relativeTime) TextView relativeTime;

	@Override
	public View getItemView(EventLog event, View v, ViewGroup parent) {

        if (v == null) {
            v = View.inflate(getContext(),
                    R.layout.view_adapter_item_event_log, null);
        }

        super.getItemView(event, v, parent);

        ButterKnife.bind(this, v);

        eventTextView.setText(event.getEvent());
        locationTextView.setText(event.getLocations());
        if (event.getAmount() > 0) {
            amountLayout.setVisibility(View.VISIBLE);
            amountTextView.setText(String.valueOf(event.getAmount()));
        } else {
            amountLayout.setVisibility(View.INVISIBLE);
        }
		guard.setText(event.getGuardName());

		String remarksString = event.getRemarks();
		if (!remarksString.isEmpty()) {
			remarks.setVisibility(View.VISIBLE);
			remarks.setText(remarksString);
		} else {
			remarks.setVisibility(View.GONE);
		}

		Date timeDate = new Date();
		if (event.getUpdatedAt() != null) {
			timeDate = event.getUpdatedAt();
		}

		timeTextView.setText(Util.dateFormatHourMinutes().format(timeDate));
		String relativeTimeString = Util.relativeTimeString(timeDate);
		relativeTime.setText(relativeTimeString);

		return v;

	}

	@Override
	public View getNextPageView(View v, ViewGroup parent) {
		if (v == null) {
			v = View.inflate(getContext(), R.layout.view_adapter_item_loadmore,
					null);
		}
		return v;
	}

}
