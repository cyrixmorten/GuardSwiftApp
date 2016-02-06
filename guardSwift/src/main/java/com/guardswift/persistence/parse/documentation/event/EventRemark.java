package com.guardswift.persistence.parse.documentation.event;

import android.content.Context;
import android.util.Log;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

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
        eventRemark.put(EventRemark.eventType, eventType);
        eventRemark.put(EventRemark.location, location);
        eventRemark.put(EventRemark.remark, remark);
        eventRemark.put(EventRemark.client, client);
        eventRemark.put(EventRemark.guard, guard);
        eventRemark.setDefaultOwner();
        return eventRemark;
    };

	@Override
	public String getParseClassName() {
		return EventRemark.class.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<EventRemark> getAllNetworkQuery() {
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

	public static class QueryBuilder extends ParseQueryBuilder<EventRemark> {

		private String TAG = QueryBuilder.this.getClass().getSimpleName();

		public QueryBuilder(boolean fromLocalDatastore) {
			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
					.getQuery(EventRemark.class));
		}

		@Override
		public ParseQuery<EventRemark> build() {
			query.setLimit(1000);
			return super.build();
		}

		public QueryBuilder matching(EventType eventType) {
			if (eventType == null) {
				Log.e(TAG, "Missing eventType");
				return this;
			}
			query.whereEqualTo(EventRemark.eventType, eventType);
			return this;
		}

		public QueryBuilder matching(Client client) {
			query.whereEqualTo(EventRemark.client, client);
			return this;
		}

		public QueryBuilder matching(String location) {
			query.whereEqualTo(EventRemark.location, location);
			return this;
		}

		public QueryBuilder sortByTimesUsed() {
			// query.addDescendingOrder(timesUsed);
			query.addAscendingOrder(remark);
			return this;
		}
	}

//	public void setEventType(EventType eventType) {
//		put(EventRemark.eventType, eventType);
//	}

//	public void setClient(Client client) {
//		put(EventRemark.client, client);
//	}

//	public void setLocation(String location) {
//		put(EventRemark.location, location);
//	}

//	public void setRemark(String remark) {
//		put(EventRemark.remark, remark);
//	}

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
