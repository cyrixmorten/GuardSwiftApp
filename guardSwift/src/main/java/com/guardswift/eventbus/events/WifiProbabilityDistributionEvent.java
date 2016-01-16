package com.guardswift.eventbus.events;

import dk.alexandra.positioning.wifi.ProbabilityDistribution;

/**
 * Created by cyrix on 2/19/15.
 */
public class WifiProbabilityDistributionEvent {


    private final ProbabilityDistribution probabilityDistribution;

    public WifiProbabilityDistributionEvent(ProbabilityDistribution probabilityDistribution) {
        this.probabilityDistribution = probabilityDistribution;
    }

    public ProbabilityDistribution getProbabilityDistribution() {
        return probabilityDistribution;
    }
}
