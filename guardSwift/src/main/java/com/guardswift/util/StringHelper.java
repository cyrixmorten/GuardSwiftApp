package com.guardswift.util;

import com.guardswift.R;

public class StringHelper {

    public static int weekDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 0: return R.string.sunday_and_holidays;
            case 1: return R.string.monday;
            case 2: return R.string.tuesday;
            case 3: return R.string.wednesday;
            case 4: return R.string.thursday;
            case 5: return R.string.friday;
            case 6: return R.string.saturday;
        }

        return R.string.unknown;
    }
}
