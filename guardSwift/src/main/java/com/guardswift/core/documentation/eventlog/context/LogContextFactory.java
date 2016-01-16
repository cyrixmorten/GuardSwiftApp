package com.guardswift.core.documentation.eventlog.context;

import java.util.List;

/**
 * Created by cyrix on 6/8/15.
 */
public interface LogContextFactory {

    List<LogContextStrategy> getStrategies();
}
