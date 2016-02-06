package com.guardswift.ui.parse.documentation.eventlog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.CircuitUnitCreateEventHandlerActivity;
import com.parse.ParseQuery;

import java.util.List;

import javax.inject.Inject;

public class CircuitUnitEventFragment extends AbstractEventFragment {

	protected static final String TAG = CircuitUnitEventFragment.class
			.getSimpleName();


	public static CircuitUnitEventFragment newInstance(Context context, CircuitUnit circuitUnit) {

        GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache().setSelected(circuitUnit);

		CircuitUnitEventFragment fragment = new CircuitUnitEventFragment();
		Bundle args = new Bundle();
        args.putBoolean(GSTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON, true);
        args.putString(GSTaskCreateReportActivity.FILTER_EVENT, "");
		fragment.setArguments(args);
		return fragment;
	}

    @Inject
    CircuitUnitCache circuitUnitCache;

	public CircuitUnitEventFragment() {
	}


    @Override
    ParseQuery<EventLog> getEventLogQuery(List<String> filterEvents, boolean excludePerimiterEvents, boolean excludeAutomaticEvent) {
        EventLog.QueryBuilder builder = EventLog.getQueryBuilder(true);

        if (excludeAutomaticEvent)
            builder.excludeAutomatic();

        if (excludePerimiterEvents)
            builder.whereIsReportEntry();

        builder.matchingEvents(filterEvents);

        builder.matching(circuitUnitCache.getSelected().getClient());

        return builder.build();
    }

    @Override
    void openAddEvent() {
//        if (CircuitUnit.Recent.getArrived() != null) {
            Intent intent = new Intent(getActivity(),
                    CircuitUnitCreateEventHandlerActivity.class);
            startActivity(intent);
//        } else {
//            Toast.makeText(getActivity(),
//                    getString(R.string.not_possible_until_arrived),
//                    Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public GSTask.TASK_TYPE getFragmentType() {
        return GSTask.TASK_TYPE.REGULAR;
    }

    @Override
    GSTask getTaskPointer() {
        return circuitUnitCache.getSelected();
    }

}
