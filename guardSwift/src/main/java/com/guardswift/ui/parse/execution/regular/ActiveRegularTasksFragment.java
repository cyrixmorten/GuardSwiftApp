package com.guardswift.ui.parse.execution.regular;

import android.content.Context;
import android.util.Log;

import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ActiveRegularTasksFragment extends AbstractTasksRecycleFragment {

    protected static final String TAG = ActiveRegularTasksFragment.class.getSimpleName();


    public static ActiveRegularTasksFragment newInstance(Context context, TaskGroupStarted circuitStarted) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTaskGroupStartedCache()
                .setSelected(circuitStarted);

        return new ActiveRegularTasksFragment();
    }

    public ActiveRegularTasksFragment() {

    }

    @Inject
    TaskGroupStartedCache circuitStartedCache;


    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {

        Log.d(TAG, "taskGroupStartedCache.getSelected()" + circuitStartedCache.getSelected().getObjectId());

        return new ParseQueryAdapter.QueryFactory<ParseTask>() {

            @Override
            public ParseQuery<ParseTask> create() {
                return new RegularRaidTaskQueryBuilder(false).
                        matchingNotEnded(circuitStartedCache.getSelected()).
                        isRunToday()
//                        sortBy(ParseTask.SORTBY_ID).
                        .build();
            }
        };
    }




}
