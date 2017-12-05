package com.guardswift.ui.parse.execution.alarm;

import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.AlarmTaskQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;


public class FinishedAlarmsFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = FinishedAlarmsFragment.class
			.getSimpleName();


	public static FinishedAlarmsFragment newInstance() {
		return new FinishedAlarmsFragment();
	}


    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {

        final Guard guard = GuardSwiftApplication.getLoggedIn();

        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new AlarmTaskQueryBuilder(false)
                        .sortByCreatedAtDescending()
                        .whereStatus(ParseTask.STATUS.FINISHED, ParseTask.STATUS.ABORTED)
                        .build();
            }
        };


    }



}
