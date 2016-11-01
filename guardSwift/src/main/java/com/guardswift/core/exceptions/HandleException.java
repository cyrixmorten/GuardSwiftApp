package com.guardswift.core.exceptions;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.eventbus.EventBusController;
import com.parse.ParseException;


public class HandleException {



    public HandleException(String tag, String message, Throwable e) {
        this(null, tag, message, e);
    }

    public HandleException(final Context context, String tag, String message, final Throwable e) {

        EventBusController.postUIUpdate(e);

        Log.e(tag, message, e);

        Crashlytics.logException(e);
        Crashlytics.log(Log.ERROR, tag, message + " error: " + e.getMessage());

        if (e instanceof ParseException) {
            ParseException parseException = (ParseException)e;
            new ParseErrorHandler().handleParseError(context, parseException);
        } else if (context != null && context instanceof Activity && BuildConfig.DEBUG) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.error_an_error_occured) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }


    }
}


