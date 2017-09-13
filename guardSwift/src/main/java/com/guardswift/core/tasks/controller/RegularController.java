package com.guardswift.core.tasks.controller;

import android.content.Context;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.ParseTaskCreateReportActivity;
import com.guardswift.ui.dialog.activity.CheckpointsDialogActivity;
import com.parse.ParseException;
import com.parse.SaveCallback;

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

        Context ctx = GuardSwiftApplication.getInstance();
        ParseTasksCache tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();

        Log.e(TAG, "invoking action: " + action.toString());


        if (!canPerformAction(action, task)) {
            Log.e(TAG, "unable to apply action to task " + action);
            return task;
        }

        EventLog.Builder event = null;

        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, circuitUnit);
                break;
            case ARRIVE:

                // Optimistically fill datastore with relevant EventLog entries
//                new EventLog().updateDatastore(circuitUnit);


//                if (circuitUnit.minutesSinceLastArrival() <= 15) {
//                    Log.w(TAG, "Not been 15 minutes since last arrival - no eventlog generated");
                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_ARRIVED);
//                }


                task.setArrived();
                tasksCache.addArrived(task);

//                if (circuitUnit.hasCheckPoints()) {
//                    if (!circuitUnit.isAborted()) {
//                        // otherwise resume where we left off
//                        circuitUnit.clearCheckpoints();
//                    }
//                    WiFiPositioningService.start(ctx);
//                }


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

            case RESET:

                event = new EventLog.Builder(ctx)
                        .taskPointer(task, ParseTask.EVENT_TYPE.LEAVE)
                        .event(ctx.getString(R.string.event_left))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.REGULAR_LEFT);

                task.reset();
                tasksCache.removeArrived(task);
                break;
            case OPEN_WRITE_REPORT:
                ParseTaskCreateReportActivity.start(ctx, task);
                break;

            case OPEN_CHECKPOINTS:
                CheckpointsDialogActivity.start(ctx, task);
                break;

            default:
                Log.e(TAG, "DEFAULT");
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return task;

        }


        if (event != null) {

            task.pinThenSaveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        new HandleException(TAG, "Failed to pinThenSaveEventually", e);
                    }

                    EventBusController.postUIUpdate(task);
                }
            });

            event.saveAsync();
        }

        return task;

    }

}
