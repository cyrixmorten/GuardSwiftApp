//package com.guardswift.core.receiver;
//
//import android.content.Context;
//import android.content.Intent;
//
//import com.guardswift.dagger.InjectingBroadcastReceiver;
//
//public class PluginControlReceiver extends InjectingBroadcastReceiver {
//
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		super.onReceive(context, intent);
//
//		String action = intent.getAction();
//
//
//		if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
//
////			notifications.displayTaskNotification(context
////					.getString(R.string.charging_screen_kept_turned_on));
////			eventBus.post(new ExternalEvent.PowerConnected());
//
//		} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
//
////			notifications.cancelAllTaskNotifications();
////			eventBus.post(new ExternalEvent.PowerDisconnected());
//
//		}
//	}
//}
