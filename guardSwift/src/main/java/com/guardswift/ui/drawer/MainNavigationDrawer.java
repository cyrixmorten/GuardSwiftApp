package com.guardswift.ui.drawer;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingActivityModule;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.data.guard.GuardListFragment;
import com.guardswift.ui.parse.data.tracker.TrackerListFragment;
import com.guardswift.ui.parse.execution.alarm.AlarmsViewPagerFragment;
import com.guardswift.ui.parse.execution.regular.RegularTaskViewPagerFragment;
import com.guardswift.ui.parse.execution.statictask.StaticTaskViewPagerFragment;
import com.guardswift.ui.parse.planning.ListExtraTasksFragment;
import com.guardswift.ui.preferences.AlarmNotificationPreferencesFragment;
import com.guardswift.ui.preferences.GuardPreferencesFragment;
import com.guardswift.util.Analytics;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.ParseException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainNavigationDrawer extends BaseNavigationDrawer {

    private static final String TAG = MainNavigationDrawer.class.getSimpleName();


    public static final int DRAWER_LOGOUT = 0;

    @Inject()
    @InjectingActivityModule.ForActivity
    Context context;

    @Inject()
    GuardCache guardCache;
    @Inject()
    TaskGroupStartedCache circuitStartedCache;

    private Drawer navigationDrawer;
    private FragmentDrawerCallback fragmentDrawerCallback;


    @Inject
    public MainNavigationDrawer() {
    }

    public Drawer getDrawer() {
        return navigationDrawer;
    }

    public Drawer create(FragmentActivity activity, Toolbar toolbar, final FragmentDrawerCallback fragmentDrawerCallback) {

        this.fragmentDrawerCallback = fragmentDrawerCallback;


        // initPreferences navigationdrawer
        navigationDrawer = new DrawerBuilder()
                .withAccountHeader(this.getHeader(activity))
                .withActivity(activity)
                .withToolbar(toolbar)
                .withSelectedItem(-1) // defaults to no selection
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem == null) {
                        return false;
                    }

                    if (drawerItem.getTag() instanceof TaskGroupStarted) {
                        Log.w(TAG, "Clicked taskGroupStarted");
                        TaskGroupStarted clickedTaskGroupStarted = (TaskGroupStarted) drawerItem.getTag();
                        for (IDrawerItem item : circuitItems) {
                            TaskGroupStarted taskGroupStarted = (TaskGroupStarted) item.getTag();
                            if (taskGroupStarted != null && clickedTaskGroupStarted.getObjectId().equals(taskGroupStarted.getObjectId())) {
                                Log.w(TAG, "Clicked " + taskGroupStarted.getName());

                                String dateSubtitle = DateUtils.formatDateTime(
                                        context,
                                        taskGroupStarted.getCreatedAt().getTime(),
                                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);

                                fragmentDrawerCallback.selectItem(RegularTaskViewPagerFragment.newInstance(taskGroupStarted), taskGroupStarted.getName(), dateSubtitle);

                                return false; // close drawer
                            }
                        }
                    }

                    fragmentDrawerCallback.selectItem(drawerItem.getIdentifier());

                    return false;
                })
                .build();

        CommonDrawerItems commonDrawerItems = new CommonDrawerItems(activity);
        navigationDrawer.addItems(commonDrawerItems.thisDevice());
        navigationDrawer.addItems(getGuardDataDrawerItem());

        if (guardCache.getLoggedIn().canAccessAlarms()) {
            navigationDrawer.addItems(getAlarmsDrawerItems());
        }

        if (guardCache.getLoggedIn().canAccessRegularTasks()) {
            navigationDrawer.addItems(getActiveCircuitDrawerItems());
        }


        if (guardCache.getLoggedIn().canAccessStaticTasks()) {
            navigationDrawer.addItems(getStaticGuardingItems());
        }

        navigationDrawer.addItems(getAdminItems());

        navigationDrawer.addStickyFooterItem(getLogoutDrawerItem());

        IDrawerItem selected = getSelectedTaskGroupDrawerItem();

        if (selected != null) {
            navigationDrawer.setSelection(selected, true);
        } else {
            navigationDrawer.openDrawer();
        }

        return navigationDrawer;
    }

    private AccountHeader getHeader(Activity activity) {
        // Create navigation header
        return new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(guardCache.getLoggedIn().getName()).withIcon(R.drawable.ic_person_black_36dp)
                )
                .withOnAccountHeaderListener((view, profile, currentProfile) -> false)
                .withSelectionListEnabledForSingleProfile(false).build();
    }

    private IDrawerItem getAddExtraTaskItem() {
        return new PrimaryDrawerItem().withName(context.getString(R.string.extra_task)).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            GenericToolbarActivity.start(context, R.string.extra_task, ListExtraTasksFragment.newInstance());
            return false;
        });
    }

    private IDrawerItem getSelectedTaskGroupDrawerItem() {

        // Determine initial selection
        TaskGroupStarted selectedCircuitStarted = circuitStartedCache.getSelected();
        IDrawerItem selected = null;
        if (selectedCircuitStarted != null && circuitItems != null) {
            for (IDrawerItem item : circuitItems) {
                TaskGroupStarted circuitStarted = (TaskGroupStarted) item.getTag();
                if (circuitStarted != null && selectedCircuitStarted.getObjectId().equals(circuitStarted.getObjectId())) {
                    Log.w(TAG, "Initial: " + circuitStarted.getName() + " " + item);
                    selected = item;
                }
            }
        }

        return selected;
    }

    private IDrawerItem existingStaticGuardingReports;

    private IDrawerItem[] getStaticGuardingItems() {
        List<IDrawerItem> staticItems = Lists.newArrayList();
        IDrawerItem staticHeader = new SectionDrawerItem().withName(R.string.static_guarding);

        existingStaticGuardingReports = new PrimaryDrawerItem().withName(context.getString(R.string.existing_reports)).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            fragmentDrawerCallback.selectItem(StaticTaskViewPagerFragment.newInstance(), context.getString(R.string.static_guarding_reports));
            return true;
        });

        IDrawerItem createNewStaticGuardingReport = new PrimaryDrawerItem().withName(context.getString(R.string.create_new)).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            ClientListFragment clientListFragment = ClientListFragment.newInstance(ClientQueryBuilder.SORT_BY.DISTANCE);
            clientListFragment.setOnClientSelectedListener(this::createReport);
            fragmentDrawerCallback.selectItem(clientListFragment, context.getString(R.string.new_static_guarding), context.getString(R.string.select_client));
            return false;
        });

        staticItems.add(staticHeader);
        staticItems.add(createNewStaticGuardingReport);
        staticItems.add(existingStaticGuardingReports);

        return staticItems.toArray(new IDrawerItem[0]);
    }

    private List<IDrawerItem> circuitItems;

    private IDrawerItem[] getActiveCircuitDrawerItems() {
        circuitItems = Lists.newArrayList();
        IDrawerItem circuitHeader = new SectionDrawerItem().withName(R.string.title_drawer_circuits);

        try {
            List<TaskGroupStarted> circuitsStarted = TaskGroupStarted.getQueryBuilder(true).sortByName().whereActive().build().find();


            // TODO: Patch fix duplicates in drawer
            boolean hasDuplicates = false;
            HashMap<String, TaskGroupStarted> uniqueCircuitsStarted = Maps.newHashMap();
            for (TaskGroupStarted circuitStarted : circuitsStarted) {
                String key = circuitStarted.getName();
                TaskGroupStarted unique = uniqueCircuitsStarted.get(key);
                if (unique != null) {
                    if (circuitStarted.getCreatedAt().after(unique.getCreatedAt())) {
                        uniqueCircuitsStarted.put(key, circuitStarted);
                        hasDuplicates = true;
                    }
                } else {
                    uniqueCircuitsStarted.put(key, circuitStarted);
                }
            }


            // Analytics to see how often duplicates are encountered
            Analytics.sendEvent("Fix", "Duplicate circuits in drawer", hasDuplicates ? "yes" : "no");

            if (hasDuplicates) {
                circuitsStarted = Lists.newArrayList(uniqueCircuitsStarted.values());
            }

            Collections.sort(circuitsStarted);

            circuitItems.add(circuitHeader);
            for (TaskGroupStarted circuitStarted : circuitsStarted) {
                Log.d(TAG, "getName(): " + circuitStarted.getName());
                IDrawerItem circuitItem = new PrimaryDrawerItem().withName(circuitStarted.getName()).withTag(circuitStarted);
                circuitItems.add(circuitItem);
            }
        } catch (ParseException e) {
            new HandleException(context, TAG, "getActiveCircuitDrawerItems", e);
        }

        // don't leave header if there is no task groups
        if (circuitItems.size() == 1) {
            circuitItems.clear();
        }

        if (!circuitItems.isEmpty()) {
            circuitItems.add(1, getAddExtraTaskItem());
        }

        return circuitItems.toArray(new IDrawerItem[0]);
    }

    private IDrawerItem[] getAlarmsDrawerItems() {
        List<IDrawerItem> alarmItems = Lists.newArrayList();
        IDrawerItem alarmHeader = new SectionDrawerItem().withName(R.string.title_drawer_alarms);
        alarmItems.add(alarmHeader);

        IDrawerItem alarmItem = new PrimaryDrawerItem().withName(R.string.alarms).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            fragmentDrawerCallback.selectItem(AlarmsViewPagerFragment.newInstance(), R.string.alarms);
            return false;
        });
        alarmItems.add(alarmItem);

        IDrawerItem alarmPreferences = new PrimaryDrawerItem().withName(R.string.settings).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            fragmentDrawerCallback.selectItem(AlarmNotificationPreferencesFragment.newInstance(), R.string.settings_alarm);
            return false;
        });
        alarmItems.add(alarmPreferences);

        return alarmItems.toArray(new IDrawerItem[0]);
    }

    private void createReport(final Client client) {
        final MaterialDialog dialog = new CommonDialogsBuilder.MaterialDialogs(context).indeterminate(client.getName(), R.string.creating_report).show();
        // fake a bit of delay to ensure that the dialog is shown/readable
        new Handler(Looper.getMainLooper()).postDelayed(() -> ParseTask.createStaticTask(client, (task, e) -> {

            dialog.dismiss();

            if (e != null) {
                if (context != null) {
                    new CommonDialogsBuilder.MaterialDialogs(context).missingInternetContent().show();
                }
                new HandleException(TAG, "save static report", e);
                return;
            }

            // show as pending
            fragmentDrawerCallback.selectItem(StaticTaskViewPagerFragment.newInstance(), context.getString(R.string.static_guarding_reports));
            if (existingStaticGuardingReports != null) {
                navigationDrawer.setSelection(existingStaticGuardingReports);
            }

        }), 500);
    }


    private IDrawerItem[] getAdminItems() {
        if (!guardCache.getLoggedIn().hasRole(Guard.Role.ADMIN)) {
            return new IDrawerItem[]{};
        }

        List<IDrawerItem> dataItems = Lists.newArrayList();
        // DATA
        IDrawerItem dataHeader = new SectionDrawerItem().withName(R.string.title_drawer_data);
        dataItems.add(dataHeader);
        dataItems.add(new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_guards)).withSelectable(false).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            GenericToolbarActivity.start(context, R.string.title_drawer_guards, GuardListFragment.newInstance());
            return false;
        }));
        dataItems.add(new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_clients)).withSelectable(false).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            ClientListFragment clientListFragment = ClientListFragment.newInstance(ClientQueryBuilder.SORT_BY.NAME);
            GenericToolbarActivity.start(context, R.string.title_drawer_clients, clientListFragment);
            return false;
        }));


        IDrawerItem adminHeader = new SectionDrawerItem().withName(R.string.title_drawer_admin);
        dataItems.add(adminHeader);
        dataItems.add(new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_gps_history)).withSelectable(false).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            GenericToolbarActivity.start(context, R.string.title_drawer_gps_history, TrackerListFragment.newInstance());
            return false;
        }));

        return dataItems.toArray(new IDrawerItem[0]);
    }

    private IDrawerItem getLogoutDrawerItem() {
        return new PrimaryDrawerItem().withIdentifier(DRAWER_LOGOUT).withName(context.getString(R.string.title_drawer_logout)).withIcon(FontAwesome.Icon.faw_sign_out);
    }

    private IDrawerItem getGuardDataDrawerItem() {
        return new PrimaryDrawerItem().withName(context.getString(R.string.my_data)).withSelectable(false).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            GenericToolbarActivity.start(context, R.string.settings, R.string.my_data, GuardPreferencesFragment.newInstance());
            return false;
        });
    }


}
