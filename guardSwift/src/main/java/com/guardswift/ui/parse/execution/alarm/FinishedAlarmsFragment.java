package com.guardswift.ui.parse.execution.alarm;

import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;


public class FinishedAlarmsFragment extends AbstractTasksRecycleFragment<ParseTask> {

	protected static final String TAG = FinishedAlarmsFragment.class
			.getSimpleName();


	public static FinishedAlarmsFragment newInstance() {
		return new FinishedAlarmsFragment();
	}

    @Override
    public PostProcessAdapterResults<ParseTask> createPostProcess() {
        return null;
    }

    @Override
    public BaseTask getObjectInstance() {
        return new ParseTask();
    }

    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {

        final Guard guard = GuardSwiftApplication.getLoggedIn();

        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new ParseTask().getQueryBuilder(false)
                        .sortByTimeEnded()
                        .whereStatus(ParseTask.STATUS.FINISHED)
                        .build();
            }
        };


    }



}
