package com.guardswift.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.guardswift.dagger.InjectingActivityModule.ForActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WakeScreen {

	private static final String TAG = WakeScreen.class.getSimpleName();

	private PowerManager.WakeLock wl;
	private PowerManager pm;

	private final Context context;
	private final Activity activity;

	@Inject
	public WakeScreen(@ForActivity Context context, AppCompatActivity activity
			) {
		this.context = context;
		this.activity = activity;
	}

	public void wakeIfPowerConnected() {
		if (isConnected()) {
			screenWakeup(false);
		}
	}

	private boolean isConnected() {
		Intent intent = context.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		return plugged == BatteryManager.BATTERY_PLUGGED_AC
				|| plugged == BatteryManager.BATTERY_PLUGGED_USB;
	}

	public void screenWakeup(boolean playSound) {

		Log.d(TAG, "screenWakeup");

		try {
			pm = (PowerManager) context
					.getSystemService(Activity.POWER_SERVICE);
			// if (!pm.isScreenOn()) {
			if (wl == null) {
				wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "GuardSwiftWeb");
			}
			if (!wl.isHeld()) {
				wl.acquire();
			}

			final Window win = activity.getWindow();
			win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
					| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			// }

		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}

		// if (playSound)
		// notifications.soundNotification("");

	}

	public void screenRelease() {

		Log.d(TAG, "screenRelease");

		if (releaseWakeLock()) {

			Log.d(TAG, "Released");
			activity.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
							| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
							| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
							| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			Log.d(TAG, "Not released");
		}

	}

	private boolean holdsWakeLock() {
		return wl != null && wl.isHeld();
	}

	private boolean releaseWakeLock() {
		try {
			if (holdsWakeLock()) {
				wl.release();
				return true;
			}

		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}

		return false;
	}

}
