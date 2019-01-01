package com.guardswift.persistence.parse.execution.task;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.query.TaskGroupQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.time.DayOfWeek;
import java.util.Date;

@ParseClassName("TaskGroup")
public class TaskGroup extends ExtendedParseObject {


	public static final String name = "name";
	public static final String createdDay = "createdDay";
	public static final String timeResetDate = "timeResetDate";


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

	private Date getTimeResetDate() {
		return getDate(TaskGroup.timeResetDate);
	}

	private int getCreatedDay() {
		int dayOfWeek =  getInt(TaskGroup.createdDay);

		// in javascript 0 is sunday
		// in java 7 is sunday
		if (dayOfWeek == 0) {
			return 7;
		}

		return dayOfWeek;
	}

	public Date getAdjustedResetDate() {
		DateTime hourAndMinuteReset = new DateTime(getTimeResetDate());

		return DateTime.now()
				.withDayOfWeek(getCreatedDay())
				.withHourOfDay(hourAndMinuteReset.getHourOfDay())
				.withMinuteOfHour(hourAndMinuteReset.getMinuteOfHour()).toDate();
	}

}
