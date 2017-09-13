package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.StaticTaskQueryBuilder;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class FinishedStaticTasksFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = FinishedStaticTasksFragment.class.getSimpleName();


	public static FinishedStaticTasksFragment newInstance() {
		return new FinishedStaticTasksFragment();
	}

	public FinishedStaticTasksFragment() {
	}


    @Override
    public PostProcessAdapterResults<ParseTask> createPostProcess() {
        return null;
    }

    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new StaticTaskQueryBuilder(false).
                        status(ParseTask.STATUS.FINISHED).
                        daysBack(7).
                        sortedByTimeEnded().
                        build();
            }
        };
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return super.isRelevantUIEvent(ev) || ev.getObject() instanceof ParseTask || ev.getObject() instanceof EventLog;
    }


}
