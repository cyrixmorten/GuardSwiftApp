//package com.guardswift.core.tasks.alarm;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.SystemClock;
//import android.support.v4.content.WakefulBroadcastReceiver;
//import android.util.Log;
//
//public class ParseAlarmReceiver extends WakefulBroadcastReceiver {
//	private static final String TAG = "ParseUpdateReceiver";
//
//
//	@Override
//	public void onReceive(final Context context, Intent intent) {
//
//
//        // This is the Intent to deliver to our service.
//        Intent service = new Intent(context, AlarmDownloaderIntentService.class);
//
//        // Start the service, keeping the device awake while it is launching.
//        Log.i(TAG, "Starting service @ " + SystemClock.elapsedRealtime());
//        startWakefulService(context, service);
//
//	}
//
//
//
//}
