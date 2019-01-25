package com.guardswift.ui.parse.data.taskgroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guardswift.R;
import com.guardswift.dagger.InjectingListFragment;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.query.TaskGroupQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.parse.ParseQueryAdapter;

public class TaskGroupListFragment extends InjectingListFragment {


    public interface OnTaskGroupSelectedListener {
        void taskGroupSelected(TaskGroup taskGroup);
    }

    public static TaskGroupListFragment newInstance(OnTaskGroupSelectedListener taskGroupSelectedListener) {

        TaskGroupListFragment fragment = new TaskGroupListFragment();

        fragment.setTaskGroupSelectedListener(taskGroupSelectedListener);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private OnTaskGroupSelectedListener taskGroupSelectedListener;

    public TaskGroupListFragment() {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    ParseQueryAdapter.QueryFactory<TaskGroup> factory =
            () -> new TaskGroupQueryBuilder(true).sortByName().build();

        ParseQueryAdapter<TaskGroup> adapter = new ParseQueryAdapter<>(getContext(), factory, android.R.layout.simple_list_item_1);

        adapter.setTextKey(TaskGroup.name);

        setListAdapter(adapter);

        getListView().setOnItemClickListener((parent, view, position, id) -> {
            TaskGroup taskGroup = adapter.getItem(position);

            this.taskGroupSelectedListener.taskGroupSelected(taskGroup);

            // TODO make optional from arguments
            Activity activity = this.getActivity();
            if (activity instanceof GenericToolbarActivity) {
                activity.finish();
            }
        });
    }

    public void setTaskGroupSelectedListener(OnTaskGroupSelectedListener taskGroupSelectedListener) {
        this.taskGroupSelectedListener = taskGroupSelectedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.gs_listview_selectable_fab, container,
                false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.taskGroupSelectedListener = null;
    }
}
