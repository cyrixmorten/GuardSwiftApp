package com.guardswift.core.exceptions;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.ParseException;


public class HandleException {


    public HandleException(String tag, String message, Throwable e) {
        this(null, tag, message, e);
    }

    public HandleException(final Context context, String tag, String message, final Throwable e) {

        e.printStackTrace();

        FirebaseCrashlytics.getInstance().recordException(e);

        if (e instanceof ParseException && context != null) {
            new ParseErrorHandler().handleParseError(context, (ParseException)e);
        }

        LogError.log(tag, message, e);
    }


}


