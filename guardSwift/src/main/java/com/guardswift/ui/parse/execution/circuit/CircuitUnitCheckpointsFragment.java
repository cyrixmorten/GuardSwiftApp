package com.guardswift.ui.parse.execution.circuit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.guardswift.R;
import com.guardswift.core.ca.FingerprintingModule;
import com.guardswift.core.ca.fingerprinting.FingerPrintIO;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.dagger.InjectingListFragment;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.eventbus.events.WifiProbabilityDistributionEvent;
import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.data.checkpoint.CheckpointAdapter;
import com.guardswift.util.Analytics;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dk.alexandra.positioning.wifi.AccessPoint;
import dk.alexandra.positioning.wifi.Coordinates;
import dk.alexandra.positioning.wifi.Fingerprint;
import dk.alexandra.positioning.wifi.FingerprintBuilder;
import dk.alexandra.positioning.wifi.PositionEstimate;
import dk.alexandra.positioning.wifi.ProbabilityDistribution;
import dk.alexandra.positioning.wifi.SampleBuilder;
import dk.alexandra.positioning.wifi.tools.Helper;

public class CircuitUnitCheckpointsFragment extends InjectingListFragment {

    protected static final String TAG = CircuitUnitCheckpointsFragment.class
            .getSimpleName();

    private AsyncTask<Void, Long, Void> mWorker = null;
    private WifiManager mWifiManager;

    private boolean mWeStartedWiFi = false;
    private Location mFingerprintLocation;
    private FingerprintBuilder mFingerprintBuilder;
    private Set<Fingerprint> mFingerprints;
    Set<AccessPoint> mSamples;
    public SampleBuilder mSampleBuilder;
    private boolean mWifiUpdateReceived = false;


