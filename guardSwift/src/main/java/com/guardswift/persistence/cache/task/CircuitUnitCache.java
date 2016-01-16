package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.persistence.parse.execution.regular.CircuitUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class CircuitUnitCache extends BaseTaskCache<CircuitUnit> {

    @Inject
    CircuitUnitCache(@ForApplication Context context) {
        super(CircuitUnit.class, context);
    }

    @Override
    public CircuitUnit getConcreteTask() {
        return new CircuitUnit();
    }

}
