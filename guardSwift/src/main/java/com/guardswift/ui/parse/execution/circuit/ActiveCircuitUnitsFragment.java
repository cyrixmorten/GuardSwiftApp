package com.guardswift.ui.parse.execution.circuit;

import android.content.Context;
import android.util.Log;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class ActiveCircuitUnitsFragment extends AbstractTasksRecycleFragment<CircuitUnit> {

	protected static final String TAG = ActiveCircuitUnitsFragment.class.getSimpleName();


	public static ActiveCircuitUnitsFragment newInstance(Context context, CircuitStarted circuitStarted ) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getCircuitStartedCache()
                .setSelected(circuitStarted);

		return new ActiveCircuitUnitsFragment();
	}

	public ActiveCircuitUnitsFragment() {

	}

    @Inject
    CircuitStartedCache circuitStartedCache;


    @Override
    public PostProcessAdapterResults<CircuitUnit> createPostProcess() {
        return new PostProcessAdapterResults<CircuitUnit>() {
            @Override
            public List<CircuitUnit> postProcess(List<CircuitUnit> queriedItems) {
                if (queriedItems != null) {
                    Collections.sort(queriedItems);
                }
                return queriedItems;
            }
        };
    }

    @Override
    public BaseTask getObjectInstance() {
        return new CircuitUnit();
    }

    @Override
    public ParseQueryAdapter.QueryFactory<CircuitUnit> createNetworkQueryFactory() {

        Log.d(TAG, "circuitStartedCache.getSelected()" + circuitStartedCache.getSelected().getObjectId());

        return new ParseQueryAdapter.QueryFactory<CircuitUnit>() {

            @Override
            public ParseQuery<CircuitUnit> create() {
                return new CircuitUnit.QueryBuilder().
                        matchingNotEnded(circuitStartedCache.getSelected()).
                        isRunToday()
//                        sortBy(CircuitUnit.SORTBY_ID).
                        .build();
            }
        };
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Log.d(TAG, "Active circuitUnits isRelevant: " + super.isRelevantUIEvent(ev));

        return super.isRelevantUIEvent(ev);
    }


}
