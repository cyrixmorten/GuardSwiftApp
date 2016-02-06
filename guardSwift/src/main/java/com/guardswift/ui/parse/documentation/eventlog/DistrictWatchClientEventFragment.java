package com.guardswift.ui.parse.documentation.eventlog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.guardswift.persistence.cache.task.DistrictWatchClientCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.DistrictWatchClientCreateEventHandlerActivity;
import com.parse.ParseQuery;

import java.util.List;

import javax.inject.Inject;

public class DistrictWatchClientEventFragment extends AbstractEventFragment {

	protected static final String TAG = DistrictWatchClientEventFragment.class
			.getSimpleName();

    public static DistrictWatchClientEventFragment newInstance(Context context, DistrictWatchClient districtWatchClient) {

        GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchClientCache().setSelected(districtWatchClient);

        DistrictWatchClientEventFragment fragment = new DistrictWatchClientEventFragment();
        Bundle args = new Bundle();
        args.putBoolean(GSTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON, true);
        args.putString(GSTaskCreateReportActivity.FILTER_EVENT, "");
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    DistrictWatchClientCache districtWatchClientCache;

	public DistrictWatchClientEventFragment() {
	}

    @Override
    ParseQuery<EventLog> getEventLogQuery(List<String> filterEvents, boolean excludePerimiterEvents, boolean excludeAutomaticEvent) {
        EventLog.QueryBuilder builder = EventLog.getQueryBuilder(false);

        if (excludeAutomaticEvent)
            builder.excludeAutomatic();

        if (excludePerimiterEvents)
            builder.whereIsReportEntry();

        builder.matchingEvents(filterEvents);

        builder.matching(districtWatchClientCache.getSelected().getClient());

        return builder.build();
    }

    @Override
    void openAddEvent() {
        Intent intent = new Intent(getActivity(),
                DistrictWatchClientCreateEventHandlerActivity.class);
        startActivity(intent);
    }

    @Override
    GSTask.TASK_TYPE getFragmentType() {
        return GSTask.TASK_TYPE.DISTRICTWATCH;
    }

    @Override
    GSTask getTaskPointer() {
        return districtWatchClientCache.getSelected();
    }


}
