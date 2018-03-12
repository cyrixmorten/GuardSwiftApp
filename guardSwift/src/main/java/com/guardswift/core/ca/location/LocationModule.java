package com.guardswift.core.ca.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.guardswift.util.GeocodedAddress;

import java.util.List;
import java.util.concurrent.TimeUnit;

import bolts.Task;
import bolts.TaskCompletionSource;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cyrix on 3/12/15.
 */
public class LocationModule {

    private static final String providerName = "MyFancyGPSProvider";

    public static class Recent {

        private static String TAG = "Locations.Recent";

        private static Location mPreviousKnownLocation;
        private static Location mLastKnownLocation;
        private static Location mLastKnownLocation_withSpeed;

        public static void setLastKnownLocation(Location location) {
            // update previous
            if (mLastKnownLocation != null) {
                mPreviousKnownLocation = mLastKnownLocation;
            }
            // update current
            mLastKnownLocation = location;
            // update with speed
            if (location.hasSpeed())
                mLastKnownLocation_withSpeed = location;
        }


        public static Location getLastKnownLocation() {
            return mLastKnownLocation;
        }

        public static Location getLastKnownLocationWithSpeed() {
            return mLastKnownLocation_withSpeed;
        }

        public static Location getPreviousKnownLocation() {
            return mPreviousKnownLocation;
        }
    }

    // Reverse geocode GPS position
    public static Task<GeocodedAddress> reverseGeocodedAddress(Context context) {
        final TaskCompletionSource<GeocodedAddress> result = new TaskCompletionSource<>();

        final ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        @SuppressLint("MissingPermission") Observable<Location> lastKnownLocationObservable = locationProvider.getLastKnownLocation();

        lastKnownLocationObservable
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.newThread())
                .timeout(5, TimeUnit.SECONDS)
                .flatMap(new Func1<Location, Observable<List<Address>>>() {
                    @Override
                    public Observable<List<Address>> call(Location location) {
                        return locationProvider.getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
                    }
                })
                .subscribe(new Action1<List<Address>>() {
                    @Override
                    public void call(List<Address> addresses) {
                        if (!addresses.isEmpty()) {
                            Address geoAddress = addresses.get(0);
                            result.setResult(new GeocodedAddress(geoAddress));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        result.setError(new Exception(throwable));
                    }
                });

        return result.getTask();
    }


}
