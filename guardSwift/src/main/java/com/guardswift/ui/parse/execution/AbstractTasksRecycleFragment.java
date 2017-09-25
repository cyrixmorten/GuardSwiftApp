package com.guardswift.ui.parse.execution;

import android.location.Location;
import android.util.Log;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PostProcessAdapterResults;
import com.parse.ParseQueryAdapter;

public abstract class AbstractTasksRecycleFragment extends AbstractParseRecyclerFragment<ParseTask, TaskRecycleAdapter.TaskViewHolder> {

    protected static final String TAG = AbstractTasksRecycleFragment.class
            .getSimpleName();


    public abstract PostProcessAdapterResults<ParseTask> createPostProcess();
    public abstract ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory();

    public AbstractTasksRecycleFragment() {
    }

    @Override
    protected ParseRecyclerQueryAdapter<ParseTask, TaskRecycleAdapter.TaskViewHolder> createRecycleAdapter() {
        ParseRecyclerQueryAdapter<ParseTask, TaskRecycleAdapter.TaskViewHolder> adapter = new TaskRecycleAdapter(getContext(), getFragmentManager(), createNetworkQueryFactory());

        PostProcessAdapterResults<ParseTask> postProcess = createPostProcess();
        if (postProcess != null) {
            adapter.setPostProcessor(postProcess);
        }

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
