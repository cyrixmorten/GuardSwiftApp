package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
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

import java.math.BigDecimal;
import java.math.BigInteger;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		eventLogCard = new EventLogCard(getActivity());
		eventLogCard.setEditable(true);
		eventLogCard.setDeletable(false);


		eventLogCard.onEventClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				UpdateEventHandlerActivity.newInstance(getActivity(), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_TYPE, parentActivity.getEventType());
			}
		});
//
		eventLogCard.onAmountClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				new CommonDialogsBuilder.BetterPicks(getActivity().getSupportFragmentManager()).enterEventAmount(parentActivity.getEventType(),
						new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
							@Override
							public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
								((AddEventHandler) getActivity()).setAmount(number.intValue());
								((TextView)view).setText(String.valueOf(number));
							}
						}
				).show();
			}
		});

		eventLogCard.onPeopleClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				UpdateEventHandlerActivity.newInstance(getActivity(), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_PEOPLE, parentActivity.getPeople());
			}
		});

		eventLogCard.onLocationsClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				UpdateEventHandlerActivity.newInstance(getActivity(), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_LOCATIONS, parentActivity.getLocations());
			}
		});

		eventLogCard.onRemarksClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				UpdateEventHandlerActivity.newInstance(getActivity(), clientCache.getSelected(), UpdateEventHandler.REQUEST_EVENT_REMARKS, parentActivity.getRemarks());
			}
		});

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

		eventLogCard.setEventLog(new EventLog.Builder(getContext())
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