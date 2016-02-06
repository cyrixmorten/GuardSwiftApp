package com.guardswift.persistence.cache.planning;

import android.content.Context;

import com.google.common.collect.Lists;
import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.task.regular.Circuit;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class CircuitStartedCache extends ParseCache<CircuitStarted> {

    private static final String TAG = CircuitStartedCache.class.getSimpleName();

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

        List<CircuitStarted> candidates = Lists.newArrayList();
        for (CircuitStarted circuitStarted: circuitsStarted) {
//            Log.w(TAG, "Active: " + circuitStarted.getName() + " - " + circuitStarted.getCreatedAt());
            if (circuitStarted.getCircuit().equals(circuit))
                candidates.add(circuitStarted);
        }
//        Log.w(TAG, "Candidates: " + candidates.size());
        CircuitStarted mostRecent = null;
        for (CircuitStarted candidate: candidates) {
            if (mostRecent == null) {
                mostRecent = candidate;
            } else {
                if (new DateTime(candidate.getCreatedAt()).isAfter(new DateTime(mostRecent.getCreatedAt()))) {
                    mostRecent = candidate;
                }
            }
        }
//        Log.w(TAG, "Mostrecent: " + mostRecent);
        return mostRecent;
    }


}
