package com.guardswift.core.exceptions;

import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Device;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class LogError {

    public static void log(String tag, String message) {
        log(tag, message, null);
    }

    public static void log(String tag, String message, Throwable exception) {
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
