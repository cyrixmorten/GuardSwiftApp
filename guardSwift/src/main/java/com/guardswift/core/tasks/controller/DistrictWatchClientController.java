package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Created by cyrix on 2/26/15.
 */
public class DistrictWatchClientController extends BaseTaskController<DistrictWatchClient> {


    private static final String TAG = DistrictWatchClientController.class.getSimpleName();

    private final Context ctx;
    private final GSTasksCache tasksCache;


    public DistrictWatchClientController() {
        this.ctx = GuardSwiftApplication.getInstance();
        this.tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();
    }





//    private void performAction(ACTION action, GSTask task, boolean automatic, boolean verify) {
//        performAction(action, task, automatic, false, null);
//    }


    public DistrictWatchClient performAction(ACTION action, final DistrictWatchClient districtWatchClient, final boolean automatic) {


        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, districtWatchClient);
                break;
            case ARRIVE:


                districtWatchClient.setArrived(true);

                new EventLog.Builder(ctx)
                        .taskPointer(districtWatchClient, GSTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .location(districtWatchClient.getFullAddress())
                        .eventCode(EventLog.EventCodes.DISTRICTWATCH_ARRIVED).saveAsync();

                tasksCache.addArrived(districtWatchClient);
                break;
            case RESET:

                tasksCache.removeArrived(districtWatchClient);
                districtWatchClient.reset();

                break;
            case OPEN_WRITE_REPORT:
                GSTaskCreateReportActivity.start(ctx, districtWatchClient);
                break;

            default:
                new HandleException(TAG, "Missing action", new MaterialDialog.NotImplementedException("Missing action: " + action));
                return districtWatchClient;

        }


        districtWatchClient.pinThenSaveEventually((new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (automatic) {
                    EventBusController.postUIUpdate(districtWatchClient);
                }
            }
        }));

        return districtWatchClient;
    }


}
