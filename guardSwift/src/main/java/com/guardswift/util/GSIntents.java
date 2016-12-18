package com.guardswift.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by cyrix on 1/9/16.
 */
public class GSIntents {
    public static void dialPhoneNumber(Context context, final String phoneNumber) {
        ToastHelper.toast(context, "Ringer op til: " + phoneNumber);
        context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    public static void openGmail(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
        context.startActivity(intent);
    }

    public static void openPDF(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                Uri.fromFile(file),
                "application/pdf");

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastHelper.toast(context, "Please install a PDF viewer app");
        }

    }

    public static void openPDFChooser(Context context, File file) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file), "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastHelper.toast(context, "Please install a PDF viewer app");
        }
    }
}
