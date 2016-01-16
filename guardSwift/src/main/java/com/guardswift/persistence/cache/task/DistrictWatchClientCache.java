package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class DistrictWatchClientCache extends BaseTaskCache<DistrictWatchClient> {

    @Inject
    DistrictWatchClientCache(@ForApplication Context context) {
        super(DistrictWatchClient.class, context);
    }

    @Override
    public DistrictWatchClient getConcreteTask() {
        return new DistrictWatchClient();
    }
}
