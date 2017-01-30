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
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Device;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;


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

        saveError(tag, message, e);
    }

    private void saveError(String tag, String message, Throwable exception) {

        ParseUser user = ParseUser.getCurrentUser();

        if (user != null) {
            Device device = new Device(GuardSwiftApplication.getInstance());

            ParseObject error = new ParseObject("Error");
            error.put("owner", user);
            error.put("installation", ParseInstallation.getCurrentInstallation());
            error.put("platform", "Android");
            error.put("tag", tag);
            error.put("gsVersion", device.getVersionCode());
            error.put("message", message);


            if (exception != null) {

                String exceptionMessage = exception.getMessage();
                if (exceptionMessage != null) {
                    error.put("exception", exceptionMessage);
                }

                Throwable cause = exception.getCause();
                if (cause != null) {

                    String causeMessage = cause.getMessage();

                    if (causeMessage != null) {
                        error.put("cause", causeMessage);
                    }
                }
            }

            error.saveInBackground();
        }
    }
}


