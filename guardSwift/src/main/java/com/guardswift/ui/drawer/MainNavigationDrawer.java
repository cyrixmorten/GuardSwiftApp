package com.guardswift.ui.drawer;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingActivityModule;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.data.client.ClientDetailsFragment;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.data.guard.GuardListFragment;
import com.guardswift.ui.parse.data.tracker.TrackerListFragment;
import com.guardswift.ui.parse.execution.alarm.AlarmsViewPagerFragment;
import com.guardswift.ui.parse.execution.circuit.CircuitViewPagerFragment;
import com.guardswift.ui.parse.execution.districtwatch.DistrictwatchViewPagerFragment;
import com.guardswift.ui.parse.execution.statictask.StaticTaskViewPagerFragment;
import com.guardswift.ui.preferences.AlarmNotificationPreferencesFragment;
import com.guardswift.ui.preferences.GuardPreferencesFragment;
import com.guardswift.util.Analytics;
import com.guardswift.util.ToastHelper;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.GetCallback;
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

    private final Context context;
    private final GuardCache guardCache;
    private final CircuitStartedCache circuitStartedCache;

    private Drawer navigationDrawer;
    private FragmentDrawerCallback fragmentDrawerCallback;


    @Inject
    public MainNavigationDrawer(@InjectingActivityModule.ForActivity Context context) {
        this.context = context;

        ParseCacheFactory parseCacheFactory = GuardSwiftApplication.getInstance().getCacheFactory();
        this.guardCache = parseCacheFactory.getGuardCache();
        this.circuitStartedCache = parseCacheFactory.getCircuitStartedCache();
    }

    public Drawer getDrawer() {
        return navigationDrawer;
    }

    public Drawer initNavigationDrawer(FragmentActivity activity, Toolbar toolbar, final FragmentDrawerCallback fragmentDrawerCallback) {

        this.fragmentDrawerCallback = fragmentDrawerCallback;


        // initPreferences navigationdrawer
        navigationDrawer = new DrawerBuilder()
                .withAccountHeader(this.getHeader(activity))
                .withActivity(activity)
                .withToolbar(toolbar)
                .withSelectedItem(-1) // defaults to no selection
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == null) {
                            return false;
                        }

                        long id = drawerItem.getIdentifier();

                        if (drawerItem.getTag() instanceof CircuitStarted) {
                            Log.w(TAG, "Clicked circuitStarted");
                            CircuitStarted clickedCircuitStarted = (CircuitStarted) drawerItem.getTag();
                            for (IDrawerItem item : circuitItems) {
                                CircuitStarted circuitStarted = (CircuitStarted) item.getTag();
                                if (circuitStarted != null && clickedCircuitStarted.getObjectId().equals(circuitStarted.getObjectId())) {
                                    Log.w(TAG, "Clicked " + circuitStarted.getName());
                                    fragmentDrawerCallback.selectItem(CircuitViewPagerFragment.newInstance(context, circuitStarted), circuitStarted.getName());
                                    return false; // close drawer
                                }
                            }
                        }

                        fragmentDrawerCallback.selectItem(id);

                        return false;
                    }
                })
                .build();

        CommonDrawerItems commonDrawerItems = new CommonDrawerItems(activity);
        navigationDrawer.addItems(commonDrawerItems.thisDevice());
        navigationDrawer.addItems(getGuardDataDrawerItem());
        addTaskItems();


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
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                    return false;
                    }
                })
                .withSelectionListEnabledForSingleProfile(false).build();
    }

    private void addTaskItems() {
        if (guardCache.getLoggedIn().canAccessAlarms()) {
            navigationDrawer.addItems(getAlarmsDrawerItems());
        }

        if (guardCache.getLoggedIn().canAccessRegularTasks()) {
            navigationDrawer.addItems(getActiveCircuitDrawerItems());
        }

        if (guardCache.getLoggedIn().canAccessDistrictTasks()) {
            navigationDrawer.addItems(getActiveDistrictWatchDrawerItems());
        }

        if (guardCache.getLoggedIn().canAccessStaticTasks()) {
            navigationDrawer.addItems(getStaticGuardingItems());
        }


        navigationDrawer.addItems(getDataDrawerItems());
        navigationDrawer.addStickyFooterItem(getLogoutDrawerItem());


        // Determine initial selection
        CircuitStarted selectedCircuitStarted = circuitStartedCache.getSelected();
        IDrawerItem selected = null;
        if (selectedCircuitStarted != null && circuitItems != null) {
            for (IDrawerItem item : circuitItems) {
                CircuitStarted circuitStarted = (CircuitStarted) item.getTag();
                if (circuitStarted != null && selectedCircuitStarted.getObjectId().equals(circuitStarted.getObjectId())) {
                    Log.w(TAG, "Initial: " + circuitStarted.getName() + " " + item);
                    selected = item;
                }
            }
        }
        if (selected != null) {
            int position = navigationDrawer.getPosition(selected);
            Log.w(TAG, "Selected: " + ((CircuitStarted) selected.getTag()).getName());
            Log.w(TAG, "Position: " + position);
            navigationDrawer.setSelection(selected, true);
        } else {
            navigationDrawer.openDrawer();
        }
    }

    private IDrawerItem existingStaticGuardingReports;

    private IDrawerItem[] getStaticGuardingItems() {
        List<IDrawerItem> staticItems = Lists.newArrayList();
        IDrawerItem staticHeader = new SectionDrawerItem().withName(R.string.static_guarding);

        existingStaticGuardingReports = new PrimaryDrawerItem().withName(context.getString(R.string.existing_reports)).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                fragmentDrawerCallback.selectItem(StaticTaskViewPagerFragment.newInstance(), context.getString(R.string.static_guarding_reports));
                return true;
            }
        });

        IDrawerItem createNewStaticGuardingReport = new PrimaryDrawerItem().withName(context.getString(R.string.create_new)).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                ClientListFragment clientListFragment = ClientListFragment.newInstance(Client.SORT_BY.DISTANCE);
                clientListFragment.setOnClientSelectedListener(new ClientListFragment.OnClientSelectedListener() {
                    @Override
                    public void clientSelected(Client client) {
                        createReport(client);
                    }
                });
                fragmentDrawerCallback.selectItem(clientListFragment, context.getString(R.string.new_static_guarding), context.getString(R.string.select_client));
                return false;
            }
        });

        staticItems.add(staticHeader);
        staticItems.add(createNewStaticGuardingReport);
        staticItems.add(existingStaticGuardingReports);

        return staticItems.toArray(new IDrawerItem[staticItems.size()]);
    }

    private List<IDrawerItem> circuitItems;

    private IDrawerItem[] getActiveCircuitDrawerItems() {
        circuitItems = Lists.newArrayList();
        IDrawerItem circuitHeader = new SectionDrawerItem().withName(R.string.title_drawer_circuits);

        try {
            List<CircuitStarted> circuitsStarted = CircuitStarted.getQueryBuilder(true).sortByName().whereActive().build().find();


            // TODO: Patch fix duplicates in drawer
            boolean hasDuplicates = false;
            HashMap<String, CircuitStarted> uniqueCircuitsStarted = Maps.newHashMap();
            for (CircuitStarted circuitStarted : circuitsStarted) {
                String key = circuitStarted.getName();
                CircuitStarted unique = uniqueCircuitsStarted.get(key);
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
            for (CircuitStarted circuitStarted : circuitsStarted) {
                Log.d(TAG, "getName(): " + circuitStarted.getName());
                IDrawerItem circuitItem = new PrimaryDrawerItem().withName(circuitStarted.getName()).withTag(circuitStarted);
                circuitItems.add(circuitItem);
                circuitStartedCache.addActive(circuitStarted);
            }
        } catch (ParseException e) {
            new HandleException(context, TAG, "getActiveCircuitDrawerItems", e);
        }

        // don't leave header if there is no task groups
        if (circuitItems.size() == 1) {
            circuitItems.clear();
        }

        return circuitItems.toArray(new IDrawerItem[circuitItems.size()]);
    }

    private List<IDrawerItem> alarmItems;

    private IDrawerItem[] getAlarmsDrawerItems() {
        alarmItems = Lists.newArrayList();
        IDrawerItem alarmHeader = new SectionDrawerItem().withName(R.string.title_drawer_alarms);
        alarmItems.add(alarmHeader);

        IDrawerItem alarmItem = new PrimaryDrawerItem().withName(R.string.alarms).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                fragmentDrawerCallback.selectItem(AlarmsViewPagerFragment.newInstance(), R.string.alarms);
                return false;
            }
        });
        alarmItems.add(alarmItem);

        IDrawerItem alarmPreferences = new PrimaryDrawerItem().withName(R.string.settings).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                fragmentDrawerCallback.selectItem(AlarmNotificationPreferencesFragment.newInstance(), R.string.settings_alarm);
                return false;
            }
        });
        alarmItems.add(alarmPreferences);

        return alarmItems.toArray(new IDrawerItem[alarmItems.size()]);
    }

    private void createReport(final Client client) {
        final MaterialDialog dialog = new CommonDialogsBuilder.MaterialDialogs(context).indeterminate(client.getName(), R.string.creating_report).show();
        // fake a bit of delay to ensure that the dialog is shown/readable
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                StaticTask.create(client, guardCache.getLoggedIn(), new GetCallback<StaticTask>() {
                    @Override
                    public void done(StaticTask task, ParseException e) {

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

                    }
                });

            }
        }, 500);
    }

    private IDrawerItem[] getActiveDistrictWatchDrawerItems() {
        List<IDrawerItem> districtWatchItems = Lists.newArrayList();
        IDrawerItem districtWatchHeader = new SectionDrawerItem().withName(R.string.title_drawer_districtwatches);
        try {
            List<DistrictWatchStarted> districtWatchesStarted = DistrictWatchStarted.getQueryBuilder(true).sortByName().whereActive().build().find();

            districtWatchItems.add(districtWatchHeader);
            for (DistrictWatchStarted districtWatchStarted : districtWatchesStarted) {
                IDrawerItem districtWatchItem = new PrimaryDrawerItem().withName(districtWatchStarted.getName()).withTag(districtWatchStarted).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        DistrictWatchStarted selectedDistrictWatchStarted = (DistrictWatchStarted) drawerItem.getTag();
                        fragmentDrawerCallback.selectItem(DistrictwatchViewPagerFragment.newInstance(context, selectedDistrictWatchStarted), selectedDistrictWatchStarted.getName());
                        return true;
                    }
                });
                districtWatchItems.add(districtWatchItem);
            }
        } catch (ParseException e) {
            new HandleException(context, TAG, "getActiveCircuitDrawerItems", e);
        }

        // don't leave header if there is no task groups
        if (districtWatchItems.size() == 1) {
            districtWatchItems.clear();
        }

        return districtWatchItems.toArray(new IDrawerItem[districtWatchItems.size()]);
    }

    private IDrawerItem[] getDataDrawerItems() {
        List<IDrawerItem> dataItems = Lists.newArrayList();
        IDrawerItem dataHeader = new SectionDrawerItem().withName(R.string.title_drawer_data);
        dataItems.add(dataHeader);
        dataItems.add(new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_clients)).withSelectable(false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                ClientListFragment clientListFragment = ClientListFragment.newInstance(Client.SORT_BY.NAME);
                clientListFragment.setOnClientSelectedListener(new ClientListFragment.OnClientSelectedListener() {
                    @Override
                    public void clientSelected(Client client) {
                        ToastHelper.toast(MainNavigationDrawer.this.context, client.getName());
                        GenericToolbarActivity.start(MainNavigationDrawer.this.context, client.getName(), client.getFullAddress(), ClientDetailsFragment.newInstance(client));

                    }
                });
                GenericToolbarActivity.start(context, R.string.title_drawer_clients, clientListFragment);
                return false;
            }
        }));
        dataItems.add(new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_guards)).withSelectable(false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                GenericToolbarActivity.start(context, R.string.title_drawer_guards, GuardListFragment.newInstance());
                return false;
            }
        }));
        dataItems.add(new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_gps_history)).withSelectable(false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                GenericToolbarActivity.start(context, R.string.title_drawer_gps_history, TrackerListFragment.newInstance());
                return false;
            }
        }));
        return dataItems.toArray(new IDrawerItem[dataItems.size()]);
    }


    private IDrawerItem getLogoutDrawerItem() {
        return new PrimaryDrawerItem().withIdentifier(DRAWER_LOGOUT).withName(context.getString(R.string.title_drawer_logout)).withIcon(FontAwesome.Icon.faw_sign_out);
    }

    private IDrawerItem getGuardDataDrawerItem() {
        return new PrimaryDrawerItem().withName(context.getString(R.string.my_data)).withSelectable(false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                GenericToolbarActivity.start(context, R.string.settings, R.string.my_data, GuardPreferencesFragment.newInstance());
                return false;
            }
        });
    }


}
