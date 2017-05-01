package com.guardswift.ui.parse.data.tracker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TrackerMapFragment extends InjectingFragment implements OnMapReadyCallback {

    private static final String TAG = TrackerMapFragment.class.getSimpleName();

    public static TrackerMapFragment newInstance(Tracker tracker) {
        TrackerMapFragment fragment = new TrackerMapFragment();
        fragment.setTracker(tracker);
        return fragment;
    }

    @Bind(R.id.btn_next)
    Button nextButton;

    private GoogleMap googleMap;
    private Tracker tracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracker_map,
                container, false);

        ButterKnife.bind(this, rootView);

        addMapFragment();


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

                    // load whole track
                    googleMap.clear();
                    List<LatLng> positions = getTrack(trackerDataArray);
                    addPolyLines(googleMap, positions);
                    zoomFit(googleMap, positions);

                    nextButton.setEnabled(true);

                    final int step = 10;
                    final int[] startIndex = {0};
                    nextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            int maxIndex = trackerDataArray.length;
                            int targetEndIndex = startIndex[0] + step;
                            int endIndex = targetEndIndex <= maxIndex ? targetEndIndex : maxIndex;

                            List<TrackerData> track = Lists.newArrayList();
                            track.addAll(Arrays.asList(trackerDataArray).subList(startIndex[0], endIndex));

                            googleMap.clear();
                            List<LatLng> positions = getTrack(track.toArray(new TrackerData[0]));
                            addPolyLines(googleMap, positions);
                            zoomFit(googleMap, positions);

                            startIndex[0] += step;

                            if (startIndex[0] > maxIndex) {
                                startIndex[0] = 0;
                            }

                        }
                    });


                }
            });
        }
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
                previous = positions.get(index-1);
                distance = ParseModule.distanceBetweenMeters(previous, next);
            }

            boolean skipped = false;

            if (previous != null) {

                if (distance > 1000) {
                    skipped = true;
                }

                Log.d(TAG, "distance: " + distance);
            }

            if (!skipped) {
                distanceTraveled += distance;
                positions.add(next);
                index++;
            } else {
                Log.d(TAG, "skipped");
            }


        }

        Log.d(TAG, "Total disatance: " + distanceTraveled);


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
