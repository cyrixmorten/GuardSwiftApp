package com.guardswift.ui.parse.data.tracker;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.core.exceptions.LogError;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.documentation.gps.TrackerData;
import com.guardswift.ui.activity.AbstractToolbarActivity;
import com.guardswift.ui.activity.SlidingPanelActivity;
import com.guardswift.ui.map.BaseMapFragment;
import com.guardswift.util.ToastHelper;
import com.guardswift.util.Util;
import com.parse.ProgressCallback;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PreviewLineChartView;

import static com.guardswift.R.id.preview_chart;
import static com.guardswift.R.id.tv_shown_timespan;
import static com.guardswift.util.Util.dateFormatHourMinutes;

public class TrackerMapFragment extends BaseMapFragment {

    private static final String TAG = TrackerMapFragment.class.getSimpleName();

    public static TrackerMapFragment newInstance(Tracker tracker) {
        TrackerMapFragment fragment = new TrackerMapFragment();
        fragment.setTracker(tracker);
        return fragment;
    }


    @BindView(tv_shown_timespan)
    TextView tvTimespan;

    @BindView(preview_chart)
    PreviewLineChartView mPreviewChart;


    private Unbinder unbinder;

//    private LineChartData chartData;
    private LineChartData previewData;

    private Tracker tracker;

    private TrackerData[] mTrackerData;
    private List<LatLng> mPositions;

    private ActionBar mActionBar;
    private SlidingUpPanelLayout mSlideUpPanel;

    private final int RENDER_TRACK_MILISECONDS = 2000;

    @Override
    protected int getMapLayoutId() {
        return R.id.layout_map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracker_map,
                container, false);

        unbinder = ButterKnife.bind(this, rootView);

