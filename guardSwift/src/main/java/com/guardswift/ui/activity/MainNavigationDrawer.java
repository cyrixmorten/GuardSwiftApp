package com.guardswift.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCacheFactory;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchStarted;
import com.guardswift.persistence.parse.execution.regular.CircuitStarted;
import com.guardswift.ui.parse.data.client.ClientListFragment;
import com.guardswift.ui.parse.data.guard.GuardListFragment;
import com.guardswift.ui.parse.execution.circuit.CircuitViewPagerFragment;
import com.guardswift.ui.parse.execution.districtwatch.DistrictwatchViewPagerFragment;
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
import com.parse.ParseException;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by cyrix on 12/12/15.
 */

public class MainNavigationDrawer {


    private static final String TAG = MainNavigationDrawer.class.getSimpleName();

    public interface MainNavigationDrawerCallback {
        void selectItem(Fragment fragment);
        void selectItem(Fragment fragment, String title);
        void selectItem(Fragment fragment, int titleResource);
        void logout();
    }

    private static final int DRAWER_LOGOUT = 0;
    private static final int DRAWER_CIRCUITSTARTED_LIST = 1;

    private final Context context;
    private final ParseCacheFactory parseCacheFactory;
    private final GuardCache guardCache;
    private final CircuitStartedCache circuitStartedCache;

    private Drawer navigationDrawer;
    private MainNavigationDrawerCallback drawerCallback;

    @Inject
    public MainNavigationDrawer(@InjectingApplication.InjectingApplicationModule.ForApplication Context context, ParseCacheFactory parseCacheFactory) {
        this.context = context;

        this.parseCacheFactory = parseCacheFactory;
        this.guardCache = parseCacheFactory.getGuardCache();
        this.circuitStartedCache = parseCacheFactory.getCircuitStartedCache();

    }

    public void initNavigationDrawer(Activity activity, Toolbar toolbar, final MainNavigationDrawerCallback drawerCallback) {

        this.drawerCallback = drawerCallback;

        // Create navigation header
        AccountHeader headerResult = new AccountHeaderBuilder()
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
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        // init navigationdrawer
        navigationDrawer = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(activity)
                .withToolbar(toolbar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == null)
                            return false;

                        switch (drawerItem.getIdentifier()) {
                            case DRAWER_LOGOUT:
                                drawerCallback.logout();
                                return true;
                        }

                        if (drawerItem.getTag() instanceof  CircuitStarted) {
                            Log.w(TAG, "Clicked circuitStarted");
                            CircuitStarted clickedCircuitStarted = (CircuitStarted) drawerItem.getTag();
                            for (IDrawerItem item : circuitItems) {
                                CircuitStarted circuitStarted = (CircuitStarted) item.getTag();
                                if (circuitStarted != null && clickedCircuitStarted.getObjectId().equals(circuitStarted.getObjectId())) {
                                    Log.w(TAG, "Clicked " + circuitStarted.getName());
                                    drawerCallback.selectItem(CircuitViewPagerFragment.newInstance(context, circuitStarted), circuitStarted.getName());
                                    return false; // close drawer
                                }
                            }
                        }
                        return false;
                    }
                })
                .build();


//        navigationDrawer.addItems(getStaticGuardingItems());
        navigationDrawer.addItems(getActiveCircuitDrawerItems());
        navigationDrawer.addItems(getActiveDistrictWatchDrawerItems());
        navigationDrawer.addItems(getDataDrawerItems());
        navigationDrawer.addStickyFooterItem(getLogoutDrawerItem());

//        navigationDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
//            @Override
//            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
//
//            }
//        });

        // Determine initial selection
        CircuitStarted selectedCircuitStarted = circuitStartedCache.getSelected();
        IDrawerItem selected = null;
        if (selectedCircuitStarted  != null) {
            for (IDrawerItem item: circuitItems) {
                CircuitStarted circuitStarted = (CircuitStarted) item.getTag();
                if (circuitStarted != null && selectedCircuitStarted.getObjectId().equals(circuitStarted.getObjectId())) {
                    Log.w(TAG, "Initial: " + circuitStarted.getName() + " " + item);
                    selected = item;
                }
            }
        }
        if (selected == null) {
            Log.w(TAG, "Selected null");
            if (circuitItems.size() > 1) {
                navigationDrawer.setSelection(circuitItems.get(1), true);
            }
        }
        if (selected != null) {
            int position = navigationDrawer.getPosition(selected);
            Log.w(TAG, "Selected: " + ((CircuitStarted)selected.getTag()).getName());
            Log.w(TAG, "Position: " + position);
            navigationDrawer.setSelection(selected, true);
        }
    }


    private IDrawerItem[] getStaticGuardingItems() {
        List<IDrawerItem> staticItems = Lists.newArrayList();
        IDrawerItem staticGuarding = new PrimaryDrawerItem().withName(context.getString(R.string.static_guarding)).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                ToastHelper.toast(context, "STATIC");
                drawerCallback.selectItem(ClientListFragment.newInstance(), "Kunder");
                return true;
            }
        });
        staticItems.add(staticGuarding);
        return staticItems.toArray(new IDrawerItem[staticItems.size()]);
    }

    private List<IDrawerItem> circuitItems;
    private IDrawerItem[] getActiveCircuitDrawerItems() {
        circuitItems = Lists.newArrayList();
        IDrawerItem circuitHeader = new SectionDrawerItem().withName(R.string.title_drawer_circuits);

        try {
            List<CircuitStarted> circuitsStarted = CircuitStarted.getQueryBuilder(true).sortByName().whereActive().build().find();

            circuitItems.add(circuitHeader);
            for (CircuitStarted circuitStarted : circuitsStarted) {
                IDrawerItem circuitItem = new PrimaryDrawerItem().withName(circuitStarted.getName()).withTag(circuitStarted);
                circuitItems.add(circuitItem);
                circuitStartedCache.addActive(circuitStarted);
            }
        } catch (ParseException e) {
            new HandleException(context, TAG, "getActiveCircuitDrawerItems", e);
        }

        return circuitItems.toArray(new IDrawerItem[circuitItems.size()]);
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
                        drawerCallback.selectItem(DistrictwatchViewPagerFragment.newInstance(context, selectedDistrictWatchStarted), selectedDistrictWatchStarted.getName());
                        return true;
                    }
                });
                districtWatchItems.add(districtWatchItem);
            }
        } catch (ParseException e) {
            new HandleException(context, TAG, "getActiveCircuitDrawerItems", e);
        }
        return districtWatchItems.toArray(new IDrawerItem[districtWatchItems.size()]);
    }

    private IDrawerItem[] getDataDrawerItems() {
        List<IDrawerItem> dataItems = Lists.newArrayList();
        IDrawerItem dataHeader = new SectionDrawerItem().withName(R.string.title_drawer_data);
        dataItems.add(dataHeader);
        dataItems.add(getGuardsDrawerItem());
        return dataItems.toArray(new IDrawerItem[dataItems.size()]);
    }

    private IDrawerItem getGuardsDrawerItem() {
        return new PrimaryDrawerItem().withName(context.getString(R.string.title_drawer_guards)).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                drawerCallback.selectItem(GuardListFragment.newInstance(), R.string.title_drawer_guards);
                return true;
            }
        });
    }


    private IDrawerItem getLogoutDrawerItem() {
        return new PrimaryDrawerItem().withIdentifier(DRAWER_LOGOUT).withName(context.getString(R.string.title_drawer_logout)).withIcon(FontAwesome.Icon.faw_sign_out);
    }

}
