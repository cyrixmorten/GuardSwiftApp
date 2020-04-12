package com.guardswift.core.exceptions;


import android.app.Activity;
import android.content.Context;

import com.parse.ParseException;
import com.parse.ui.ParseLoginBuilder;

public class ParseErrorHandler {

    private static String TAG = ParseErrorHandler.class.getSimpleName();

    public void handleParseError(Context context, ParseException e) {
        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN: handleInvalidSessionToken(context);
                break;
        }
    }

    private void handleInvalidSessionToken(final Context context) {

        //--------------------------------------
        // Option 1: Show a message asking the user to log out and log back in.
        //--------------------------------------
        // If the user needs to finish what they were doing, they have the opportunity to do so.
        //
        // new AlertDialog.Builder(getActivity())
        //   .setMessage("Session is no longer valid, please log out and log in again.")
        //   .setCancelable(false).setPositiveButton("OK", ...).create().show();

        //--------------------------------------
        // Option #2: Show login screen so user can re-authenticate.
        //--------------------------------------
        // You may want this if the logout button could be inaccessible in the UI.
        //
        // startActivityForResult(new ParseLoginBuilder(getActivity()).build(), 0);


        if (context instanceof Activity) {
            //new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.session_expired, context.getString(R.string.session_expired_message), new MaterialDialog.SingleButtonCallback() {
            //    @Override
            //    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            //        ((Activity)context).startActivityForResult(new ParseLoginBuilder(context).build(), 0);
            //    }
            //}).show();

            ((Activity)context).startActivityForResult(new ParseLoginBuilder(context).build(), 0);
        }
    }
}

