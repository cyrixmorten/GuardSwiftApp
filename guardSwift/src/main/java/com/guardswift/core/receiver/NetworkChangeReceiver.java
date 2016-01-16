//package com.guardswift.core.receiver;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//
///**
// * Created by cyrix on 2/18/15.
// */
//public class NetworkChangeReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(final Context context, final Intent intent) {
//        final ConnectivityManager connMgr = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        final android.net.NetworkInfo wifi = connMgr
//                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//        final android.net.NetworkInfo mobile = connMgr
//                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//        if (wifi.isAvailable() || mobile.isAvailable()) {
////            UploadUnsavedObjects.startActionSaveEventLogs(context);
//        }
//    }
//}
