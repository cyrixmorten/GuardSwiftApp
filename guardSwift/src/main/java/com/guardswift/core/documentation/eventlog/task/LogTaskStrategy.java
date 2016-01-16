package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/7/15.
 */
public interface LogTaskStrategy {

    void log(GSTask task, ParseObject toParseObject);

}
