package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.documentation.report.create.activity.AddEventHandler;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandler;
import com.guardswift.ui.parse.documentation.report.create.activity.UpdateEventHandlerActivity;
import com.guardswift.ui.view.card.EventLogCard;

import java.util.Objects;

import javax.inject.Inject;

public class AddEventSummaryFragment extends InjectingFragment implements
		EventEntryFragment {

	protected static final String TAG = AddEventSummaryFragment.class
			.getSimpleName();

	public static AddEventSummaryFragment newInstance() {
		return new AddEventSummaryFragment();
	}

	public AddEventSummaryFragment() {
	}

	@Inject
	EventTypeCache eventTypeCache;

	@Inject
	ClientCache clientCache;

	EventLogCard eventLogCard;

	private AddEventHandler parentActivity;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {


		eventLogCard = new EventLogCard(getActivity());
		eventLogCard.setEditable(true);
		eventLogCard.setDeletable(false);


		eventLogCard.onEventClickListener(view -> UpdateEventHandlerActivity.newInstance(Objects.requireNonNull(getActivity()), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_TYPE, parentActivity.getEventType()));
//
		eventLogCard.onAmountClickListener(view -> new CommonDialogsBuilder.BetterPicks(Objects.requireNonNull(getActivity()).getSupportFragmentManager()).enterEventAmount(parentActivity.getEventType(),
				(reference, number, decimal, isNegative, fullNumber) -> {
					((AddEventHandler) getActivity()).setAmount(number.intValue());
					((TextView) view).setText(String.valueOf(number));
				}
		).show());

		eventLogCard.onPeopleClickListener(view -> UpdateEventHandlerActivity.newInstance(Objects.requireNonNull(getActivity()), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_PEOPLE, parentActivity.getPeople()));

		eventLogCard.onLocationsClickListener(view -> UpdateEventHandlerActivity.newInstance(Objects.requireNonNull(getActivity()), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_LOCATIONS, parentActivity.getLocations()));

		eventLogCard.onRemarksClickListener(view -> UpdateEventHandlerActivity.newInstance(Objects.requireNonNull(getActivity()), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_REMARKS, parentActivity.getRemarks()));

		return eventLogCard;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (getActivity() instanceof AddEventHandler) {
			parentActivity = ((AddEventHandler) getActivity());
		} else {
			throw new IllegalStateException("Activity must be of type AddEventHandler");
		}
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void fragmentBecameVisible() {

		eventLogCard.setEventLog(new EventLog.Builder(Objects.requireNonNull(getContext()))
				.event(parentActivity.getEventType())
				.amount(parentActivity.getAmount())
				.people(parentActivity.getPeople())
				.location(parentActivity.getLocations())
				.remarks(parentActivity.getRemarks())
				.build());

	}

    @Override
    public void fragmentBecameInvisible() {

    }

	@Override
	public int getTitle() {
		return R.string.summary;
	}

}