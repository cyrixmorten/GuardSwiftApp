package com.guardswift.core.parse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseObjectFactory;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.gps.LocationTracker;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GuardLoginActivity;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.util.Device;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import bolts.Continuation;
import bolts.Task;

@Singleton
public class ParseModule {

    private static final String TAG = ParseModule.class.getSimpleName();

    public static final String DEVApplicationID = "7fynHGuQW5NZLROiIDcCzLddbINcUSwPdoE0L72d";
    public static final String DEVClientKey = "esiyR18YFQ5Ew1dMZDwcFuFYTZls8TwYWUtPs5Tu";
    public static final String ApplicationID = "gejAg1OFJrBwepcORHB3U7V7fawoDjlymRe8grHJ";
    public static final String ClientKey = "ZOZ7GGeu2tfOQXGRcMSOtDMg1qTGVZaxjO8gl89p";

    public static final String FUNCTION_SEND_REPORT = "sendReport";

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
        guard.setOnline(true);

        guardCache.setLoggedIn(guard);

        new EventLog.Builder(context)
                .event(context.getString(R.string.login))
                .eventCode(EventLog.EventCodes.GUARD_LOGIN).saveAsync();

        GuardSwiftApplication.getInstance().startServices();

        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(Intent.makeRestartActivityTask(intent.getComponent()));
    }


    private void logout(final SaveCallback saveCallback, ProgressCallback progressCallback) {

        Guard guard = guardCache.getLoggedIn();
        Log.w(TAG, "LOGOUG GUARD: " + guard);

        if (guard != null) {
            guard.setOnline(false);
            guard.pinThenSaveEventually();

            // todo temporarily disabled GPS tracking
            new EventLog.Builder(context)
            .event(context.getString(R.string.logout))
                    .eventCode(EventLog.EventCodes.GUARD_LOGOUT).saveAsync(new GetCallback<EventLog>() {
                        @Override
                        public void done(EventLog object, ParseException e) {
                            saveCallback.done(e);
                        }
            });

//            LocationTracker.uploadForGuard(context, guard, progressCallback).continueWith(new Continuation<String, Object>() {
//                @Override
//                public Object then(Task<String> task) throws Exception {
//                    if (task.isFaulted()) {
//                        new HandleException(context, TAG, "upload guard locations", task.getError());
//                        saveCallback.done((ParseException) task.getError());
//                    } else {
//                        Log.w(TAG, "Location url: " + task.getResult());
//                        new EventLog.Builder(context)
//                                .event(context.getString(R.string.logout))
//                                .locationTrackerUrl(task.getResult())
//                                .eventCode(EventLog.EventCodes.GUARD_LOGOUT).saveAsync(new GetCallback<EventLog>() {
//                            @Override
//                            public void done(EventLog object, ParseException e) {
//                                saveCallback.done(e);
//                            }
//                        });
//                    }
//
//                    return null;
//                }
//            });
        } else {
            saveCallback.done(null);
        }

    }

    private void clearData() {

        guardCache.removeLoggedIn();
        tasksCache.clear();

        GuardSwiftApplication.getInstance().getCacheFactory().getCircuitStartedCache().clear();
        GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchStartedCache().clear();

        GuardSwiftApplication.getInstance().stopServices();


        unpinAllParseObjects();
    }

    private void unpinAllParseObjects() {

        List<Task<Object>> unpinClassNamed = Lists.newArrayList();
        for (ExtendedParseObject parseObject : new ParseObjectFactory().getAll()) {
            unpinClassNamed.add(parseObject.unpinAllPinnedToClass());
        }

        Task.whenAll(unpinClassNamed).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                GuardSwiftApplication.getInstance().teardownParseObjectsLocally();
                return ParseObject.unpinAllInBackground(ParseObject.DEFAULT_PIN);
            }
        });
    }


    public static ParseGeoPoint geoPointFromLocation(Location loc) {
        if (loc == null)
            return null;

        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    public void logout(final Activity activity) {
        if (!new Device(activity).isOnline()) {
            // Missing internet connection
            new CommonDialogsBuilder.MaterialDialogs(activity).infoDialog(R.string.title_internet_missing, R.string.could_not_connect_to_server).show();
            return;
        }
        // Prepare progress dialog
        final MaterialDialog updateDialog = new MaterialDialog.Builder(activity)
                .title(R.string.working)
                .content(R.string.please_wait)
                .progress(true, 0)
//                .progress(false, 0, true)
                .show();

//        updateDialog.setMaxProgress(100);
        updateDialog.setCancelable(false);


        // Perform logout, upload
        logout(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                Log.d(TAG, "Logout completed! " + e);
                if (e != null) {
                    if (e.getCode() == ParseException.TIMEOUT) {
                        new CommonDialogsBuilder.MaterialDialogs(activity).infoDialog(R.string.title_internet_missing, R.string.could_not_connect_to_server).show();
                    } else {
                        new CommonDialogsBuilder.MaterialDialogs(activity).infoDialog(R.string.error_an_error_occured, e.getMessage()).show();
                    }

                    new HandleException(TAG, "Guard logout", e);
                    return;
                }

                GuardLoginActivity.start(activity);
                clearData();

                updateDialog.cancel();
                activity.finish();

            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                Log.e(TAG, "Logout progress: " + percentDone);
//                                sDialog.getProgressHelper()
//                                        .setProgress(percentDone);
                updateDialog.setProgress(percentDone);
            }
        });
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
