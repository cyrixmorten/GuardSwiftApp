package com.guardswift.persistence.cache.planning;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.regular.Circuit;
import com.guardswift.persistence.parse.execution.regular.CircuitStarted;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class CircuitStartedCache extends ParseCache<CircuitStarted> {

    private static final String ACTIVE = "active";
    private static final String SELECTED = "selected";

    @Inject
    CircuitStartedCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(CircuitStarted.class, context);
    }

    public void setSelected(CircuitStarted circuitStarted) {
        put(SELECTED, circuitStarted);
    }

    public CircuitStarted getSelected() {
        return get(SELECTED);
    }

    public void addActive(CircuitStarted circuitStarted) {
        addUnique(ACTIVE, circuitStarted);
    }

    public CircuitStarted matching(Circuit circuit) {
        Set<CircuitStarted> circuitsStarted = getSet(ACTIVE);
        for (CircuitStarted circuitStarted: circuitsStarted) {
            if (circuitStarted.getCircuit().equals(circuit))
                return circuitStarted;
        }
        return null;
    }
}
