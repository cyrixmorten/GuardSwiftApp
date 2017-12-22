package com.guardswift.ui.parse.execution;

import android.location.Location;
import android.util.Log;

import com.guardswift.eventbus.EventBusController;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQueryAdapter;

public abstract class AbstractTasksRecycleFragment extends AbstractParseRecyclerFragment<ParseTask, TaskRecycleAdapter.TaskViewHolder> {

    protected static final String TAG = AbstractTasksRecycleFragment.class
            .getSimpleName();


    public abstract ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory();

    public AbstractTasksRecycleFragment() {
        super(true);
    }

    @Override
    protected ParseRecyclerQueryAdapter<ParseTask, TaskRecycleAdapter.TaskViewHolder> createRecycleAdapter() {
        return new TaskRecycleAdapter(getContext(), getFragmentManager(), createNetworkQueryFactory());
    }




    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Object obj = ev.getObject();

        boolean isRelevant = false;

        if (obj instanceof Location) {
            isRelevant = true;
        }

        if (obj instanceof EventBusController.ForceUIUpdate) {
            isRelevant = true;
        }

        Log.d(TAG, "Abstract isRelevantUIEvent " + isRelevant);

        return isRelevant ;
    }




}
