package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;
import com.guardswift.persistence.parse.execution.alarm.Alarm;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class AlarmCache extends BaseTaskCache<Alarm> {

    private boolean dialogShowing;

    @Inject
    AlarmCache(@ForApplication  Context context) {
        super(Alarm.class, context);
    }

    @Override
    public Alarm getConcreteTask() {
        return new Alarm();
    }


    public boolean isDialogShowing() {
        return dialogShowing;
    }

    public void setDialogShowing(boolean dialogShowing) {
        this.dialogShowing = dialogShowing;
    }
}
