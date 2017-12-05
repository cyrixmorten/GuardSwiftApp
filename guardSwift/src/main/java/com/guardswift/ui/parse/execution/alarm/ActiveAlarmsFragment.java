package com.guardswift.ui.parse.execution.alarm;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.AlarmTaskQueryBuilder;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;


public class ActiveAlarmsFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = ActiveAlarmsFragment.class
			.getSimpleName();


	public static ActiveAlarmsFragment newInstance() {
		return new ActiveAlarmsFragment();
	}

	public ActiveAlarmsFragment() {
        // update view using network query on resume
        this.setReloadOnResume(true);
	}

    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {


        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new AlarmTaskQueryBuilder(false)
                        .whereStatus(ParseTask.STATUS.PENDING, ParseTask.STATUS.ACCEPTED, ParseTask.STATUS.ARRIVED)
                        .sortByCreatedAtDescending()
                        .build();

            }
        };


    }



}
