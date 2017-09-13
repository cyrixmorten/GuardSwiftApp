package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;

public interface LogTaskStrategy {

    void log(ParseTask task, ParseObject toParseObject);

}
