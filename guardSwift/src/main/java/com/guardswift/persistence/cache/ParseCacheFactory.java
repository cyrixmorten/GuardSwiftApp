package com.guardswift.persistence.cache;

import com.google.common.collect.Lists;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.documentaion.EventLogCache;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.cache.planning.DistrictWatchStartedCache;
import com.guardswift.persistence.cache.task.AlarmCache;
import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.cache.task.DistrictWatchClientCache;
import com.guardswift.persistence.cache.task.GSTasksCache;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class ParseCacheFactory {


    private List<ParseCache> allCaches = Lists.newArrayList();
    // Planning
    private CircuitStartedCache circuitStartedCache;
    private DistrictWatchStartedCache districtWatchStartedCache;

    // Tasks
    private GSTasksCache tasksCache; // adds extra functionality to task caches
    private AlarmCache alarmCache;
    private CircuitUnitCache circuitUnitCache;
    private DistrictWatchClientCache districtWatchClientCache;
    // Data
    private GuardCache guardCache;
    private ClientCache clientCache;
    private EventTypeCache eventTypeCache;
    // Documentation
    private final EventLogCache eventLogCache;


    @Inject
    public ParseCacheFactory(GuardCache guardCache, CircuitStartedCache circuitStartedCache, DistrictWatchStartedCache districtWatchStartedCache, GSTasksCache tasksCache, AlarmCache alarmCache, CircuitUnitCache circuitUnitCache, DistrictWatchClientCache districtWatchClientCache, ClientCache clientCache, EventTypeCache eventTypeCache, EventLogCache eventLogCache) {
        this.guardCache = guardCache;
        this.circuitStartedCache = circuitStartedCache;
        this.districtWatchStartedCache = districtWatchStartedCache;
        this.tasksCache = tasksCache;
        this.alarmCache = alarmCache;
        this.circuitUnitCache = circuitUnitCache;
        this.districtWatchClientCache = districtWatchClientCache;
        this.clientCache = clientCache;
        this.eventTypeCache = eventTypeCache;
        this.eventLogCache = eventLogCache;

        allCaches.add(guardCache);
        allCaches.add(circuitStartedCache);
        allCaches.add(districtWatchStartedCache);
        allCaches.add(alarmCache);
        allCaches.add(circuitUnitCache);
        allCaches.add(districtWatchClientCache);
        allCaches.add(clientCache);
        allCaches.add(eventTypeCache);
        allCaches.add(eventLogCache);
    }

    public GuardCache getGuardCache() {
        return guardCache;
    }

    public CircuitStartedCache getCircuitStartedCache() {
        return circuitStartedCache;
    }

    public AlarmCache getAlarmCache() {
        return alarmCache;
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


    public ClientCache getClientCache() {
        return clientCache;
    }

    public EventTypeCache getEventTypeCache() {
        return eventTypeCache;
    }

    public DistrictWatchStartedCache getDistrictWatchStartedCache() {
        return districtWatchStartedCache;
    }

    public EventLogCache getEventLogCache() {
        return eventLogCache;
    }
}
