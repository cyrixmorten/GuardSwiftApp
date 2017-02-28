package com.guardswift.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Util {

    public static SimpleDateFormat dateFormatHourMinutes() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
                Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat;
    }

    public static String relativeTimeString(Date date) {
        return DateUtils.getRelativeTimeSpanString(date.getTime(),
                new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_ALL)
                .toString();
    }


    public static float distanceMeters(Location l1, Location l2) {
        if (l1 == null || l2 == null)
            return Float.MAX_VALUE;

        double lat1 = l1.getLatitude();
        double lng1 = l1.getLongitude();
        double lat2 = l2.getLatitude();
        double lng2 = l2.getLongitude();
        float[] results = new float[3];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);

        return results[0];
    }

    public static String stringFormat(int hourOfDay, int minute) {
        String strHour = String.valueOf(hourOfDay);
        String strMinute = String.valueOf(minute);
        if (hourOfDay < 10) {
            strHour = "0" + strHour;
        }
        if (minute < 10) {
            strMinute = "0" + strMinute;
        }
        return strHour + "." + strMinute;
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * *
     * Method for Setting the Height of the ListView dynamically. Hack to fix
     * the issue of not showing all the items of the ListView when placed inside
     * a ScrollView
     * **
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
                MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void showKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null)
            return;

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        // will only trigger if no physical keyboard is onActionOpen
        inputMethodManager.showSoftInput(activity.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null)
            return;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void hideKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }
}
