package com.guardswift.core.tasks.controller;

import android.content.Context;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.ca.fingerprinting.WiFiPositioningService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.guardswift.ui.dialog.activity.CheckpointsDialogActivity;
import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Created by cyrix on 2/26/15.
 */
public class CircuitUnitController extends BaseTaskController<CircuitUnit> {


    private static final String TAG = CircuitUnitController.class.getSimpleName();

    private final Context ctx;
    private final GuardCache guardCache;
    private final GSTasksCache tasksCache;


    public CircuitUnitController() {
        this.ctx = GuardSwiftApplication.getInstance();
        this.guardCache = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();
        this.tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();
    }


    public CircuitUnit performAction(ACTION action, final CircuitUnit circuitUnit, final boolean automatic) {

        Log.e(TAG, "invoking action: " + action.toString());

        if (!canPerformAction(action, circuitUnit)) {
            Log.e(TAG, "unable to apply action to task " + action);
            return circuitUnit;
        }


        final Guard guard = guardCache.getLoggedIn();

        EventLog.Builder event = null;

        final String clientName = circuitUnit.getClient().getName();
        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, circuitUnit);
                break;
            case ARRIVE:

                // Optimistically fill datastore with relevant EventLog entries
                new EventLog().updateDatastore(circuitUnit);


//                if (circuitUnit.minutesSinceLastArrival() <= 15) {
//                    Log.w(TAG, "Not been 15 minutes since last arrival - no eventlog generated");
                event = new EventLog.Builder(ctx)
                        .taskPointer(circuitUnit, GSTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.CIRCUITUNIT_ARRIVED);
//                }


                circuitUnit.setArrived(guard);
                tasksCache.addArrived(circuitUnit);

                if (circuitUnit.hasCheckPoints()) {
                    if (!circuitUnit.isAborted()) {
                        // otherwise resume where we left off
                        circuitUnit.clearCheckpoints();
                    }
                    WiFiPositioningService.start(ctx);
                }


                break;
            case ABORT:

                event = new EventLog.Builder(ctx)
                        .taskPointer(circuitUnit, GSTask.EVENT_TYPE.ABORT)
                        .event(ctx.getString(R.string.event_aborted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.CIRCUITUNIT_ABORT);

                WiFiPositioningService.stop(ctx);

                circuitUnit.setAborted();
                tasksCache.removeArrived(circuitUnit);


                break;

            case FINISH:


                event = new EventLog.Builder(ctx)
                        .taskPointer(circuitUnit, GSTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.CIRCUITUNIT_FINISHED);

                WiFiPositioningService.stop(ctx);

                circuitUnit.setFinished(guard);
                tasksCache.removeArrived(circuitUnit);

                break;

            case RESET:
                circuitUnit.reset();

                break;
            case OPEN_ADD_EVENT:
                GSTaskCreateReportActivity.start(ctx, circuitUnit);
                break;

            case OPEN_CHECKPOINTS:
                CheckpointsDialogActivity.start(ctx, circuitUnit);
                break;

        }


        circuitUnit.pinThenSaveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                EventBusController.postUIUpdate(circuitUnit);
            }
        });

        if (event != null) {
            event.saveAsync();
        }

        return circuitUnit;

    }

}
