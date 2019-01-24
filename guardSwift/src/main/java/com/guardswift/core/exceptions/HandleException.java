package com.guardswift.core.exceptions;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;


public class HandleException {


    public HandleException(String tag, String message, Throwable e) {
        this(null, tag, message, e);
    }

    public HandleException(final Context context, String tag, String message, final Throwable e) {

        e.printStackTrace();

        Crashlytics.logException(e);
        Crashlytics.log(Log.ERROR, tag, message + " error: " + e.getMessage());

        if (e instanceof ParseException) {
            new ParseErrorHandler().handleParseError(context, (ParseException)e);
        }

        LogError.log(tag, message, e);
    }


}


