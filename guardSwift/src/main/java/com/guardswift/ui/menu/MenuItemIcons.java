package com.guardswift.ui.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by cyrixmorten on 05/06/2017.
 */

public class MenuItemIcons {

    private static int SIZE_DP = 24;
    private static int COLOR_DEFAULT = Color.DKGRAY;

    public static Drawable createWithFontAwesomeIcon(Context context, FontAwesome.Icon faIcon) {
        return new IconicsDrawable(context)
                .icon(faIcon)
                .color(COLOR_DEFAULT)
                .sizeDp(SIZE_DP);
    }

}
