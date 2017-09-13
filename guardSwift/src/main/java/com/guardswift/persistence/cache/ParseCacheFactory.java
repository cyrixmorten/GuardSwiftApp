package com.guardswift.persistence.cache;

import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.documentaion.EventLogCache;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.cache.task.TaskCache;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class ParseCacheFactory {


    // Planning
    private final TaskGroupStartedCache taskGroupStartedCache;

    // Tasks
    private ParseTasksCache tasksCache; // adds extra functionality to task caches
    private final TaskCache taskCache;
//    private final StaticTaskCache staticTaskCache;
//    private final CircuitUnitCache circuitUnitCache;
//    private final DistrictWatchClientCache districtWatchClientCache;
    // Data
    private final GuardCache guardCache;
    private final ClientCache clientCache;
    private final EventTypeCache eventTypeCache;
    // Documentation
    private final EventLogCache eventLogCache;


    @Inject
    public ParseCacheFactory(GuardCache guardCache,
                             TaskGroupStartedCache taskGroupStartedCache,
                             ParseTasksCache tasksCache,
                             TaskCache taskCache,
//                             StaticTaskCache staticTaskCache,
//                             CircuitUnitCache circuitUnitCache,
//                             DistrictWatchClientCache districtWatchClientCache,
                             ClientCache clientCache,
                             EventTypeCache eventTypeCache,
                             EventLogCache eventLogCache) {

        this.guardCache = guardCache;
        this.taskGroupStartedCache = taskGroupStartedCache;
        this.tasksCache = tasksCache;
        this.taskCache = taskCache;
//        this.staticTaskCache = staticTaskCache;
//        this.circuitUnitCache = circuitUnitCache;
//        this.districtWatchClientCache = districtWatchClientCache;
        this.clientCache = clientCache;
        this.eventTypeCache = eventTypeCache;
        this.eventLogCache = eventLogCache;

    }


    // ParseTask groups
    public TaskGroupStartedCache getTaskGroupStartedCache() {
        return taskGroupStartedCache;
    }

    // Tasks
    public TaskCache getTaskCache() {
        return taskCache;
    }

//    public StaticTaskCache getStaticTaskCache() {
//        return staticTaskCache;
//    }
//
//    public CircuitUnitCache getCircuitUnitCache() {
//        return circuitUnitCache;
//    }
//
//    public DistrictWatchClientCache getDistrictWatchClientCache() {
//        return districtWatchClientCache;
//    }

    public ParseTasksCache getTasksCache() {
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
