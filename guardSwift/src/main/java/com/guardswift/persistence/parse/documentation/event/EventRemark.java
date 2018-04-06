package com.guardswift.persistence.parse.documentation.event;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.query.EventRemarkQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

@ParseClassName("EventRemark")
public class EventRemark extends ExtendedParseObject {


	public static final String objectName = "Event";
	public static final String eventType = "eventType";
	public static final String client = "client";
    public static final String guard = "guard";
	public static final String location = "location";
	public static final String remark = "remark";

	public static final String timesUsed = "timesUsed";

    public static EventRemark create(EventType eventType, String location, String remark, Client client, Guard guard) {
        EventRemark eventRemark = new EventRemark();
		eventRemark.setEventType(eventType);
		eventRemark.setLocation(location);
		eventRemark.setRemark(remark);
		eventRemark.setClient(client);
		eventRemark.setGuard(guard);
        eventRemark.setDefaultOwner();
        return eventRemark;
    }


    @SuppressWarnings("unchecked")
	@Override
	public ParseQuery<EventRemark> getAllNetworkQuery() {
		return new EventRemarkQueryBuilder(false).build();
	}


	public static EventRemarkQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
		return new EventRemarkQueryBuilder(fromLocalDatastore);
	}

	public void setGuard(Guard guard) {
		put(EventRemark.guard, guard);
	}

	public void setEventType(EventType eventType) {
		put(EventRemark.eventType, eventType);
	}

	public void setClient(Client client) {
		put(EventRemark.client, client);
	}

	public void setLocation(String location) {
		put(EventRemark.location, location);
	}

	public void setRemark(String remark) {
		put(EventRemark.remark, remark);
	}

	public String getLocation() {
		return getString(location);
	}

	public String getRemark() {
		return getString(remark);
	}

//	public void setOwner(ParseObject owner) {
//		put(EventRemark.owner, owner);
//	}
//
//	public ParseObject getOwner() {
//		return getParseObject(owner);
//	}

}
