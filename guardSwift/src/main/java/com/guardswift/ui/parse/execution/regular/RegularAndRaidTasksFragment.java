package com.guardswift.ui.parse.execution.regular;

import android.content.Context;

import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class RegularAndRaidTasksFragment extends AbstractTasksRecycleFragment {

    protected static final String TAG = RegularAndRaidTasksFragment.class.getSimpleName();


    public static RegularAndRaidTasksFragment newInstance(Context context, TaskGroupStarted taskGroupStarted) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTaskGroupStartedCache()
                .setSelected(taskGroupStarted);

        return new RegularAndRaidTasksFragment();
    }

    @Inject
    TaskGroupStartedCache taskGroupStartedCache;

    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return () -> new RegularRaidTaskQueryBuilder(false)
                .matching(taskGroupStartedCache.getSelected().getTaskGroup())
                .isRunToday()
                .build();
    }




}
