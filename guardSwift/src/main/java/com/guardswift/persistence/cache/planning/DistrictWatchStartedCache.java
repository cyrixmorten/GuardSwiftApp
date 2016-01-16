package com.guardswift.persistence.cache.planning;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchStarted;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class DistrictWatchStartedCache extends ParseCache<DistrictWatchStarted> {

    private static final String SELECTED = "selected";

    @Inject
    DistrictWatchStartedCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(DistrictWatchStarted.class, context);
    }

    public void setSelected(DistrictWatchStarted districtWatchStarted) {
        put(SELECTED, districtWatchStarted);
    }

    public DistrictWatchStarted getSelected() {
        return get(SELECTED);
    }
}
