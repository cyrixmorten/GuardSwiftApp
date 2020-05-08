package com.guardswift.ui.parse.execution.regular;

import android.content.Context;

import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ui.widget.ParseQueryAdapter;

import javax.inject.Inject;

public class FinishedRegularTasksFragment extends AbstractTasksRecycleFragment {

	protected static final String TAG = FinishedRegularTasksFragment.class.getSimpleName();

	public static FinishedRegularTasksFragment newInstance(Context context, TaskGroupStarted taskGroupStarted) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTaskGroupStartedCache()
                .setSelected(taskGroupStarted);

        return new FinishedRegularTasksFragment();
	}

    @Inject
    TaskGroupStartedCache taskGroupStarted;

	public FinishedRegularTasksFragment() {
	}


    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return () ->
                new RegularRaidTaskQueryBuilder(false).
                    matchingEnded(taskGroupStarted.getSelected()).
                    isRunToday().
                    sortBy(RegularRaidTaskQueryBuilder.SORTBY_ID).
                    build();
    }


}

