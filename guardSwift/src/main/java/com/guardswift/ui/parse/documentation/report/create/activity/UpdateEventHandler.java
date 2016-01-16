package com.guardswift.ui.parse.documentation.report.create.activity;

/**
 * Created by cyrix on 12/20/15.
 */
public interface UpdateEventHandler extends AddEventHandler {
    // used when updating an event entry
    int REQUEST_EVENT_TYPE = 0;
    int REQUEST_EVENT_PEOPLE = 1;
    int REQUEST_EVENT_LOCATIONS = 2;
    int REQUEST_EVENT_REMARKS = 3;
}
