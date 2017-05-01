package com.guardswift.ui.drawer;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;

import com.mikepenz.materialdrawer.Drawer;

abstract class BaseNavigationDrawer {

    private static final String TAG = BaseNavigationDrawer.class.getSimpleName();

    abstract Drawer getDrawer();
    abstract Drawer initNavigationDrawer(FragmentActivity activity, Toolbar toolbar, final FragmentDrawerCallback fragmentDrawerCallback);

    public Drawer initNavigationDrawer(FragmentActivity activity, Toolbar toolbar, int viewId) {
        return initNavigationDrawer(activity, toolbar, new ToolbarFragmentDrawerCallback(activity, toolbar, viewId));
    }


}
