package com.guardswift.persistence.parse.data;

import android.content.Context;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.List;

@ParseClassName("Message")
public class Message extends ExtendedParseObject {

	public static final String PIN = "Message";

	public static final String message = "message";
	public static final String author = "author";
    public static final String readBy = "readBy";

	@Override
	public String getPin() {
		return PIN;
	}

    public static Message create(String message, String author) {
        Message clientInfo = new Message();
        clientInfo.put(Message.message, message);
        clientInfo.put(Message.author, author);
        clientInfo.put(owner, ParseUser.getCurrentUser());
        return clientInfo;

    };

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<Message> getAllNetworkQuery() {
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

	public static class QueryBuilder extends ParseQueryBuilder<Message> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(PIN, fromLocalDatastore, ParseQuery
					.getQuery(Message.class));
		}

	}

    public void markAsRead(Guard guard) {
        add(readBy, guard);
    }

    public boolean isReadBy(Guard guard) {
        List<Guard> readByGuards = getList(readBy);
        return (readByGuards != null) && readByGuards.contains(guard);
    }

	public String getAuthor() {
		if (has(author)) return getString(author);
        return "";
	}

	public String getMessage() {
		if (has(message)) return getString(message);
        return "";
	}

}
