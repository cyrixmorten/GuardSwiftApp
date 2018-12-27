package com.guardswift.core.tasks.controller;

import android.content.Context;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.fabric.TrackEvent;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;

public class RegularController extends BaseTaskController {


    private static final String TAG = RegularController.class.getSimpleName();


    private static RegularController instance;
    public static RegularController getInstance() {
        if (instance == null) {
            instance = new RegularController();
        }

        return instance;
    }


    private RegularController() {}



    public ParseTask performAction(ACTION action, final ParseTask task, final boolean automatic) {

        TrackEvent.taskAction(action, task, automatic);

        Context ctx = GuardSwiftApplication.getInstance();
        ParseTasksCache tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();

        Log.e(TAG, "invoking action: " + action.toString());

        EventLog.Builder event = null;

        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, circuitUnit);
                break;
            case ARRIVE:

                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_ARRIVED);


                if (automatic) {
                    task.setArrived();
                    tasksCache.addArrived(task);
                }
                
                task.incrementArrivedCount();

                break;
            case ABORT:

                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.ABORT)
                        .event(ctx.getString(R.string.event_aborted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_ABORT);


                task.setAborted();
                tasksCache.removeArrived(task);


                break;

            case FINISH:


                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_FINISHED);


                task.setFinished();
                tasksCache.removeArrived(task);

                break;

            case PENDING:

                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.PENDING)
                        .event(ctx.getString(R.string.event_left))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_PENDING);

                task.setPending();
                tasksCache.removeArrived(task);
                break;
            case OPEN_WRITE_REPORT:
                ParseTaskCreateReportActivity.start(ctx, task);
                break;


            default:
                Log.e(TAG, "DEFAULT");
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
