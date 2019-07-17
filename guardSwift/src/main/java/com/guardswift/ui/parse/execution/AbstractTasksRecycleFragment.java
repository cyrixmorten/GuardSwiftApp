package com.guardswift.ui.parse.execution;

import android.location.Location;

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

    public AbstractTasksRecycleFragment() { }

    @Override
    protected ParseRecyclerQueryAdapter<ParseTask, TaskRecycleAdapter.TaskViewHolder> createRecycleAdapter() {
        return new TaskRecycleAdapter(getContext(), getFragmentManager(), createNetworkQueryFactory());
    }




    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof EventBusController.ForceUIUpdate ||
                ev.getObject() instanceof Location;
    }




}
