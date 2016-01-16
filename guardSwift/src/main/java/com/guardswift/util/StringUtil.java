package com.guardswift.util;

/**
 * Created by cyrix on 2/3/15.
 */
public class StringUtil {

    public static String lineSeperator() {
        return System.getProperty("line.separator");
    }
    public static String removeLast(String str, String occurance) {
        if (null != str && str.length() > 0 )
        {
            int endIndex = str.lastIndexOf(occurance);
            if (endIndex != -1)
            {
                return str.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
            }
        }
        return str;
    }
    public static String removeIfLastLetter(String str, String occurance) {
        String pruned = removeLast(str, occurance);
        if ((str.length() - pruned.length()) == 1) {
            return pruned;
        }
        return str;
    }
}
