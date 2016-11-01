package com.guardswift.ui.parse.execution.circuit;

import android.content.Context;
import android.util.Log;

import com.guardswift.core.tasks.controller.CircuitUnitController;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.guardswift.ui.parse.execution.AbstractTasksRecycleFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class FinishedCircuitUnitsFragment extends AbstractTasksRecycleFragment<CircuitUnit> {

	protected static final String TAG = FinishedCircuitUnitsFragment.class.getSimpleName();

	public static FinishedCircuitUnitsFragment newInstance(Context context, CircuitStarted circuitStarted) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getCircuitStartedCache()
                .setSelected(circuitStarted);

        return new FinishedCircuitUnitsFragment();
	}

    @Inject
    CircuitStartedCache circuitStartedCache;
    @Inject
    CircuitUnitController controller;

	public FinishedCircuitUnitsFragment() {
	}

    @Override
    public PostProcessAdapterResults<CircuitUnit> createPostProcess() {
        return null;
    }

    @Override
    public BaseTask getObjectInstance() {
        return new CircuitUnit();
    }

    @Override
    public ParseQueryAdapter.QueryFactory<CircuitUnit> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<CircuitUnit>() {

            @Override
            public ParseQuery<CircuitUnit> create() {
                return
                        new CircuitUnit.QueryBuilder().
                        matchingEnded(circuitStartedCache.getSelected()).
                        isRunToday().
                        sortBy(CircuitUnit.SORTBY_ID).
                        build();
            }
        };
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Object obj = ev.getObject();

        boolean isRelevant = super.isRelevantUIEvent(ev);

        if (obj instanceof GSTask) {
            boolean isSameTaskType = ((GSTask) obj).getTaskType() == ((GSTask)getObjectInstance()).getTaskType();
            if (isSameTaskType) {
                GSTask.TASK_STATE state = ((GSTask) obj).getTaskState();
                if (state == GSTask.TASK_STATE.FINSIHED) {
                    isRelevant = true;
                }
            }
        }

        Log.d(TAG, "Finished circuitUnits isRelevant: " + obj + " -> " + isRelevant);

        return isRelevant;
    }
}

