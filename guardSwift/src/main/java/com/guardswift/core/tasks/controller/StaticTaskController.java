package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;


public class StaticTaskController extends BaseTaskController {


    private static final String TAG = StaticTaskController.class.getSimpleName();

    private final Context ctx;

    private final GuardCache guardCache;


    public StaticTaskController()
    {
        this.ctx = GuardSwiftApplication.getInstance();
        this.guardCache =  GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();
    }

    public GSTask performAction(ACTION action, GSTask task, boolean automatic) {

        StaticTask staticTask = (StaticTask)task;

        final Guard guard = guardCache.getLoggedIn();

        EventLog.Builder event = null;

        switch (action) {

            // Triggered when a static task is created
            case ARRIVE:

//                new EventLog().updateDatastore(staticTask);

                staticTask.setStartedBy(guard);

                // Guard started static task
                event = new EventLog.Builder(ctx)
                        .taskPointer(staticTask, GSTask.EVENT_TYPE.BEGIN)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.STATIC_ARRIVED);

                break;

            case FINISH:

                staticTask.setFinished();

                event = new EventLog.Builder(ctx)
                        .taskPointer(staticTask, GSTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.STATIC_FINISHED);


                break;

            case OPEN_WRITE_REPORT:
                GSTaskCreateReportActivity.start(ctx, staticTask);
                break;

            default:
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return staticTask;


        }


        staticTask.pinThenSaveEventually();

        if (event != null) {
            event.saveAsync();
        }


        return staticTask;

    }



}
