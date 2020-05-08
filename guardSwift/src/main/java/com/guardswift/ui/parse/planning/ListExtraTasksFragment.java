package com.guardswift.ui.parse.planning;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.guardswift.R;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.menu.MenuItemBuilder;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.execution.TaskRecycleAdapter;
import com.parse.ui.widget.ParseQueryAdapter;

public class ListExtraTasksFragment extends AbstractParseRecyclerFragment<ParseTask, TaskRecycleAdapter.TaskViewHolder> {

    protected static final String TAG = ListExtraTasksFragment.class.getSimpleName();


    public static ListExtraTasksFragment newInstance() {
        return new ListExtraTasksFragment();
    }

    public ListExtraTasksFragment() {
        setHasOptionsMenu(true);
        setReloadOnResume();
    }

    @Override
    protected ParseRecyclerQueryAdapter<ParseTask, TaskRecycleAdapter.TaskViewHolder> createRecycleAdapter() {
        TaskRecycleAdapter adapter = new TaskRecycleAdapter(getContext(), getParentFragmentManager(), createNetworkQueryFactory());
        adapter.setOpenTaskListener((task) -> GenericToolbarActivity.start(getContext(), R.string.create_extra_task, AddExtraTaskFragment.newInstance(task)));
        return adapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        new MenuItemBuilder(getContext())
                .showAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .addToMenu(menu, R.string.create_new, menuItem -> {

                    GenericToolbarActivity.start(getContext(), R.string.create_extra_task, AddExtraTaskFragment.newInstance());

                    return false;
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public ParseQueryAdapter.QueryFactory<ParseTask> createNetworkQueryFactory() {
        return () -> new RegularRaidTaskQueryBuilder(false)
                .isExtraTask()
                .build();
    }

    @Override
    public boolean isRelevantUIEvent(UpdateUIEvent ev) {
        Object obj = ev.getObject();
        if (obj instanceof ParseTask) {
//            reloadAdapter();
            return true;
        }

        return false;
    }
}
