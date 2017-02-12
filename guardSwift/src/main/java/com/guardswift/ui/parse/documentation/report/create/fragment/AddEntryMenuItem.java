package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;

import com.guardswift.R;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by cyrixmorten on 12/02/2017.
 */

class AddEntryMenuItem {

    public void create(Context context, Menu menu, MenuItem.OnMenuItemClickListener clickListener) {
        menu.add(context.getString(R.string.add_new))
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                .setIcon(
                        new IconicsDrawable(context)
                                .icon(FontAwesome.Icon.faw_plus_circle)
                                .color(Color.DKGRAY)
                                .sizeDp(24)
                )
                .setOnMenuItemClickListener(clickListener);
    }
}
