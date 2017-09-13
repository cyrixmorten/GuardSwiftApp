package com.guardswift.ui.parse.documentation.eventlog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.guardswift.persistence.cache.task.TaskCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.EventLogQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.parse.ParseQuery;

import java.util.List;

import javax.inject.Inject;

public class RegularEventFragment extends AbstractEventFragment {

	protected static final String TAG = RegularEventFragment.class
			.getSimpleName();


	public static RegularEventFragment newInstance(Context context, ParseTask regularTask) {

        GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().setSelected(regularTask);

		RegularEventFragment fragment = new RegularEventFragment();
		Bundle args = new Bundle();
        args.putBoolean(ParseTaskCreateReportActivity.HAS_ADD_EVENT_BUTTON, true);
        args.putString(ParseTaskCreateReportActivity.FILTER_EVENT, "");
		fragment.setArguments(args);
		return fragment;
	}

    @Inject
    TaskCache taskCache;

	public RegularEventFragment() {
	}


    @Override
    ParseQuery<EventLog> getEventLogQuery(List<String> filterEvents, boolean excludePerimiterEvents, boolean excludeAutomaticEvent) {
        EventLogQueryBuilder builder = EventLog.getQueryBuilder(true);

//        if (excludeAutomaticEvent)
//            builder.excludeAutomatic();

        if (excludePerimiterEvents)
            builder.whereIsReportEntry();


        builder.matching(taskCache.getSelected().getClient());

        return builder.build();
    }

    @Override
    void openAddEvent() {
//        if (CircuitUnit.Recent.getArrived() != null) {
            Intent intent = new Intent(getActivity(),
                    CreateEventHandlerActivity.class);
            startActivity(intent);
//        } else {
//            Toast.makeText(getActivity(),
//                    getString(R.string.not_possible_until_arrived),
//                    Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public ParseTask.TASK_TYPE getFragmentType() {
        return ParseTask.TASK_TYPE.REGULAR;
    }

    @Override
    ParseTask getTaskPointer() {
        return taskCache.getSelected();
    }

}
