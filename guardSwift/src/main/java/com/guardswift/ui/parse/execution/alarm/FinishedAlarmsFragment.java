package com.guardswift.ui.parse.execution.alarm;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.AlarmTaskQueryBuilder;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ui.widget.ParseQueryAdapter;


public class FinishedAlarmsFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = FinishedAlarmsFragment.class
			.getSimpleName();


	public static FinishedAlarmsFragment newInstance() {
		return new FinishedAlarmsFragment();
	}


    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {

        return () -> new AlarmTaskQueryBuilder(false)
                .sortByCreatedAtDescending()
                .whereStatus(ParseTask.STATUS.FINISHED, ParseTask.STATUS.ABORTED)
                .build();


    }



}
