package com.guardswift.ui.parse.execution.regular;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.exceptions.LogError;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.eventbus.events.BootstrapCompleted;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.persistence.parse.query.TaskGroupStartedQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.parse.planning.AddExtraTaskFragment;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

public class RegularTaskViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = RegularTaskViewPagerFragment.class
            .getSimpleName();


    public static RegularTaskViewPagerFragment newInstance(TaskGroupStarted circuitStarted) {

        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getTaskGroupStartedCache()
                .setSelected(circuitStarted);

        RegularTaskViewPagerFragment fragment = new RegularTaskViewPagerFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    TaskGroupStartedCache taskGroupStartedCache;

    @Inject
    GuardCache guardCache;

    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    private String nameOfSelectedTaskGroup;
    private MaterialDialog newTaskGroupAvailableDialog;
    private MaterialDialog progressDialog;

    public RegularTaskViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_tasks_all), RegularAndRaidTasksFragment.newInstance(getContext(), taskGroupStartedCache.getSelected()));
        //fragmentMap.put(getString(R.string.title_tasks_new), ActiveRegularTasksFragment.newInstance(getContext(), taskGroupStartedCache.getSelected()));
        //fragmentMap.put(getString(R.string.title_tasks_old), FinishedRegularTasksFragment.newInstance(getContext(), taskGroupStartedCache.getSelected()));

        nameOfSelectedTaskGroup = taskGroupStartedCache.getSelected().getName();

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.taskgroup, menu);

        menu.findItem(R.id.menu_add_extra_task).setOnMenuItemClickListener((menuItem) -> {
            GenericToolbarActivity.start(getContext(), R.string.extra_task, AddExtraTaskFragment.newInstance());
            return true;
        });


        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public void onAttach(Context context) {

        newTaskGroupAvailableDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).ok(R.string.update_data, getString(R.string.new_taskgroup_available), (dialog, which) -> {

            progressDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).indeterminate().show();

            new RegularRaidTaskQueryBuilder(true).build().findInBackground().onSuccessTask(task -> {
                Log.i(TAG, "Unpinning tasks");
                return ParseObject.unpinAllInBackground(task.getResult());
            }).onSuccessTask(task -> new TaskGroupStartedQueryBuilder(true).build().findInBackground()).onSuccessTask(task -> {
                Log.i(TAG, "Unpinning tasksGroups");
                return ParseObject.unpinAllInBackground(task.getResult());
            }).onSuccessTask(task -> {
                Log.i(TAG, "Teardown and bootstrap");
                GuardSwiftApplication.getInstance().teardownParseObjectsLocally(false);

                return GuardSwiftApplication.getInstance().bootstrapParseObjectsLocally(null, guardCache.getLoggedIn());
            }).continueWith(task -> {
                if (task.isFaulted()) {
                    LogError.log(TAG, "Update to new TaskGroup", task.getError());
                }
                return null;
            });

        }).build();

        super.onAttach(context);
    }

    public void onEventMainThread(BootstrapCompleted ev) {

        Log.i(TAG, "BootstrapCompleted");

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        try {
            TaskGroupStarted pinnedTaskGroup = new TaskGroupStartedQueryBuilder(true).matchingName(nameOfSelectedTaskGroup).build().getFirst();

            if (!pinnedTaskGroup.equals(taskGroupStartedCache.getSelected()) && getActivity() instanceof MainActivity) {

                Log.i(TAG, "pinnedTaskGroup not equal selected");

                MainActivity mainActivity = (MainActivity) getActivity();

                String dateSubtitle = DateUtils.formatDateTime(
                        getContext(),
                        pinnedTaskGroup.getCreatedAt().getTime(),
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);

                mainActivity.getMainDrawerCallback().setActionBarTitle(pinnedTaskGroup.getName(), dateSubtitle);

                taskGroupStartedCache.setSelected(pinnedTaskGroup);
            }
        } catch (ParseException e) {
            LogError.log(TAG, "Check TaskGroup", e);
        }
    }

    @Override
    public void onResume() {
        // Detect if there is newer taskGroupStarted available
        new TaskGroupStartedQueryBuilder(false).matching(taskGroupStartedCache.getSelected().getTaskGroup()).whereActive().build().getFirstInBackground((object, e) -> {
            if (object != null && getActivity() != null) {
                Date latestCreatedAt = object.getCreatedAt();
                Date currentCreatedAt = taskGroupStartedCache.getSelected().getCreatedAt();

                if (new DateTime(latestCreatedAt).isAfter(new DateTime(currentCreatedAt))) {
                    if (!newTaskGroupAvailableDialog.isShowing()) {
                        newTaskGroupAvailableDialog.show();
                    }
                }
            }
        });

        super.onResume();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.i(TAG, "onDestroyView");

        fragmentMap = null;

    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