        setRetainInstance(true);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = getActivity();
        if (activity instanceof AbstractToolbarActivity) {
            AbstractToolbarActivity parentActivity = ((AbstractToolbarActivity)activity);

            mActionBar = parentActivity.getSupportActionBar();
            mSlideUpPanel = parentActivity.getSlidingPanelLayout();
            mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            mSlideUpPanel.setTouchEnabled(false);
            mSlideUpPanel.setAnchorPoint(0.3f);
            mSlideUpPanel.setFadeOnClickListener(view1 -> mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
            mSlideUpPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                    // disable drag of slide panel to allow scrolling of content
//                    mSlideUpPanel.setTouchEnabled(newState != SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            });

            setSlidingPanelTitle(getString(R.string.click_route), "");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();

        mActionBar = null;
        mSlideUpPanel = null;
//        previewData = null;
//        tracker = null;
//        mPositions = null;
    }



    private void updateTrackData(TrackerData[] trackerData) {
        mTrackerData = trackerData;
        mPositions = getTrack(trackerData);

        if (mActionBar != null && mTrackerData.length > 0) {
            TrackerData first = mTrackerData[0];
            TrackerData last = mTrackerData[mTrackerData.length - 1];

            String startTimeString = Util.dateFormatHourMinutes().format(new Date(first.getTime()));
            String endTimeString = Util.dateFormatHourMinutes().format(new Date(last.getTime()));

            CharSequence actionBarTitleChars = mActionBar.getTitle();

            if (actionBarTitleChars != null) {
                String newActionBarTitle = actionBarTitleChars + " " + getString(R.string.timespan, startTimeString, endTimeString);
                mActionBar.setTitle(newActionBarTitle);
            }

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
            loadTrack(tracker, (trackerData, e) -> {
                if (e != null) {
                    LogError.log(TAG, "Unable to download trackerData for Tracker: " + tracker.getObjectId(), e);
                    return;
                }

                updateTrackData(trackerData);


//                    mChart.setLineChartData(chartData);
//                    // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
//                    // zoom/scroll is unnecessary.
//                    mChart.setZoomEnabled(false);
//                    mChart.setScrollEnabled(false);




                if (mPositions.size() > 50) {
                    addGraphData(trackerData);
                    mPreviewChart.setLineChartData(previewData);
                    mPreviewChart.setViewportChangeListener(new ViewportListener(googleMap));

                    previewX(false, 10);
                } else {
                    mPreviewChart.setVisibility(View.GONE);
                }



                googleMap.clear();



                if (!mPositions.isEmpty()) {
                    zoomFit(googleMap, mPositions);
                    addPolyLines(googleMap, mPositions);
                } else {
                    ToastHelper.toast(getContext(), getString(R.string.error_downloading_file));
                }

            });
        }
    }


    private void previewX(boolean animate, float viewportWidth) {

        Viewport maxViewport = mPreviewChart.getMaximumViewport();

        Viewport tempViewport = new Viewport(0, maxViewport.top, viewportWidth, maxViewport.bottom);

        if (animate) {
            mPreviewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            mPreviewChart.setCurrentViewport(tempViewport);
        }
        mPreviewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    private void addGraphData(TrackerData[] trackerDataArray) {

        List<PointValue> values = new ArrayList<>();

        int i = 0;
        for (TrackerData trackerData : trackerDataArray) {
            values.add(new PointValue(i, trackerData.getSpeedKmH()));
            i++;
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_BLUE);
        line.setHasPoints(false);// too many values so don't draw points.

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        previewData = new LineChartData(lines);
        previewData.setAxisXBottom(new Axis());
        previewData.setAxisYLeft(new Axis().setHasLines(true));


    }


    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {


        private GoogleMap googleMap;

        ViewportListener(GoogleMap googleMap) {
            this.googleMap = googleMap;
        }

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart.
//            mChart.setCurrentViewport(newViewport);

            if (mPositions != null && googleMap != null) {
                int startIndex = (int) newViewport.left;
                int endIndex = (int) newViewport.right;


                if (mPositions.size() >= startIndex && endIndex <= mPositions.size() - 1) {
                    zoomFit(googleMap, mPositions.subList(startIndex, endIndex));

                    TrackerData start = mTrackerData[startIndex];
                    TrackerData end = mTrackerData[endIndex];

                    String startHourMinutes = dateFormatHourMinutes().format(new Date(start.getTime()));
                    String endHourMinutes = dateFormatHourMinutes().format(new Date(end.getTime()));

                    tvTimespan.setText(getString(R.string.shown_timespan) + " " + getString(R.string.timespan, startHourMinutes, endHourMinutes));
                }
            }
        }

    }

    private class TrackerPolyline {
        private List<LatLng> positions;
        private TrackerData trackerData;

        TrackerPolyline(List<LatLng> positions, TrackerData trackerData) {
            this.positions = positions;
            this.trackerData = trackerData;
        }

        List<LatLng> getPositions() {
            return positions;
        }

        TrackerData getTrackerData() {
            return trackerData;
        }


    }

    private void addPolyLines(final GoogleMap googleMap, final List<LatLng> positions) {
        final List<TrackerPolyline> trackerPolylines = Lists.newArrayList();

        final int DELAY_PER_RENDER = RENDER_TRACK_MILISECONDS / positions.size();

        int DELAY_NEXT_RENDER = DELAY_PER_RENDER;

        for (int i = 0; i<positions.size()-1; i++) {
            TrackerData trackerData = mTrackerData[i];


            final PolylineOptions option = new PolylineOptions()
                    .width(5)
                    .color(ActivityDetectionModule.getColorFromType(trackerData.getActivityType()));
            option.clickable(true);

            LatLng a = positions.get(i);
            LatLng b = positions.get(i+1);

            option.add(a);
            option.add(b);

            trackerPolylines.add(
                    new TrackerPolyline(Arrays.asList(a, b), trackerData)
            );

            new Handler().postDelayed(() -> {
                if (isAdded()) {
                    googleMap.addPolyline(option);
                }
            }, DELAY_NEXT_RENDER);

            DELAY_NEXT_RENDER += DELAY_PER_RENDER;
        }


        googleMap.setOnPolylineClickListener(polyline -> {
            Log.d(TAG, polyline.getPoints().toString());
            for (TrackerPolyline trackerPolyline: trackerPolylines) {
                if (trackerPolyline.getPositions().equals(polyline.getPoints())) {
                    Log.d(TAG, trackerPolyline.getTrackerData().toString());

                    TrackerData data = trackerPolyline.getTrackerData();

                    String time = data.getHumanReadableLongDate(getContext());
                    String activity = getString(R.string.activity) + ": " + ActivityDetectionModule.getHumanReadableNameFromType(getContext(), data.getActivityType());
                    setSlidingPanelTitle(time, activity);

                    mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    mSlideUpPanel.setTouchEnabled(true);
                    setSlidingPanelFragment(TrackerDataFragment.newInstance(data));
                }
            }
        });

        googleMap.setOnMapClickListener(latLng -> {
            Log.d(TAG, "Map clicked at: " + latLng);
            setSlidingPanelTitle(getString(R.string.position), getString(R.string.latlng, latLng.latitude, latLng.longitude));

            mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            mSlideUpPanel.setTouchEnabled(false);
        });
    }

    private void setSlidingPanelTitle(String title, String subTitle) {
        if (getActivity() instanceof SlidingPanelActivity) {
            ((SlidingPanelActivity)getActivity()).setSlidingTitle(title, subTitle);
        }
    }

    private void setSlidingPanelFragment(Fragment fragment) {
        if (getActivity() instanceof SlidingPanelActivity) {
            ((SlidingPanelActivity)getActivity()).setSlidingContent(fragment);
        }
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
        tracker.downloadTrackerData(callback, percentDone -> Log.d(TAG, "Percent: " + percentDone));
    }

}
