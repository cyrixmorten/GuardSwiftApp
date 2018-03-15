package com.guardswift.ui.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by cyrixmorten on 12/02/2017.
 */

public class MenuItemBuilder {

    private Context context;

    private int flags = -1;
    private Drawable icon;

    public MenuItemBuilder(Context context) {
        this.context = context;
    }

    public MenuItemBuilder showAsActionFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public MenuItemBuilder icon(Drawable icon) {
        this.icon = icon;
        return this;
    }

    public void addToMenu(Menu menu, int title, MenuItem.OnMenuItemClickListener clickListener) {
        MenuItem item = menu.add(context.getString(title));

        if (flags != -1) {
            item.setShowAsActionFlags(flags);
        }

        if (icon != null) {
            item.setIcon(icon);
        }

        item.setOnMenuItemClickListener(clickListener);
    }

//    public void create(Context context, Menu menu, MenuItem.OnMenuItemClickListener clickListener) {
//        menu.add(context.getString(R.string.add_new))
//                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//                .setIcon(
//                        MenuItemIcons.create(context, FontAwesome.Icon.faw_plus_circle)
//                )
//                .setOnMenuItemClickListener(clickListener);
//    }
}
