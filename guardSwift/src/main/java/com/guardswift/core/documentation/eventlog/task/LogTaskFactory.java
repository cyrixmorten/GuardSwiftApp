package com.guardswift.core.documentation.eventlog.task;

import java.util.List;

/**
 * Created by cyrix on 6/8/15.
 */
public interface LogTaskFactory {

    List<LogTaskStrategy> getStrategies();
}
