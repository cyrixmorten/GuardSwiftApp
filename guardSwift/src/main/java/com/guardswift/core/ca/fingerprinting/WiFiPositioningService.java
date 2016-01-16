package com.guardswift.core.ca.fingerprinting;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.guardswift.core.ca.FingerprintingModule;
import com.guardswift.dagger.InjectingService;
import com.guardswift.eventbus.events.WifiProbabilityDistributionEvent;
import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import dk.alexandra.positioning.wifi.Fingerprint;
import dk.alexandra.positioning.wifi.PositionEstimate;
import dk.alexandra.positioning.wifi.ProbabilityDistribution;
import dk.alexandra.positioning.wifi.WiFiPositionListener;
import dk.alexandra.positioning.wifi.tools.WiFiPositioning;

/**
 * Service that provides WiFi position updates.
 * <p/>
 * To use this service start it and bind to broadcasts like this:
 * <code>
 * startService(new Intent(this, WiFiPositioningService.class));
 * bindService(new Intent(this, WiFiPositioningService.class), mWifiServiceConnection, BIND_AUTO_CREATE);
 * </code>
 *
 * @author jakobl
 */
public class WiFiPositioningService extends InjectingService implements WiFiPositionListener {

    private static final String TAG = WiFiPositioningService.class.getSimpleName();

    //    public static final String STORED_FINGERPRINTS = "stored_fingerprints";
    public static final String WIFI_POSITION_UPDATED_INTENTFILTER = "dk.alexandra.positioning.wifi.WIFI_POSITION";
    public static final String WIFI_POSITION = "wifi_position";
    public static final String WIFI_POSITION_SYMBOLIC = "wifi_position_symbolic";
    public static final String WIFI_POSITION_PROBABILITY = "wifi_position_probability";
    public static final String WIFI_PDF = "wifi_pdf";
    public static final String WIFI_POSITION_ESTIMATED_DISTANCE = "wifi_position_estimated_distance";
    private WifiManager mWifiManager;
    private WiFiPositioning mWifiPositioning;
    private final boolean mWeStartedWiFi = true;
    private Timer mScanTimer;
    private boolean mStarted = false;


    @Inject
    FingerprintingModule fingerprintingModule;

    @Inject
    FingerPrintIO fingerPrintIO;
    @Inject
    GSTasksCache tasksCache;


    BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            GSTasksCache tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();
            if (tasksCache.getArrivedWithCheckpoints(GSTask.TASK_TYPE.REGULAR).isEmpty()) {
                Log.e(TAG, "Not arrived at any tasks - stopping");
                stopSelf();
                return;
            }

            List<ScanResult> results = mWifiManager.getScanResults();
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (results != null && info != null) {
                StringBuffer buffer = new StringBuffer("t=" + System.currentTimeMillis() + ";pos=;id=" + info.getMacAddress());
                for (ScanResult result : results) {
                    buffer.append(";" + result.BSSID + "=" + result.level + "," + result.frequency + ",3,0");
                }
                if (mWifiPositioning != null) {
                    mWifiPositioning.addSample(buffer.toString());
                }
                fingerprintingModule.setLastKnownSample(buffer.toString());
            }
        }
    };
    private ProbabilityDistribution mLastPdf;

//    public static void startIfArrivedOtherwiseStop(final Context context, ParseModulePreferences parsePreferences) {
//        startIfArrivedOtherwiseStop(context, CircuitStarted.Recent.getSelected(parsePreferences), Guard.Recent.getSelected(parsePreferences));
//    }

