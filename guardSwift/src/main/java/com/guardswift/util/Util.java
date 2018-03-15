package com.guardswift.util;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.format.DateUtils;
import android.view.inputmethod.InputMethodManager;

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
        if (date == null) {
            return "";
        }

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



    public static void hideKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null)
            return;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


}
