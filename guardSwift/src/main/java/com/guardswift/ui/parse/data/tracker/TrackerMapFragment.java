package com.guardswift.ui.parse.data.tracker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.documentation.gps.TrackerData;
import com.guardswift.ui.web.GoogleMapFragment;
import com.parse.ProgressCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.guardswift.R.id.chart;

public class TrackerMapFragment extends InjectingFragment implements OnMapReadyCallback {

    private static final String TAG = TrackerMapFragment.class.getSimpleName();

    public static TrackerMapFragment newInstance(Tracker tracker) {
        TrackerMapFragment fragment = new TrackerMapFragment();
        fragment.setTracker(tracker);
        return fragment;
    }

    @BindView(chart)
    LineChart mChart;

    private GoogleMap googleMap;
    private Tracker tracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracker_map,
                container, false);

        ButterKnife.bind(this, rootView);

        addMapFragment();

        setRetainInstance(true);

        return rootView;
    }

    private void addMapFragment() {

        final FragmentManager fm = getChildFragmentManager();

        GoogleMapFragment mapFragment = (GoogleMapFragment) fm.findFragmentByTag("map");

        if (mapFragment != null) {
            Log.e(TAG, "reusing map");
            fm.beginTransaction().attach(mapFragment).commit();

            mapFragment.getMapAsync(this);

        } else {
            Log.e(TAG, "new map");
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (isAdded()) {

                        GoogleMapFragment mapFragment = GoogleMapFragment
                                .newInstance();
                        fm.beginTransaction()
                                .replace(R.id.layout_map, mapFragment, "map")
                                .commitAllowingStateLoss();
                    }
                }
            }, 500);
        }
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;

        Log.d(TAG, "Minutes: " + tracker.getMinutes());
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: " + (tracker != null));
        if (tracker != null) {
            loadTrack(tracker, new Tracker.DownloadTrackerDataCallback() {
                @Override
                public void done(final TrackerData[] trackerDataArray, Exception e) {
                    if (e != null) {
                        Log.e(TAG, "Unable to download file", e);
                        return;
                    }

                    addGraphData(trackerDataArray);

                    googleMap.clear();

                    List<LatLng> positions = getTrack(trackerDataArray);
                    addPolyLines(googleMap, positions);
                    zoomFit(googleMap, positions);


                }
            });
        }
    }

    private void addGraphData(TrackerData[] trackerDataArray) {

        List<Entry> still = Lists.newArrayList();
        List<Entry> walking = Lists.newArrayList();
        List<Entry> running = Lists.newArrayList();
        List<Entry> driving = Lists.newArrayList();
        List<Entry> other = Lists.newArrayList();

        for (TrackerData trackerData : trackerDataArray) {

            Entry entry = new Entry(trackerData.getTime(), trackerData.getSpeed());

            Log.d(TAG, entry.toString());

            switch (trackerData.getActivityType()) {
                case DetectedActivity.STILL: still.add(entry); break;
                case DetectedActivity.ON_FOOT:
                case DetectedActivity.WALKING: walking.add(entry); break;
                case DetectedActivity.RUNNING: running.add(entry); break;
                case DetectedActivity.IN_VEHICLE: driving.add(entry); break;
                case DetectedActivity.ON_BICYCLE:
                case DetectedActivity.TILTING:
                case DetectedActivity.UNKNOWN: other.add(entry); break;
            }
        }

        Log.d(TAG, "Still: " + still.size());
        Log.d(TAG, "Walking: " + walking.size());
        Log.d(TAG, "Running: " + running.size());
        Log.d(TAG, "Driving: " + driving.size());
        Log.d(TAG, "Other: " + other.size());

        LineDataSet stillDataSet = new LineDataSet(still, "Still"); // add entries to dataset
        stillDataSet.setColor(Color.BLUE);
//        dataSet.setValueTextColor(...); // styling, ...

        LineDataSet walkingDataSet = new LineDataSet(walking, "Walking"); // add entries to dataset
        walkingDataSet.setColor(Color.CYAN);

        LineDataSet runningDataSet = new LineDataSet(walking, "Running"); // add entries to dataset
        runningDataSet.setColor(Color.YELLOW);

        LineDataSet drivingDataSet = new LineDataSet(walking, "Driving"); // add entries to dataset
        drivingDataSet.setColor(Color.RED);
//        drivingDataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.fade_red));



        LineDataSet otherDataSet = new LineDataSet(walking, "Other"); // add entries to dataset
        otherDataSet.setColor(Color.MAGENTA);

        LineData lineData = new LineData(stillDataSet, walkingDataSet, runningDataSet, drivingDataSet, otherDataSet);
//        lineData.setValueFormatter(new IValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                Log.d(TAG, "Format: " + entry.toString());
//                return String.valueOf(value);
//            }
//        });
        mChart.setData(lineData);
        mChart.getDescription().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                SimpleDateFormat sdf = new SimpleDateFormat("mm:HH", Locale.getDefault());

                return sdf.format(new Date((long)value));
            }
        });
        mChart.invalidate(); // refresh
    }



    private void addPolyLines(GoogleMap googleMap, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(5).color(Color.RED);
        options.addAll(positions);
        googleMap.addPolyline(options);
    }

    private List<LatLng> getTrack(TrackerData[] trackerDataArray) {
        List<LatLng> positions = Lists.newArrayList();


        long distanceTraveled = 0;

        LatLng previous = null;

        int index = 0;
        for (TrackerData trackerData : trackerDataArray) {
            LatLng next = new LatLng(trackerData.getLatitude(), trackerData.getLongitude());

            float distance = 0;
            if (index > 0) {
                previous = positions.get(index - 1);
                distance = ParseModule.distanceBetweenMeters(previous, next);
            }

            if (trackerData.getActivityType() == DetectedActivity.IN_VEHICLE) {
                distanceTraveled += distance;
            }

            positions.add(next);
            index++;


        }

        Log.d(TAG, "Total distance: " + distanceTraveled);


        return positions;
    }

    private void zoomFit(GoogleMap googleMap, List<LatLng> positions) {
        Log.d(TAG, "Positions: " + positions.size());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : positions) {
            builder.include(position);
        }
        LatLngBounds bounds = builder.build();

        int padding = 25; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        googleMap.moveCamera(cu);

        googleMap.animateCamera(cu);
    }

    private void loadTrack(Tracker tracker, Tracker.DownloadTrackerDataCallback callback) {
        tracker.downloadTrackerData(callback, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                Log.d(TAG, "Percent: " + percentDone);
            }
        });
    }
}
