package com.guardswift.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by cyrix on 1/9/16.
 */
public class Intents {
    public static void dialPhoneNumber(Context context, final String phoneNumber) {
        ToastHelper.toast(context, "Ringer op til: " + phoneNumber);
        context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }
}
