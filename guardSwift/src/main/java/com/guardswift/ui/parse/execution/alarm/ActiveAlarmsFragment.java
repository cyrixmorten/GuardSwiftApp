package com.guardswift.ui.parse.execution.alarm;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;


public class ActiveAlarmsFragment extends AbstractTasksRecycleFragment<ParseTask> {

	protected static final String TAG = ActiveAlarmsFragment.class
			.getSimpleName();


	public static ActiveAlarmsFragment newInstance() {
		return new ActiveAlarmsFragment();
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


        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new ParseTask().getQueryBuilder(false)
                        .whereStatus(ParseTask.STATUS.PENDING, ParseTask.STATUS.ACCEPTED, ParseTask.STATUS.ARRIVED)
                        .sortByCreatedAtDescending()
                        .build();

            }
        };


    }



}
