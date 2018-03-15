package com.guardswift.ui.map;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.ui.web.GoogleMapFragment;

/**
 * Created by cyrixmorten on 14/03/2018.
 */

public abstract class BaseMapFragment extends InjectingFragment implements OnMapReadyCallback  {

    private static final String TAG = BaseMapFragment.class.getSimpleName();

    protected abstract int getMapLayoutId();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                                .replace(getMapLayoutId(), mapFragment, "map")
                                .commitAllowingStateLoss();
                    }
                }
            }, 500);
        }
    }

}
