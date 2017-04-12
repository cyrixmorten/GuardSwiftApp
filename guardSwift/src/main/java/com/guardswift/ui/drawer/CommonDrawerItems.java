package com.guardswift.ui.drawer;

import android.content.Context;
import android.view.View;

import com.guardswift.R;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.preferences.InstallationPreferencesFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

/**
 * Created by cyrixmorten on 12/04/2017.
 */

public class CommonDrawerItems {

    private Context context;

    public CommonDrawerItems(Context context) {
        this.context = context;
    }

    public IDrawerItem thisDevice() {
        return new PrimaryDrawerItem().withName(context.getString(R.string.this_device)).withSelectable(false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                GenericToolbarActivity.start(context, R.string.settings, R.string.this_device, InstallationPreferencesFragment.newInstance());
                return false;
            }
        });
    }
}
