package com.guardswift.core.parse;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.guardswift.R;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GuardLoginActivity;
import com.guardswift.ui.activity.MainActivity;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParseModule {

	private static final String TAG = ParseModule.class.getSimpleName();

	public static final String DEVApplicationID = "7fynHGuQW5NZLROiIDcCzLddbINcUSwPdoE0L72d";
	public static final String DEVClientKey = "esiyR18YFQ5Ew1dMZDwcFuFYTZls8TwYWUtPs5Tu";
	public static final String ApplicationID = "gejAg1OFJrBwepcORHB3U7V7fawoDjlymRe8grHJ";
	public static final String ClientKey = "ZOZ7GGeu2tfOQXGRcMSOtDMg1qTGVZaxjO8gl89p";

	private final Context context;
	private final GSTasksCache tasksCache;
	private final GuardCache guardCache;

	@Inject
	public ParseModule(@ForApplication Context context) {
		this.context = context;
		this.tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();
		this.guardCache = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();
	}

//	public static boolean isAlarmResponsible() {
//		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
//		ParseObject alarmGroup = installation.getParseObject("alarmGroup");
//		if (alarmGroup != null) {
//			return alarmGroup.getBoolean("responsible");
//		}
//		return false;
//	}


    public void login(Guard guard) {
//        Guard.Recent.setSelected(guard);
		guardCache.setLoggedIn(guard);

        new EventLog.Builder(context)
                .event(context.getString(R.string.login))
                .eventCode(EventLog.EventCodes.GUARD_LOGIN).saveAsync();

		GuardSwiftApplication.getInstance().startServices();

		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(Intent.makeRestartActivityTask(intent.getComponent()));
    }


    public void logout(final SaveCallback saveCallback, ProgressCallback progressCallback) {

//		Guard guard = guardCache.getLoggedIn();
//		if (guard != null) {
//			LocationTracker.uploadForGuard(context, guard, progressCallback).continueWith(new Continuation<String, Object>() {
//				@Override
//				public Object then(Task<String> task) throws Exception {
//					if (!task.isFaulted()) {
//						new EventLog.Builder(context)
//								.event(context.getString(R.string.logout))
//								.locationTrackerUrl(task.getResult())
//								.eventCode(EventLog.EventCodes.GUARD_LOGOUT).saveAsync(new SaveCallback() {
//							@Override
//							public void done(ParseException e) {
//								clearData();
//							}
//						});
//						GuardLoginActivity.start(context);
//						saveCallback.done(null);
//
//					} else {
//						saveCallback.done(new ParseException(task.getError()));
//					}
//					return null;
//				}
//			});
//		} else {
			GuardLoginActivity.start(context);
			saveCallback.done(null);
			clearData();
//		}

    }

    private void clearData() {

		guardCache.removeLoggedIn();
		tasksCache.clearGeofencedTasks();

		GuardSwiftApplication.getInstance().stopServices();

//		GuardSwiftApplication.getInstance().getCacheFactory().clearAll();
    }

//    private void reCreateRecentObjects() {
//        Guard.Recent.getSelected(preferences);
//        Circuit.Recent.getSelected(preferences);
//        CircuitStarted.Recent.getSelected(preferences);
//    }

//    public boolean isGuardLoggedIn() {
//        return guardCache.isLoggedIn();
//    }



	public static ParseGeoPoint geoPointFromLocation(Location loc) {
		if (loc == null)
			return null;

		return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
	}



	public static class DistanceStrings {

		public static final String METERS = "meter";
		public static final String KILOMETERS = "km";

		public String distanceType;
		public String distanceValue;

		public int km;
		public int meters;

		public DistanceStrings(String distanceType, String distanceValue,
				int km, int meters) {
			super();
			this.distanceType = distanceType;
			this.distanceValue = distanceValue;
			this.km = km;
			this.meters = meters;
		}

	}

//    public static ParseModule.DistanceStrings getDistanceStrings(Client client) {
//        Location deviceLocation = LocationModule.Recent.getLastKnownLocation();
//        ParseGeoPoint targetGeoPoint = client.getPosition();
//        return ParseModule.distanceBetweenString(
//                deviceLocation, targetGeoPoint);
//    }

	public static DistanceStrings distanceBetweenString(
			Location deviceLocation, ParseGeoPoint targetGeoPoint) {
		double distance = distanceBetweenKilomiters(deviceLocation, targetGeoPoint);
		if (distance == -1) {
			return null;
		}

		int wholeKms = (int) distance;

		// use meters instead
		if (wholeKms == 0) {
			double meters = distance - Math.floor(distance);

			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(
					Locale.ENGLISH);
			otherSymbols.setDecimalSeparator(',');
			otherSymbols.setGroupingSeparator('.');

			DecimalFormat df = new DecimalFormat("#.###", otherSymbols);

			String meterString = df.format(meters);
			if (meterString.contains(",")) {
				String[] splitMeters = meterString.split("\\,");
				int meterInt = Integer.parseInt(splitMeters[1]);
				return new DistanceStrings(DistanceStrings.METERS,
						String.valueOf(meterInt), 0, meterInt);
			} else {
				return new DistanceStrings(DistanceStrings.METERS, meterString,
						0, (int) meters);
			}
		} else {
			DecimalFormat df = new DecimalFormat("##.##");
			df.setRoundingMode(RoundingMode.HALF_UP);
			return new DistanceStrings(DistanceStrings.KILOMETERS,
					df.format(distance), wholeKms, 0);
		}
	}

    public static float distanceBetweenMeters(Location deviceLocation,
                                                   ParseGeoPoint targetGeoPoint) {
		if (deviceLocation == null || targetGeoPoint == null) {
			return Float.MAX_VALUE;
		}


        Location targetLocation = new Location("");
        targetLocation.setLatitude(targetGeoPoint.getLatitude());
        targetLocation.setLongitude(targetGeoPoint.getLongitude());

        return deviceLocation.distanceTo(targetLocation);
    }

	public static double distanceBetweenKilomiters(Location deviceLocation,
                                                   ParseGeoPoint targetGeoPoint) {
		if (deviceLocation == null || targetGeoPoint == null)
			return -1;

		ParseGeoPoint deviceGeoPoint = new ParseGeoPoint(
				deviceLocation.getLatitude(), deviceLocation.getLongitude());

		return deviceGeoPoint.distanceInKilometersTo(targetGeoPoint);
	}

	public static <T extends ParseQuery<?>> void sortNearest(T query, String key) {
        Location lastLocation = LocationModule.Recent.getLastKnownLocation();
		if (lastLocation != null) {
			ParseGeoPoint geoPoint = ParseModule
					.geoPointFromLocation(lastLocation);
			query.whereNear(key, geoPoint);
		}
	}
}
