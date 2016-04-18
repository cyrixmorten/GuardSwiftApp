package com.guardswift.persistence.parse;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ParseQueryBuilder<T extends ParseObject> {

	private static final String TAG = ParseQueryBuilder.class.getSimpleName();

	protected final String pin;
	protected final boolean fromLocalDatastore;
	protected ParseQuery<T> query;

	public ParseQueryBuilder(String pin, boolean fromLocalDatastore,
			ParseQuery<T> query) {
		this.pin = pin;
		this.fromLocalDatastore = fromLocalDatastore;
		this.query = query;
	}


    public ParseQueryBuilder<T> matchingObjectId(String objectId) {
        query.whereEqualTo("objectId", objectId);
        return this;
	}

    public ParseQueryBuilder<T> matchingObjectIds(String... objectIds) {
		if (objectIds.length > 0)
        	query.whereContainedIn("objectId", Arrays.asList(objectIds));
        return this;
    }

    public ParseQueryBuilder<T> fromLocalDatastore() {
        query.fromLocalDatastore();
        query.fromPin(pin);
        return this;
    }

	@SafeVarargs
	protected final void appendQueries(ParseQuery<T>... appendingQueries) {

		List<ParseQuery<T>> queries = new ArrayList<ParseQuery<T>>(
				Arrays.asList(appendingQueries));
//		queries.addUnique(query);
		query = ParseQuery.or(queries);
	}

	public ParseQuery<T> build() {
		query.whereEqualTo("owner", ParseUser.getCurrentUser());
		query.whereDoesNotExist("archive");
		setFromLocalDatastore();
		return query;
	};

    public ParseQuery<ParseObject> buildAsParseObject() {
		return (ParseQuery<ParseObject>)build();
    };


	private void setFromLocalDatastore() {
		if (fromLocalDatastore) {
            query.fromLocalDatastore();
            query.fromPin(pin);
		}
	}



}
