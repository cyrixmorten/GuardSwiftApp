package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.StaticTaskQueryBuilder;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class ActiveStaticTasksFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = ActiveStaticTasksFragment.class.getSimpleName();


	public static ActiveStaticTasksFragment newInstance() {
		return new ActiveStaticTasksFragment();
	}

	public ActiveStaticTasksFragment() {
	}

    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new StaticTaskQueryBuilder(false).
                        status(ParseTask.STATUS.ARRIVED).
                        daysBack(7).
                        sortedByCreated().
                        build();
            }
        };
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return false;
    }


}
