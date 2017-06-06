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

//        EventBusController.postUIUpdate(e);

        Log.e(tag, message, e);

        Crashlytics.logException(e);
        Crashlytics.log(Log.ERROR, tag, message + " error: " + e.getMessage());

        if (e instanceof ParseException) {
            ParseException parseException = (ParseException)e;
            new ParseErrorHandler().handleParseError(context, parseException);
        }

//        saveError(tag, message, e);
    }

//    private void saveError(String tag, String message, Throwable exception) {
//
//        ParseUser user = ParseUser.getCurrentUser();
//
//        if (user != null) {
//            Device device = new Device(GuardSwiftApplication.getInstance());
//
//            ParseObject error = new ParseObject("Error");
//            error.put("owner", user);
//            error.put("installation", ParseInstallation.getCurrentInstallation());
//            error.put("platform", "Android");
//            error.put("tag", tag);
//            error.put("gsVersion", device.getVersionCode());
//            error.put("message", message);
//
//
//            if (exception != null) {
//
//                String exceptionMessage = exception.getMessage();
//                if (exceptionMessage != null) {
//                    error.put("exception", exceptionMessage);
//                }
//
//                Throwable cause = exception.getCause();
//                if (cause != null) {
//
//                    String causeMessage = cause.getMessage();
//
//                    if (causeMessage != null) {
//                        error.put("cause", causeMessage);
//                    }
//                }
//            }
//
//            error.saveInBackground();
//        }
//    }
}


