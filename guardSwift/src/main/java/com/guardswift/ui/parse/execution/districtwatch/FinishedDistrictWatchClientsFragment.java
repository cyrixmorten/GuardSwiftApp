package com.guardswift.ui.parse.execution.districtwatch;

import android.content.Context;

import com.guardswift.persistence.cache.planning.DistrictWatchStartedCache;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class FinishedDistrictWatchClientsFragment extends AbstractTasksRecycleFragment<DistrictWatchClient> {

    protected static final String TAG = ActiveDistrictWatchClientsFragment.class
            .getSimpleName();

    public static FinishedDistrictWatchClientsFragment newInstance(Context context, DistrictWatchStarted districtWatchStarted) {

        GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchStartedCache().setSelected(districtWatchStarted);

        return new FinishedDistrictWatchClientsFragment();
    }


    @Inject
    DistrictWatchStartedCache districtWatchStartedCache;

    @Override
    public PostProcessAdapterResults<DistrictWatchClient> createPostProcess() {
        return null;
    }

    @Override
    public BaseTask getObjectInstance() {
        return new DistrictWatchClient();
    }


    @Override
    public ParseQueryAdapter.QueryFactory<DistrictWatchClient> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<DistrictWatchClient>() {

            @Override
            public ParseQuery<DistrictWatchClient> create() {
                return new DistrictWatchClient.QueryBuilder(false).
                        matching(districtWatchStartedCache.getSelected().getDistrictWatch()).
                        whereTimesArrivedEqualsExpected().
                        whereVisited().
                        isRunToday().
                        sortBy(DistrictWatchClient.SORTBY_ADDRESS).
                        build();
            }
        };
    }


}