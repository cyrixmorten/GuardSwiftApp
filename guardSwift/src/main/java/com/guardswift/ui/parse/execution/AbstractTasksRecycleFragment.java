package com.guardswift.ui.parse.execution;

import android.location.Location;
import android.util.Log;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQueryAdapter;

public abstract class AbstractTasksRecycleFragment<T extends BaseTask> extends AbstractParseRecyclerFragment<T, TaskRecycleAdapter.TaskViewHolder> {

    protected static final String TAG = AbstractTasksRecycleFragment.class
            .getSimpleName();


    public abstract BaseTask getObjectInstance();
    public abstract ParseQueryAdapter.QueryFactory<T> getNetworkQueryFactory();

    public AbstractTasksRecycleFragment() {
    }

    @Override
    protected ParseRecyclerQueryAdapter<T, TaskRecycleAdapter.TaskViewHolder> getRecycleAdapter() {
        ParseRecyclerQueryAdapter<T, TaskRecycleAdapter.TaskViewHolder> adapter = new TaskRecycleAdapter<>(getContext(), getNetworkQueryFactory());
        adapter.setFromLocalDataStore(true);
        return adapter;
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Object obj = ev.getObject();

        boolean isRelevant = false;

        if (obj instanceof Location) {
            isRelevant = true;
        }

        Log.d(TAG, "Abstract isRelevantUIEvent " + isRelevant);

        return isRelevant ;
    }




}
