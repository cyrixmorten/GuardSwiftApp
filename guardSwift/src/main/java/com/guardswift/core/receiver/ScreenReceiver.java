//package com.guardswift.core.receiver;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.PowerManager;
//
//import com.guardswift.dagger.InjectingBroadcastReceiver;
//import com.guardswift.util.Device;
//
//import javax.inject.Inject;
//
///**
// * Created by cyrix on 2/18/15.
// */
//public class ScreenReceiver extends InjectingBroadcastReceiver {
//
//    @Inject
//    Device device;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        super.onReceive(context, intent);
//
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//
//        boolean isInteractive;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//            isInteractive = pm.isInteractive();
//        } else {
//            isInteractive = pm.isScreenOn();
//        }
//
//        boolean screenOn = false;
//        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
////            Log.e(ScreenReceiver.class.getSimpleName(), "SCREEN_OFF");
//            screenOn = false;
//        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
////            Log.e(ScreenReceiver.class.getSimpleName(), "SCREEN_ON");
//            screenOn = true;
//        }
//
//        device.setScreenOn(isInteractive || screenOn);
//    }
//}
