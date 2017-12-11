package com.guardswift.persistence.parse.query;


import com.google.common.collect.Lists;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Arrays;
import java.util.List;

import static com.guardswift.persistence.parse.documentation.event.EventLog.eventCode;


public class EventLogQueryBuilder extends
        ParseQueryBuilder<EventLog> {

    public EventLogQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(EventLog.class));
    }

    @Override
    public ParseQuery<EventLog> build() {
        return super.build();
    }

    public EventLogQueryBuilder matching(Guard guard) {
        query.whereEqualTo(EventLog.guard, guard);
        return this;
    }

    public EventLogQueryBuilder matching(Client client) {
        query.whereEqualTo(EventLog.client, client);

        return this;
    }

    public EventLogQueryBuilder matching(ParseTask task) {
        query.whereEqualTo(EventLog.task, task);

        return this;
    }

    public EventLogQueryBuilder matching(TaskGroupStarted taskGroupStarted) {
        if (taskGroupStarted == null) {
            return this;
        }

        query.whereEqualTo(EventLog.taskGroupStarted, taskGroupStarted);

        return this;
    }

    public EventLogQueryBuilder eventCode(int eventCode) {
        query.whereEqualTo(EventLog.eventCode, eventCode);
        return this;
    }

    public EventLogQueryBuilder eventCodes(int... eventCodes) {
        query.whereContainedIn(EventLog.eventCode, Arrays.asList(eventCodes));
        return this;
    }

    public EventLogQueryBuilder matchingEvents(List<String> events) {
        if (events != null && !events.isEmpty())
            query.whereContainedIn(EventLog.event, events);
        return this;
    }

    public EventLogQueryBuilder matchingEventTypes(String... eventTypes) {
        List<String> events = Lists.newArrayList(eventTypes);
        if (!events.isEmpty())
            query.whereContainedIn(EventLog.eventType, events);
        return this;
    }

    public EventLogQueryBuilder matchingEvent(String event) {
        if (event != null && !event.isEmpty())
            query.whereEqualTo(EventLog.event, event);
        return this;
    }

    public EventLogQueryBuilder matchingEventCode(int eventCode) {
        query.whereEqualTo(EventLog.eventCode, eventCode);
        return this;
    }

    /*
     * Exclude checkpoint and other automatic events
     */
    public EventLogQueryBuilder excludeAutomatic() {

        query.whereNotEqualTo(EventLog.automatic, true);

        return this;
    }

    /*
     * Exclude onActionArrive, onActionAbort, onActionFinish events
     */
    public EventLogQueryBuilder whereIsReportEntry() {


        List<Integer> arrived = Arrays.asList(EventLog.EventCodes.REGULAR_ARRIVED, EventLog.EventCodes.ALARM_ARRIVED, EventLog.EventCodes.RAID_ARRIVED, EventLog.EventCodes.STATIC_ARRIVED);
        List<Integer> written = Arrays.asList(EventLog.EventCodes.STATIC_OTHER, EventLog.EventCodes.ALARM_OTHER, EventLog.EventCodes.REGULAR_OTHER, EventLog.EventCodes.RAID_OTHER, EventLog.EventCodes.REGULAR_EXTRA_TIME);

        List<Integer> reportEntryCodes = Lists.newArrayList();
        reportEntryCodes.addAll(arrived);
        reportEntryCodes.addAll(written);

        query.whereContainedIn(eventCode, reportEntryCodes);

        return this;
    }

    public EventLogQueryBuilder orderByDescendingTimestamp() {
        query.orderByDescending(EventLog.deviceTimestamp);
        return this;
    }

    public EventLogQueryBuilder orderByAscendingTimestamp() {
        query.orderByAscending(EventLog.deviceTimestamp);
        return this;
    }

}
