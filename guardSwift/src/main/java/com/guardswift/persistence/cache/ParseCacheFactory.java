package com.guardswift.persistence.cache;

import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.documentaion.EventLogCache;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.cache.planning.DistrictWatchStartedCache;
import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.cache.task.DistrictWatchClientCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.cache.task.StaticTaskCache;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class ParseCacheFactory {


    // Planning
    private final CircuitStartedCache circuitStartedCache;
    private final DistrictWatchStartedCache districtWatchStartedCache;

    // Tasks
    private GSTasksCache tasksCache; // adds extra functionality to task caches
    private final StaticTaskCache staticTaskCache;
    private final CircuitUnitCache circuitUnitCache;
    private final DistrictWatchClientCache districtWatchClientCache;
    // Data
    private final GuardCache guardCache;
    private final ClientCache clientCache;
    private final EventTypeCache eventTypeCache;
    // Documentation
    private final EventLogCache eventLogCache;


    @Inject
    public ParseCacheFactory(GuardCache guardCache, CircuitStartedCache circuitStartedCache, DistrictWatchStartedCache districtWatchStartedCache, GSTasksCache tasksCache, StaticTaskCache staticTaskCache, CircuitUnitCache circuitUnitCache, DistrictWatchClientCache districtWatchClientCache, ClientCache clientCache, EventTypeCache eventTypeCache, EventLogCache eventLogCache) {
        this.guardCache = guardCache;
        this.circuitStartedCache = circuitStartedCache;
        this.districtWatchStartedCache = districtWatchStartedCache;
        this.tasksCache = tasksCache;
        this.staticTaskCache = staticTaskCache;
        this.circuitUnitCache = circuitUnitCache;
        this.districtWatchClientCache = districtWatchClientCache;
        this.clientCache = clientCache;
        this.eventTypeCache = eventTypeCache;
        this.eventLogCache = eventLogCache;

    }


    // Task groups
    public CircuitStartedCache getCircuitStartedCache() {
        return circuitStartedCache;
    }
    public DistrictWatchStartedCache getDistrictWatchStartedCache() {
        return districtWatchStartedCache;
    }

    // Tasks
    public StaticTaskCache getStaticTaskCache() {
        return staticTaskCache;
    }

    public CircuitUnitCache getCircuitUnitCache() {
        return circuitUnitCache;
    }

    public DistrictWatchClientCache getDistrictWatchClientCache() {
        return districtWatchClientCache;
    }

    public GSTasksCache getTasksCache() {
        return tasksCache;
    }

    // Data
    public GuardCache getGuardCache() {
        return guardCache;
    }

    public ClientCache getClientCache() {
        return clientCache;
    }

    public EventTypeCache getEventTypeCache() {
        return eventTypeCache;
    }


    // Documentation
    public EventLogCache getEventLogCache() {
        return eventLogCache;
    }
}
