package com.guardswift.ui.parse.documentation.report.create.activity;

import com.guardswift.persistence.parse.data.EventType;

/**
 * Created by cyrix on 12/20/15.
 */
public interface AddEventHandler {
    // used to pass data between activities and fragments
    String EXTRA_EVENT_BUNDLE = "com.guardswift.EXTRA_EVENT_BUNDLE";
    String EXTRA_EVENT_TYPE = "com.guardswift.EXTRA_EVENT_TYPE";
    String EXTRA_AMOUNT = "com.guardswift.EXTRA_AMOUNT";
    String EXTRA_LOCATIONS = "com.guardswift.EXTRA_LOCATIONS";
    String EXTRA_PEOPLE = "com.guardswift.EXTRA_PEOPLE";
    String EXTRA_REMARKS = "com.guardswift.EXTRA_REMARKS";


    void setEventType(EventType eventType);
    String getEventType();
    void setAmount(int amount);
    int getAmount();
    void setPeople(String people);
    String getPeople();
    void setLocations(String clientLocations);
    String getLocations();
    void setRemarks(String remarks);
    String getRemarks();
//    void setPeople(List<Person> people);
//    void setLocations(List<ClientLocation> clientLocations);
}
