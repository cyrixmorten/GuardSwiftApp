package com.guardswift.ui.drawer;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingActivityModule;
import com.guardswift.util.Util;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GuardLoginNavigationDrawer extends BaseNavigationDrawer {

    private static final String TAG = GuardLoginNavigationDrawer.class.getSimpleName();


    private final Context context;

    private Drawer navigationDrawer;
    private FragmentDrawerCallback fragmentDrawerCallback;


    @Inject
    public GuardLoginNavigationDrawer(@InjectingActivityModule.ForActivity Context context) {
        this.context = context;
    }

    public Drawer getDrawer() {
        return navigationDrawer;
    }

    public Drawer create(final FragmentActivity activity, Toolbar toolbar, final FragmentDrawerCallback fragmentDrawerCallback) {

        this.fragmentDrawerCallback = fragmentDrawerCallback;


        // initPreferences navigationdrawer
        navigationDrawer = new DrawerBuilder()
                .withAccountHeader(this.getHeader(activity))
                .withActivity(activity)
                .withToolbar(toolbar)
                .withSelectedItem(-1) // defaults to no selection
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        Util.hideKeyboard(activity);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        Util.hideKeyboard(activity);
                    }
                })
                .build();

        CommonDrawerItems commonDrawerItems = new CommonDrawerItems(activity);

        navigationDrawer.addItems(commonDrawerItems.thisDevice());
        if (BuildConfig.DEBUG) {
            navigationDrawer.addStickyFooterItem(getLogoutDrawerItem(activity));
        }

        return navigationDrawer;
    }

    private AccountHeader getHeader(Activity activity) {
        return new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.header)
                .build();
    }

    IDrawerItem getLogoutDrawerItem(final Activity activity) {
        return new PrimaryDrawerItem().withName(context.getString(R.string.logout)).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            new HandleException(TAG, "Logout account", e);
                            return;
                        }

                        activity.startActivityForResult(new ParseLoginBuilder(context).build(), 0);
                    }
                });
                return true;
            }
        });
    }






}
