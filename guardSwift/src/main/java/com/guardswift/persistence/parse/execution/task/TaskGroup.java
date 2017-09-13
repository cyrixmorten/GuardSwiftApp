package com.guardswift.persistence.parse.execution.task;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.query.TaskGroupQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("TaskGroup")
public class TaskGroup extends ExtendedParseObject {


	public static final String name = "name";

	@Override
	public String getParseClassName() {
		return TaskGroup.class.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseQuery<TaskGroup> getAllNetworkQuery() {
		return new TaskGroupQueryBuilder(false).build();
	}

	public static TaskGroupQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
		return new TaskGroupQueryBuilder(fromLocalDatastore);
	}

	public String getName() {
		return getString(name);
	}


	public ParseObject getOwner() {
		return getParseObject(owner);
	}

}
