package com.guardswift.core.exceptions;

import android.util.Log;

import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Device;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class LogError {

    private static String TAG = LogError.class.getSimpleName();

    public static void log(String tag, String message) {
        log(tag, message, null);
    }

    public static void log(String tag, String message, Throwable exception) {

        Log.e(TAG, message, exception);

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

            Guard guard = GuardSwiftApplication.getLoggedIn();
            if (guard != null) {
                error.put("guard", guard);
            }

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

                StackTraceElement[] st = exception.getStackTrace();
                if (st != null && st.length > 0) {
                    error.put("trace", st[0].toString());
                }
            }

            error.saveInBackground();

        }
    }


}
