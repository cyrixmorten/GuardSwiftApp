package com.guardswift.ui.drawer;

import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.Toolbar;

import com.mikepenz.materialdrawer.Drawer;

abstract class BaseNavigationDrawer {

    private static final String TAG = BaseNavigationDrawer.class.getSimpleName();

    abstract Drawer getDrawer();
    abstract Drawer create(FragmentActivity activity, Toolbar toolbar, final FragmentDrawerCallback fragmentDrawerCallback);

    public Drawer create(FragmentActivity activity, Toolbar toolbar, int viewId) {
        return create(activity, toolbar, new ToolbarFragmentDrawerCallback(activity, toolbar, viewId));
    }


}
