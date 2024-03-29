package com.guardswift.ui.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

/**
 * Created by cyrixmorten on 05/06/2017.
 */

public class MenuItemIcons {

    private static int SIZE_DP = 24;
    private static int COLOR_DEFAULT = Color.DKGRAY;

    public static Drawable create(Context context, IIcon icon) {
        return new IconicsDrawable(context)
                .icon(icon)
                .color(COLOR_DEFAULT)
                .sizeDp(SIZE_DP);
    }

}
