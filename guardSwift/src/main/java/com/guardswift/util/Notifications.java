//package com.guardswift.util;
//
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.support.v4.app.NotificationCompat;
//
//import com.guardswift.R;
//import com.guardswift.ui.activity.details.DetailsActivityFactory;
//import com.guardswift.persistence.parse.planning.GSTask;
//
//public class Notifications {
//
//	private static final String TAG = Notifications.class.getSimpleName();
//
//
//	private static final int NOTICIFATION_ID_TASK = 1;
//
//	public static void displayTaskNotification(Context context, GSTask task, String notificationMessage) {
//
//        NotificationManager  mNotificationManager = (NotificationManager) context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//				context).setSmallIcon(R.drawable.ic_launcher)
//				.setContentTitle(task.getTaskTitle(context))
//				.setContentText(notificationMessage).setAutoCancel(true);
//
//		// Creates an explicit intent for an Activity in your app
//		Intent resultIntent = DetailsActivityFactory.getIntent(context, task);//new Intent(context, MainActivity.class);
//		// resultIntent.putExtra(Constants.EXTRA_ACTION, "NOTIFICATION");
//		// resultIntent.putExtra(Constants.EXTRA_MESSAGE, message);
//		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		mBuilder.setContentIntent(contentIntent);
//		// mBuilder.setFullScreenIntent(contentIntent, true);
//
//		// mId allows you to init the checkpoint later on.
//		mNotificationManager.notify(NOTICIFATION_ID_TASK, mBuilder.build());
//	}
//
//	public static void cancelAllTaskNotifications(Context context) {
//        NotificationManager  mNotificationManager = (NotificationManager) context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//        mNotificationManager.cancel(NOTICIFATION_ID_TASK);
//	}
//
////    public static void soundNotification(Context context) {
////        soundNotification(context, "");
////    }
////	public static void soundNotification(Context context, String custom_uri) {
////
////		try {
////			Uri checkpoint = null;
////			if (custom_uri.isEmpty()) {
////				checkpoint = RingtoneManager
////						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////			} else {
////				checkpoint = Uri.parse(custom_uri);
////			}
////
////			Ringtone r = RingtoneManager.getRingtone(context, checkpoint);
////
////			r.play();
////
////		} catch (Exception e) {
////			Log.e(TAG, "", e);
////		}
////	}
//}
