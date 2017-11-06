package com.guardswift.util;

import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;
import com.guardswift.ui.GuardSwiftApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Device {

	final String TAG = "Device";

	// returns true if a multipane layout has been shown;
	private boolean multiPaneEnabled;

	private final Context context;
	private static boolean screenOn;

	@Inject
	public Device(@ForApplication Context context) {
		super();
		this.context = context;
	}

	public Device() {
		this.context = GuardSwiftApplication.getInstance();
	}

	public boolean isRunningOnEmulator() {
		return Build.FINGERPRINT.contains("generic");
	}

	public void setMultiPaneEnabled(boolean hasMasterView) {
		if (hasMasterView) {
			multiPaneEnabled = true;
		}
	}

    public boolean hasGpsAndNetworkEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGPSEnabled && isNetworkEnabled;
    }

	public boolean isMultiPaneEnabled() {
		return multiPaneEnabled;
	}

	// public boolean isMultiPane() {
	// boolean isMultiPane = context.getResources().getBoolean(
	// com.turios.R.bool.has_two_panes);
	//
	// return isMultiPane;
	// }

	public Boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected())
			return true;

		return false;
	}

	public int getVersionCode() {
		int v = 0;
		try {
			v = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// Huh? Really?
		}
		return v;
	}

	public String getVersionName() {
		String v = "";
		try {
			v = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// Huh? Really?
		}
		return v;
	}

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

	public void setScreenOn(boolean screenOn) {
		Log.w(TAG, "Screen state changed");
		if (screenOn) {
			Log.w(TAG, "Screen ON");
		} else {
			Log.w(TAG, "Screen OFF");
		}
		Device.screenOn = screenOn;
	}

	public static boolean isScreenOn() {
		return screenOn;
	}

	public boolean isNFCEnabled() {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
		boolean enabled = false;

		if (nfcAdapter != null) {
			enabled = nfcAdapter.isEnabled();
		}

		return enabled;
	}

}
