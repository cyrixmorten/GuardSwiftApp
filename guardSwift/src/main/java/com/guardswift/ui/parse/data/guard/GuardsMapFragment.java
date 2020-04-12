package com.guardswift.ui.parse.data.guard;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Lists;
import com.google.maps.android.ui.IconGenerator;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.query.GuardQueryBuilder;
import com.guardswift.ui.map.BaseMapFragment;
import com.guardswift.util.ToastHelper;
import com.guardswift.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;
import static android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

/**
 * Created by cyrixmorten on 14/03/2018.
 */
public class GuardsMapFragment extends BaseMapFragment {

    private static final String TAG = GuardsMapFragment.class.getSimpleName();

    public static GuardsMapFragment newInstance() {
        return new GuardsMapFragment();
    }


    GoogleMap googleMap;

    private long mInterval = TimeUnit.SECONDS.toMillis(10);
    private Handler mHandler;
    boolean hasPerformedZoomFit;

    @Override
    protected int getMapLayoutId() {
        return R.id.layout_map;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guards_map,
                container, false);

        setRetainInstance(true);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;

        startRepeatingTask();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopRepeatingTask();
    }

    @Override
    public void onResume() {
        if (googleMap != null) {
            startRepeatingTask();
        }
        super.onResume();
    }

    Runnable mPoolUpdate = new Runnable() {
        @Override
        public void run() {
            update(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    mHandler.postDelayed(mPoolUpdate, mInterval);
                }
            });
        }
    };

    void startRepeatingTask() {
        mPoolUpdate.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mPoolUpdate);
    }

    private void update(final SaveCallback callback) {
        new GuardQueryBuilder(false).build().findInBackground(new FindCallback<Guard>() {
            @Override
            public void done(List<Guard> list, ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage(), e);
                    ToastHelper.toast(getContext(), getString(R.string.title_internet_missing));
                    return;
                }

                if (isAdded()) {
                    List<LatLng> positions = addGuardMarkers(list);

                    if (!hasPerformedZoomFit) {
                        zoomFit(positions);
                    }

                    callback.done(null);
                }
            }
        });
    }

    private List<LatLng> addGuardMarkers(List<Guard> guards) {

        googleMap.clear();

        List<LatLng> positions = Lists.newArrayList();

        IconGenerator iconFactory = new IconGenerator(getContext());

        for (Guard guard : guards) {
            ParseGeoPoint position = guard.getPosition();


            if (position != null) {
                LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());

                // Assume offline (red)
                float zIndex = 1;
                iconFactory.setStyle(IconGenerator.STYLE_RED);

                // Online (green)
                if (guard.isOnline()) {
                    zIndex = 2;
                    iconFactory.setStyle(IconGenerator.STYLE_GREEN);
                }

                // Inactive (grey)
                Date positionUpdatedAt = guard.getPositionUpdatedAt();
                if (positionUpdatedAt == null) {
                    positionUpdatedAt = new Date(0);
                }
                int daysOld = Days.daysBetween(new DateTime(positionUpdatedAt), new DateTime()).getDays();

                if (daysOld > 1) {
                    zIndex = 0;
                    iconFactory.setStyle(IconGenerator.STYLE_DEFAULT);
                }

                googleMap.addMarker(createMarker(iconFactory, guard, latLng, zIndex));

                positions.add(latLng);
            }
        }

        return positions;
    }

    private MarkerOptions createMarker(IconGenerator iconFactory, Guard guard, LatLng position, float zIndex) {
        return new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(markerText(guard)))).
                position(position).
                zIndex(zIndex).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
    }


    private String markerText(Guard guard) {
        String prefix = guard.getName();
        String suffix = guard.getPositionUpdatedAt() != null ?
                Util.relativeTimeString(guard.getPositionUpdatedAt()) : "";
        String sequence = prefix + " " + suffix;
        SpannableStringBuilder ssb = new SpannableStringBuilder(sequence);
        ssb.setSpan(new StyleSpan(ITALIC), 0, prefix.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(BOLD), prefix.length(), sequence.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb.toString();
    }

    private void zoomFit(List<LatLng> positions) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : positions) {
            builder.include(position);
        }
        LatLngBounds bounds = builder.build();

        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        googleMap.moveCamera(cu);

        googleMap.animateCamera(cu);

        hasPerformedZoomFit = true;
    }
}