package com.guardswift.persistence.parse.data;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.query.EventTypeQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("EventType")
public class EventType extends ExtendedParseObject {

//    public static class Recent {
//        private static EventType selected;
//
//        public static EventType getSelected() {
//            return selected;
//        }
//
//        public static void setSelected(EventType selected) {
//            Recent.selected = selected;
//        }
//
//    }


	public static final String name = "name";
	public static final String client = "client";
	public static final String hasAmount = "hasAmount";
	public static final String hasPeople = "hasPeople";
	public static final String hasLocations = "hasLocations";
	public static final String hasRemarks = "hasRemarks";

	public static final String timesUsed = "timesUsed";

	@Override
	public String getParseClassName() {
		return EventType.class.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<EventType> getAllNetworkQuery() {
		return new EventTypeQueryBuilder(false).build();
	}


    public static EventTypeQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new EventTypeQueryBuilder(fromLocalDatastore);
    }


	public int getTimesUsed() {
		return getInt(timesUsed);
	}

	public void setName(String name) {
		put(EventType.name, name);
	}

	public String getName() {
		return getString(name);
	}

	public void setOwner(ParseObject owner) {
		put(EventType.owner, owner);
	}

	public ParseObject getOwner() {
		return getParseObject(owner);
	}

	public boolean hasAmount() {
		return getBoolean(hasAmount);
	}

	public boolean hasPeople() {
		return getBoolean(hasPeople);
	}

	public boolean hasLocations() {
		return getBoolean(hasLocations);
	}

	public boolean hasRemarks() {
		return getBoolean(hasRemarks);
	}

}