    public static CircuitUnitCheckpointsFragment newInstance(CircuitUnit circuitUnit) {

        GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache().setSelected(circuitUnit);

        CircuitUnitCheckpointsFragment fragment = new CircuitUnitCheckpointsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CircuitUnitCheckpointsFragment() {

    }


    @Inject
    FingerprintingModule fingerprintingModule;
    @Inject
    FingerPrintIO fingerprintIO;
    @Inject
    CircuitUnitCache circuitUnitCache;


    //    private ListAdapter mAdapter;
    private CheckpointAdapter mAdapter;
    private CircuitUnit mCircuitUnit;

    private String[] checkpointNames;
    private List<ClientLocation> checkpoints;

    BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            List<ScanResult> results = mWifiManager.getScanResults();
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (results != null && info != null) {
                StringBuffer buffer = new StringBuffer("t=" + System.currentTimeMillis() + ";pos=;id=" + info.getMacAddress());
                for (ScanResult result : results) {
                    // TODO: The substring is a hack to correct for APs at daimi
                    // with multiple MACs.
//					buffer.append(";" + result.BSSID.substring(0, result.BSSID.length() - 1) + "0" + "=" + result.level + "," + result.frequency + ",3,0");
                    buffer.append(";" + result.BSSID + "=" + result.level + "," + result.frequency + ",3,0");
                }
                if (mSampleBuilder != null) {
                    mSampleBuilder.addSample(buffer.toString());
                }
                mWifiUpdateReceived = true;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {


        mCircuitUnit = circuitUnitCache.getSelected();
        checkpoints = mCircuitUnit.getCheckpoints();
        checkpointNames = mCircuitUnit.getCheckpointsNamesAsArray();

        mFingerprints = fingerprintIO.loadFingerprints(mCircuitUnit);
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        mAdapter = new CheckpointAdapter(getActivity(), R.layout.view_adapter_item_checkpoint, checkpoints, Arrays.asList(checkpointNames));


//		mAdapter = new SimpleMultichoiceArrayAdapter(getActivity(), checkpointNames, checkpointsChecked);
//        mAdapter = new ArrayAdapter<String>(
//                getActivity(),
//                android.R.layout.simple_list_item_multiple_choice,
//                checkpointNames);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mWifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWeStartedWiFi) {
            mWifiManager.setWifiEnabled(false);
        }
        getActivity().unregisterReceiver(mWifiBroadcastReceiver);
        if (mWorker != null) {
            mWorker.cancel(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mRootView = inflater.inflate(R.layout.listview,
                container, false);

        ButterKnife.bind(this, mRootView);


        setListAdapter(mAdapter);

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        enabledIfArrived();
        updateCheckedState();

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String checkpointName = checkpointNames[position];
                final ClientLocation checkpoint = checkpoints.get(position);
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.update_data)
                        .positiveText(R.string.start)
                        .negativeText(R.string.not_now)
                        .neutralText(android.R.string.cancel)
                        .content(R.string.collect_fingerprints_message_existing)
                        .callback(new MaterialDialog.ButtonCallback() {
                                      @Override
                                      public void onPositive(MaterialDialog dialog) {
                                          startLearningCheckpoint(checkpoint, checkpointName);
                                      }
                                  }


                        ).show();
                return true;
            }
        });

    }

    private void updateCheckedState() {
        if (getListView() != null) {
            boolean[] checkedCheckpoints = mCircuitUnit.getCheckpointsCheckedArray();
            for (int i = 0; i < checkedCheckpoints.length; i++) {
                getListView().setItemChecked(i, checkedCheckpoints[i]);
            }
        }
    }

    private void enabledIfArrived() {
        boolean arrived = mCircuitUnit.isArrived();
        boolean finished = mCircuitUnit.isFinished();
        getListView().setEnabled(arrived && !finished);
    }

    @Override
    public void onEventMainThread(UpdateUIEvent ev) {
        Object obj = ev.getObject();
        if (obj instanceof Location) {
            return;
        }

        if (getListView() != null) {
            enabledIfArrived();
            updateCheckedState();
        }
    }

    Map<String, Integer> checkpointProbabilities = new HashMap<>();
    Map<String, PositionEstimate> checkpointEstimates = new HashMap<>();

    public void onEventMainThread(WifiProbabilityDistributionEvent ev) {
        if (getListView() != null && mAdapter != null) {
            ProbabilityDistribution pdf = ev.getProbabilityDistribution();
            checkpointProbabilities = new HashMap<>();
            checkpointEstimates = new HashMap<>();
            Collection<PositionEstimate> estimates = pdf.getDistribution();
            for (PositionEstimate est : estimates) {
                int percentage = (int) Math.round(est.getProbability() * 100);
                String symbolic = est.getCoordinates().getSymbolic();
                checkpointProbabilities.put(symbolic, percentage);
                checkpointEstimates.put(symbolic, est);
                Log.d(TAG, "EST: " + symbolic + " - " + percentage + " dist: " + est.getDistance());
            }
            mAdapter.setCheckpointProbabilities(checkpointProbabilities, checkpointEstimates);
        }
    }
    // @Override
    // public void onAttach(Activity activity) {
    // super.onAttach(activity);
    //
    // this.activity = ((MainActivity) activity);
    // this.activity
    // .onSectionAttached(NavigationDrawerFragment.POSITION_LOGOUT);
    // }

    private boolean savingCheckpoint;

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        final String checkpointName = checkpointNames[position];
        SparseBooleanArray checkedItems = l.getCheckedItemPositions();

        final boolean isChecked = checkedItems.valueAt(position);

        final ClientLocation checkpoint = checkpoints.get(position);

        if (isChecked) {
            if (!checkpoint.hasFingerprint()) {
                // we need training
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.missing_data)
                        .positiveText(R.string.start)
                        .negativeText(R.string.not_now)
                        .neutralText(android.R.string.cancel)
                        .content(R.string.collect_fingerprints_message_new)
                        .callback(new MaterialDialog.ButtonCallback() {
                                      @Override
                                      public void onPositive(MaterialDialog dialog) {
                                          startLearningCheckpoint(checkpoint, checkpointName);
                                          saveCheckpointEvent(checkpoint, true);
                                      }

                                      @Override
                                      public void onNegative(MaterialDialog dialog) {
                                          saveCheckpointEvent(checkpoint, true);
                                          Analytics.eventCheckpointAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Missed);
                                      }

                                      @Override
                                      public void onNeutral(MaterialDialog dialog) {
                                          // do nothing
                                          getListView().setItemChecked(position, false);
                                      }
                                  }


                        ).show();
            } else {
                // checkpoint missed by automatic proximity detection
                // TODO improve existing fingerprints or addUnique new fingerprint using same name

                Analytics.eventCheckpointAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Incorrect);