//    public static void startIfArrivedOtherwiseStop(Context context) {
//        Guard guard =  new GuardCache(context).getLoggedIn();
//        startIfArrivedOtherwiseStop(context, guard);
//    }
//
//    private static void startIfArrivedOtherwiseStop(final Context context, final Guard guard) {
//
//
////        new Handler().postDelayed(new Runnable() {
////            @Override
////            public void run() {
//                Log.d(TAG, "startIfArrivedOtherwiseStop " + guard);
//                // turn off wifi service if not arrived at other circuitunits
//                new CircuitUnit().getQueryBuilder(true).matching(guard).build().findInBackground(new FindCallback<CircuitUnit>() {
//                    @Override
//                    public void done(List<CircuitUnit> circuitUnits, ParseException e) {
//
//                        if (e != null) {
//                            new HandleException(context, TAG, "startIfArrivedOtherwiseStop", e);
//                            return;
//                        }
//
//                        List<CircuitUnit> arrivedNotEnded = new ArrayList<CircuitUnit>();
//                        for (CircuitUnit circuitUnit: circuitUnits) {
//                            if (circuitUnit.isArrived() && !circuitUnit.isFinished() && circuitUnit.hasCheckPoints()) {
//                                arrivedNotEnded.addUnique(circuitUnit);
//                            }
//                        }
//                        Log.d(TAG, "startIfArrivedOtherwiseStop found " + arrivedNotEnded.size());
//                        if (arrivedNotEnded.size() == 0) {
//                            WiFiPositioningService.stop(context);
//                        } else {
//                            for (CircuitUnit circuitUnit: arrivedNotEnded) {
//                                if (circuitUnit.hasCheckPoints()) {
//                                    CircuitUnit.Recent.setArrived(circuitUnit);
//                                    WiFiPositioningService.start(context);
//                                    return;
//                                }
//                            }
//                            Log.d(TAG, "startIfArrivedOtherwiseStop no checkpoints");
//                            WiFiPositioningService.stop(context);
//                        }
//                    }
//                });
////            }
////        }, 3000);
//
//    }

    public static void start(Context context) {
        CircuitUnitCache tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache();
        if (!tasksCache.getArrived().isEmpty()) {
            Log.d(TAG, "Starting WiFi positioning");
            context.startService(new Intent(context, WiFiPositioningService.class));
        } else {
            Log.d(TAG, "Starting aborted - not arrived at any tasks");
        }
    }

    public static void stop(Context context) {
        Log.d(TAG, "Stopping");
        context.stopService(new Intent(context, WiFiPositioningService.class));
    }

    public class LocalBinder extends Binder {
        public WiFiPositioningService getService() {
            return WiFiPositioningService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "STARTING!");
        mStarted = true;
        Set<Fingerprint> storedFingerprints = null;
        Set<GSTask> arrivedTasks = tasksCache.getArrivedWithCheckpoints(GSTask.TASK_TYPE.REGULAR);
        storedFingerprints = fingerPrintIO.loadFingerprints(arrivedTasks);//new FileInputStream(preferences.getFingerprintFile(localData.getSelectedClient().getObjectId()));
        if (storedFingerprints == null || storedFingerprints.isEmpty()) {
            stopSelf();
        } else {
            mWifiPositioning = new WiFiPositioning(storedFingerprints);
            mWifiPositioning.addWiFiPositionListener(this);
            if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED && mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                mWifiManager.setWifiEnabled(true);
            }
            mScanTimer = new Timer();
            mScanTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (mWifiManager != null && mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                        mWifiManager.startScan();
                    }
                }
            }, new Date(), 500);
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mWifiPositioning != null) {
            mWifiPositioning.removeWiFiPositionListener(this);
        }
        if (mWeStartedWiFi) {
            mWifiManager.setWifiEnabled(false);
        }
        mStarted = false;
        if (mScanTimer != null) {
            mScanTimer.cancel();
        }
        unregisterReceiver(mWifiBroadcastReceiver);
        Log.i(TAG, "STOPPING!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void positionUpdated(PositionEstimate e) {
        sendBroadcast(new Intent().setAction(WIFI_POSITION_UPDATED_INTENTFILTER).putExtra(WIFI_POSITION, e.getCoordinates())
                .putExtra(WIFI_POSITION_PROBABILITY, e.getProbability()).putExtra(WIFI_POSITION_SYMBOLIC, e.getCoordinates().getSymbolic()).putExtra(WIFI_POSITION_ESTIMATED_DISTANCE, e.getDistance()));
    }

    public void addWiFiPositionListener(WiFiPositionListener listener) {
        if (mWifiPositioning != null) {
            mWifiPositioning.addWiFiPositionListener(listener);
        }
    }

    public void removeWiFiPositionListener(WiFiPositionListener listener) {
        if (mWifiPositioning != null) {
            mWifiPositioning.removeWiFiPositionListener(listener);
        }
    }

    public ProbabilityDistribution getLastProbabilityDistribution() {
        if (mLastPdf != null) {
            return mLastPdf;
        } else {
            return new ProbabilityDistribution(null, 0);
        }
    }

    public void positionUpdated(ProbabilityDistribution probabilityDistribution) {
        mLastPdf = probabilityDistribution;
        EventBus.getDefault().post(new WifiProbabilityDistributionEvent(probabilityDistribution));
    }

}
