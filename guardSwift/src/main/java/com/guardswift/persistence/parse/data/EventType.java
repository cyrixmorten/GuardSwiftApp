package com.guardswift.persistence.parse.data;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.client.Client;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

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
		return new QueryBuilder(false).build();
	}

	@Override
	public void updateFromJSON(final Context context,
			final JSONObject jsonObject) {
		// TODO Auto-generated method stub
	}

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

	public static class QueryBuilder extends ParseQueryBuilder<EventType> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(EventType.class));
		}

		@Override
		public ParseQuery<EventType> build() {
			query.setLimit(1000);
			return super.build();
		}

		@SuppressWarnings("unchecked")
		public QueryBuilder matchingIncludes(Client client) {

			ParseQuery<EventType> queryClientEventType = ParseQuery
					.getQuery(EventType.class);
			queryClientEventType.whereEqualTo(EventType.client, client);

			ParseQuery<EventType> queryEventType = ParseQuery
					.getQuery(EventType.class);
			queryEventType.whereDoesNotExist(EventType.client);

			appendQueries(queryClientEventType, queryEventType);

			return this;
		}

		public QueryBuilder sortByTimesUsed() {
			query.addDescendingOrder(timesUsed);
			return this;
		}

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
