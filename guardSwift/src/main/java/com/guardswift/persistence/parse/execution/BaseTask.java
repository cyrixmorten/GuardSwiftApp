package com.guardswift.persistence.parse.execution;

import com.guardswift.persistence.parse.ExtendedParseObject;

/**
 * Created by cyrix on 11/14/15.
 */
public abstract class BaseTask extends ExtendedParseObject implements GSTask {
    @Override
    public String getGeofenceId() {
        return getParseClassName() + "," + getObjectId();
    }


    //    @SuppressWarnings("unchecked")
//    public TaskQueryBuilder<BaseTask> getTaskQueryBuilder(boolean fromLocalDatastore) {
//        return getQueryBuilder(fromLocalDatastore);
//    };
}
