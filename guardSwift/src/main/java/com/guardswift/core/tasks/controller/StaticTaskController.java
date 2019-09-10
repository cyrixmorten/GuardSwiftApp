package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;


public class StaticTaskController extends BaseTaskController {


    private static final String TAG = StaticTaskController.class.getSimpleName();

    private static StaticTaskController instance;
    public static StaticTaskController getInstance() {
        if (instance == null) {
            instance = new StaticTaskController();
        }

        return instance;
    }

    private StaticTaskController() {}

    public ParseTask applyAction(ACTION action, ParseTask task, boolean automatic) {

        Context ctx = GuardSwiftApplication.getInstance();
        GuardCache guardCache =  GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();


        final Guard guard = guardCache.getLoggedIn();

        EventLog.Builder event = null;

        switch (action) {

            // Triggered when a static task is created
            case ARRIVE:

//                new EventLog().updateDatastore(staticTask);

                task.setStartedBy(guard);

                // Guard started static task
                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.BEGIN)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.STATIC_ARRIVED);

                break;

            case FINISH:

                task.setFinished();

                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.STATIC_FINISHED);


                break;

            case OPEN_WRITE_REPORT:
                ParseTaskCreateReportActivity.start(ctx, task);
                break;

            default:
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return task;


        }


        task.saveEventuallyAndNotify();

        if (event != null) {
            event.saveAsync();
        }


        return task;

    }



}
