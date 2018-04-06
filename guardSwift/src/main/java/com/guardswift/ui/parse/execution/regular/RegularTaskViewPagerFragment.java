package com.guardswift.ui.parse.execution.regular;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.exceptions.LogError;
import com.guardswift.eventbus.events.BootstrapCompleted;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.RegularRaidTaskQueryBuilder;
import com.guardswift.persistence.parse.query.TaskGroupStartedQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.drawer.MessagesDrawer;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
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

        Log.e(TAG, "SHOW: " + circuitStarted.getName());
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

    private MessagesDrawer messagesDrawer;
    private MenuItem messagesMenu;

    private String nameOfSelectedTaskgroup;
    private MaterialDialog newTaskgroupAvailableDialog;
    private MaterialDialog progressDialog;

    public RegularTaskViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_tasks_new), ActiveRegularTasksFragment.newInstance(getContext(), taskGroupStartedCache.getSelected()));
        fragmentMap.put(getString(R.string.title_tasks_old), FinishedRegularTasksFragment.newInstance(getContext(), taskGroupStartedCache.getSelected()));

        nameOfSelectedTaskgroup = taskGroupStartedCache.getSelected().getName();

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {

        newTaskgroupAvailableDialog = new CommonDialogsBuilder.MaterialDialogs(getActivity()).ok(R.string.update_data, getString(R.string.new_taskgroup_available), (dialog, which) -> {

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
            TaskGroupStarted pinnedTaskGroup = new TaskGroupStartedQueryBuilder(true).matchingName(nameOfSelectedTaskgroup).build().getFirst();

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
                    if (!newTaskgroupAvailableDialog.isShowing()) {
                        newTaskgroupAvailableDialog.show();
                    }
                }
            }
        });

        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.taskgroup, menu);

        messagesMenu = menu.findItem(R.id.menu_messages);
        messagesMenu.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }


    private void updateMessageMenuBadge(int messagesCount, final boolean hide) {
        Log.i(TAG, "updateMessageMenuBadge: " + messagesCount + " " + hide);

        if (messagesCount == 0) {
            // Hide the badge https://github.com/mikepenz/Android-ActionItemBadge/issues/9
            messagesCount = Integer.MIN_VALUE;
        }

        if (getActivity() != null && getContext() != null && messagesMenu != null) {

//                IconicsDrawable messagesIcon = new IconicsDrawable(getContext())
//                        .icon(GoogleMaterial.Icon.gmd_email)
//                        .color(Color.DKGRAY)
//                        .sizeDp(24);

            final int finalMessagesCount = messagesCount;

            new Handler(getContext().getMainLooper()).postAtFrontOfQueue(() -> {
                ActionItemBadge.update(getActivity(), messagesMenu, ContextCompat.getDrawable(getContext(), R.drawable.ic_forum_black_24dp), ActionItemBadge.BadgeStyles.RED, finalMessagesCount);
                messagesMenu.setVisible(!hide);
            });

        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_messages) {
            if (messagesDrawer != null) {
                messagesDrawer.open();
                ActionItemBadge.update(messagesMenu, Integer.MIN_VALUE);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            messagesDrawer = ((MainActivity) activity).getMessagesDrawer();

            loadMessages();
        }
    }

    private void loadMessages() {
        Log.i(TAG, "loadMessages");

        if (messagesDrawer == null) {
            return;
        }

        messagesDrawer.loadMessages(getMessagesGroupId()).continueWith(task -> {
            if (task.isFaulted()) {
                LogError.log(TAG, "Failed to load messages", task.getError());

                updateMessageMenuBadge(0, true);

                return null;
            }

            updateMessageMenuBadge(messagesDrawer.getNewMessagesCount(), false);

            return null;
        });
    }

    private String getMessagesGroupId() {
        TaskGroupStarted taskGroupStarted = taskGroupStartedCache.getSelected();
        if (taskGroupStarted != null) {
            TaskGroup taskGroup = taskGroupStarted.getTaskGroup();
            if (taskGroup != null) {
                return taskGroup.getObjectId();
            }
        }
        return "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.i(TAG, "onDestroyView");

        messagesDrawer = null;
        messagesMenu = null;
        fragmentMap = null;

    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

