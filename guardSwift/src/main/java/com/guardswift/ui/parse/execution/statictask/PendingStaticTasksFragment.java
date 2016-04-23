package com.guardswift.ui.parse.execution.statictask;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class PendingStaticTasksFragment extends AbstractTasksRecycleFragment<StaticTask> {

	protected static final String TAG = PendingStaticTasksFragment.class.getSimpleName();


	public static PendingStaticTasksFragment newInstance() {
		return new PendingStaticTasksFragment();
	}

	public PendingStaticTasksFragment() {
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
                        pending().
                        daysBack(7).
                        sortedByCreated().
                        build();
            }
        };
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return super.isRelevantUIEvent(ev) || ev.getObject() instanceof StaticTask || ev.getObject() instanceof EventLog;
    }


}
