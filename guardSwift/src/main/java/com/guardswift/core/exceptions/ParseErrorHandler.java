package com.guardswift.core.exceptions;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseLoginActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.parse.ParseException;

public class ParseErrorHandler {

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

        if (context == null || !(context instanceof Activity)) {
            Context ctx = GuardSwiftApplication.getInstance();
            ctx.startActivity(new Intent(ctx, ParseLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            new CommonDialogsBuilder.MaterialDialogs(context).ok(R.string.session_expired, context.getString(R.string.session_expired_message), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    context.startActivity(new Intent(context, ParseLoginActivity.class));
                }
            });
        }
    }
}

