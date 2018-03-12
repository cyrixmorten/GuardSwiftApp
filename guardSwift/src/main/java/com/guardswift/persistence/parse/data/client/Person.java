package com.guardswift.persistence.parse.data.client;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("Person")
public class Person extends ExtendedParseObject {


	public static final String name = "name";
	public static final String desc = "desc";
	public static final String phoneNumber = "phoneNumber";

	public static Person create(String name) {
		Person person = new Person();
		person.setName(name);
		return person;
	}


	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<Person> getAllNetworkQuery() {
		return new QueryBuilder(false).build();
	}



	public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
		return new QueryBuilder(fromLocalDatastore);
	}

	@Override
	public int compareTo(ExtendedParseObject object) {
		if (getName() == null || object == null) {
			return 0;
		}

		if (object instanceof Person) {
			return getName().compareTo(((Person)object).getName());
		}

		return 0;
	}

	public static class QueryBuilder extends ParseQueryBuilder<Person> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
					.getQuery(Person.class));
		}

	}

	public void setName(String name) {
		put(Person.name, name);
	}

	public String getName() {
        return getString(name);
    }

    public String getDesc() {
        return getString(desc);
    }

    public String getPhoneNumber() {
        return getString(phoneNumber);
    }

}
