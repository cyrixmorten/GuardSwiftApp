package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class FinishedStaticTasksFragment extends AbstractTasksRecycleFragment<StaticTask> {

	protected static final String TAG = FinishedStaticTasksFragment.class.getSimpleName();


	public static FinishedStaticTasksFragment newInstance() {
		return new FinishedStaticTasksFragment();
	}

	public FinishedStaticTasksFragment() {
	}


    @Override
    public PostProcessAdapterResults<StaticTask> createPostProcess() {
        return null;
    }

    @Override
    public BaseTask getObjectInstance() {
        return new StaticTask();
    }

    @Override
    public ParseQueryAdapter.QueryFactory<StaticTask> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<StaticTask>() {

            @Override
            public ParseQuery<StaticTask> create() {
                return new StaticTask.QueryBuilder(false).
                        finished().
                        daysBack(7).
                        sortedByTimeEnded().
                        build();
            }
        };
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return super.isRelevantUIEvent(ev) || ev.getObject() instanceof StaticTask || ev.getObject() instanceof EventLog;
    }


}