//                ClientLocation checkpointGuess = ClientLocation.Recent.getNearCheckpoint();
//                if (checkpointGuess != null && !checkpointGuess.equals(checkpoint)) {
//                    new MaterialDialog.Builder(getActivity())
//                            .title(R.string.question)
//                            .positiveText(R.string.start)
//                            .negativeText(R.string.not_now)
//                            .neutralText(android.R.string.cancel)
//                            .content(R.string.collect_fingerprints_message2, checkpoint.getLocations(), checkpointGuess.getLocations())
//                            .callback(new MaterialDialog.ButtonCallback() {
//                                          @Override
//                                          public void onPositive(MaterialDialog dialog) {
//                                              startLearningCheckpoint(checkpoint, checkpointName);
//                                              saveCheckpointEvent(checkpoint, true);
//                                          }
//
//                                          @Override
//                                          public void onNegative(MaterialDialog dialog) {
//                                              saveCheckpointEvent(checkpoint, true);
//                                          }
//
//                                          @Override
//                                          public void onNeutral(MaterialDialog dialog) {
//                                              // do nothing
//                                              getListView().setItemChecked(clientPosition, false);
//                                          }
//
//                                      }
//
//                            ).show();
//                } else {

                    //TODO confirm clientPosition before adjusting fingerprint
                    Set<AccessPoint> sample = fingerprintingModule.getLastKnownSample();
                    checkpoint.adjustFingerprint(sample);
                    saveCheckpointEvent(checkpoint, true);
//                }


            }

