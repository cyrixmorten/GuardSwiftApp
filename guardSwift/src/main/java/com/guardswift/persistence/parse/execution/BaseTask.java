package com.guardswift.persistence.parse.execution;

import com.guardswift.persistence.parse.ExtendedParseObject;

public abstract class BaseTask extends ExtendedParseObject implements GSTask {

    @Override
    public String getGeofenceId() {
        return getParseClassName() + "," + getObjectId();
    }

}