//            disableWithDelay();
        } else {
            saveCheckpointEvent(checkpoint, false);
        }


    }

    private void disableWithDelay() {
        getListView().setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getListView() != null)
                    getListView().setEnabled(true);
            }
        }, 1000);
    }

    public void storeFingerPrint(ClientLocation clientLocation, String checkpointName) {
        // see if we are updating existing
        Fingerprint fingerprintBeingEdited = findFingerprint(mFingerprints, checkpointName);

        Coordinates coordinates = null;
        if (mFingerprintLocation != null) { // See if the user picked a location or one was passed along
            coordinates = new Coordinates(mFingerprintLocation.getLongitude(), mFingerprintLocation.getLatitude(), mFingerprintLocation.getAltitude(), checkpointName);
        } else if (fingerprintBeingEdited != null) { // Otherwise, try to get one if the fingerprint is being edited
            Coordinates coordinatesEdit = fingerprintBeingEdited.getCoordinates();
            /** Now comments are updated when editing **/
            coordinates = new Coordinates(coordinatesEdit.getX(), coordinatesEdit.getY(), coordinatesEdit.getZ(), checkpointName);
        } else { // No location found, create an empty one
            coordinates = new Coordinates(0, 0, 0, checkpointName);
        }

        Fingerprint fingerprint = null;
        if (fingerprintBeingEdited != null && mFingerprintBuilder == null) {
            // If we're editing a fingerprint and no new measurement has been made, create from old data
            fingerprint = new Fingerprint(fingerprintBeingEdited.getId(), coordinates, fingerprintBeingEdited.getAverageSignalStrengths(), fingerprintBeingEdited.getStandardDeviations());

        } else {

            fingerprint = Helper.createFingerprint(coordinates, mFingerprintBuilder);

            if (fingerprintBeingEdited != null) {
                /** Fingerprint being edited is now replacing previous **/
                Fingerprint old = null;
                for (Fingerprint fingerprnt : mFingerprints) {
                    if (fingerprintBeingEdited.getId().equals(fingerprnt.getId())) {
                        old = fingerprnt;
                    }
                }
                mFingerprints.remove(old);
            }
        }

//        mFingerprints.remove(fingerprint);
        mFingerprints.add(fingerprint);

        final Context applicationContext = getActivity().getApplicationContext();
        // initially store the current fingerprint and samples
        fingerprintIO.storeFingerprint(mSamples, fingerprint, clientLocation, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // then store the collection of all fingerprints on the client
                fingerprintIO.storeFingerprints(mFingerprints, mCircuitUnit.getClient(), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (applicationContext != null) {
                            applicationContext.startService(new Intent(applicationContext, WiFiPositioningService.class));
                        }
                    }
                });
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (getActivity() != null) {
//                    Log.e(TAG, "Restarting WifiPositioningService");
//                    getActivity().startService(new Intent(getActivity(), WiFiPositioningService.class));
//                }
//            }
//        }, 1000);

    }

    private Fingerprint findFingerprint(Set<Fingerprint> fingerprints, String checkpointName) {
        for (Fingerprint fingerprint : fingerprints) {
            if (fingerprint.getCoordinates().getSymbolic().equals(checkpointName)) {
                return fingerprint;
            }
        }
        return null;
    }

    private void startLearningCheckpoint(ClientLocation clientLocation, String checkpointName) {
        if (mWorker == null) {
            mWorker = new FingerprintTask(clientLocation, checkpointName).execute();
        } else {
            mWorker.cancel(false);
        }
    }

    private void saveCheckpointEvent(final ClientLocation checkpoint, final boolean isChecked) {
//        if (!savingCheckpoint && isChecked) {
//            savingCheckpoint = true;

            // init checkpoint with latest probabilty and estimate
//            if (!checkpointProbabilities.isEmpty() && !checkpointEstimates.isEmpty()) {
//                PositionEstimate positionEstimate = checkpointEstimates.get(checkpoint.getLocations());
//                Integer probability = checkpointProbabilities.get(checkpoint.getLocations());
//
//                if (positionEstimate != null) {
//                    checkpoint.setLastDistance(positionEstimate.getDistance());
//                    checkpoint.setLastProbability(positionEstimate.getProbability());
//                }
//            }



            checkpoint.setChecked(isChecked);
            checkpoint.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), getString(R.string.error_an_error_occured), Toast.LENGTH_LONG).show();
                        Crashlytics.logException(e);
                        return;
                    }

                    if (getActivity() == null)
                        return;


                    if (isChecked) {
                        new EventLog.Builder(getActivity())
                                .taskPointer(mCircuitUnit, GSTask.EVENT_TYPE.CHECKPOINT)
                                .checkpoint(checkpoint, false)
                                .wifiSample(fingerprintingModule.getLastKnownSample())
                                .correctGuess(false)
                                .saveAsync();
                    }
                }
            });

            // delay  to avoid rapid array operations
//            final ProgressDialog dialog = new ProgressDialog(getActivity());
//            dialog.setMessage(getString(R.string.saving));
//            dialog.show();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    savingCheckpoint = false;
//                    dialog.cancel();
//                }
//            }, 1000);
//        }
    }

    private class FingerprintTask extends AsyncTask<Void, Long, Void> {

        private static final int DEFAULT_FINGERPRINT_DURATION = 30;
        private final ClientLocation clientLocation;
        private final String checkpointName;

        public MaterialDialog working_dialog;
        public MaterialDialog waiting_dialog;

        long startTime;
        long endTime;
        private Long progress;
        private int fingerprintDuration = DEFAULT_FINGERPRINT_DURATION * 1000;


        public FingerprintTask(ClientLocation clientLocation, String checkpointName) {
            this.clientLocation = clientLocation;
            this.checkpointName = checkpointName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            FingerprintingModule.Recent.setTraining(true);
//            TextView durationView = (TextView) findViewById(R.id.fingerprint_create_fingerprint_duration_text_view);
//            String durationText = durationView.getText().toString();
//            if (!"".equals(durationText)) {
//                try {
//                    int duration = Integer.parseInt(durationText);
//                    fingerprintDuration = duration * 1000;
//                } catch (NumberFormatException e) {
//                    // do nothing, should never happen
//                }
//            } else {
//                fingerprintDuration = DEFAULT_FINGERPRINT_DURATION;
//            }
            mFingerprintBuilder = null;
//            updateStoreButtonState();
            mSampleBuilder = Helper.startSample();
//            Button startButton = (Button) findViewById(R.id.fingerprint_create_start_button);
//            startButton.setText("Cancel");
//            startButton.setEnabled(true);
//            ProgressBar progressBar = (ProgressBar) findViewById(R.id.fingerprint_create_progress_bar);
//            progressBar.setVisibility(View.VISIBLE);
//            progressBar.setMax(fingerprintDuration);
            mWifiUpdateReceived = false;
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            waiting_dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.wifi_turning_on)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .show();

            Log.d(TAG, "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            progress = values[0];

            if (mWifiUpdateReceived) {
                if (waiting_dialog != null) {
                    waiting_dialog.dismiss();
                    waiting_dialog = null;
                }
                if (working_dialog == null) {
                    working_dialog = new MaterialDialog.Builder(getActivity())
                            .title(checkpointName)
                            .content(getString(R.string.learning_checkpoint))
                            .progress(false, DEFAULT_FINGERPRINT_DURATION)
                            .cancelable(false)
                            .negativeText(android.R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    FingerprintTask.this.cancel(true);
                                    super.onNegative(dialog);
                                }
                            })
                            .show();
                }
                working_dialog.setProgress(progress.intValue());
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground start");
            // Wait for the first wifi reading to come through
            while (!isCancelled() && mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED && !mWifiUpdateReceived) {
//                publishProgress(new Long(fingerprintDuration));
                if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                    if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                        mWeStartedWiFi = true;
                        mWifiManager.setWifiEnabled(true);
                    }
                }
                mWifiManager.startScan();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            startTime = System.currentTimeMillis();
            endTime = startTime + fingerprintDuration;
            while (!isCancelled() && (endTime - System.currentTimeMillis()) > 0 && mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                mWifiManager.startScan();
                long now = System.currentTimeMillis();

                long diff = (now - startTime);
                long diffSeconds = diff / 1000;
                publishProgress(diffSeconds);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            // The time is up, now to get the measurements
            if (!isCancelled() && mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                mFingerprintBuilder = Helper.startBuildingFingerprint();
                mSamples = Helper.stopSample(mSampleBuilder);
                for (AccessPoint accessPoint : mSamples) {
                    mFingerprintBuilder.addAccessPoint(accessPoint);
                }
            } else if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                cancel(false);
            }
            mWifiUpdateReceived = false;
            Log.d(TAG, "doInBackground end");
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (isAdded() && getActivity() != null) {
                Toast.makeText(getActivity(), getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                resetView();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            storeFingerPrint(clientLocation, checkpointName);
            resetView();

//            if (getActivity() != null) {
//                WiFiPositioningService.startIfArrivedOtherwiseStop(getActivity());
//            }
        }

        private void resetView() {

            FingerprintingModule.Recent.setTraining(false);

            if (waiting_dialog != null) {
                waiting_dialog.cancel();
            }
            if (working_dialog != null) {
                working_dialog.dismiss();
            }
            if (getActivity() != null) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            mWorker = null;
        }

    }

}
