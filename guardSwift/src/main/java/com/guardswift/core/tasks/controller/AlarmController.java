package com.guardswift.core.tasks.controller;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.guardswift.R;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.data.AlarmGroup;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.util.ToastHelper;
import com.parse.ParseException;

import java.util.List;

/**
 * Created by cyrix on 2/26/15.
 */
public class AlarmController extends BaseTaskController<Alarm> {


    private static final String TAG = AlarmController.class.getSimpleName();

    private final Context ctx;

    private final GuardCache guardCache;


    public AlarmController()
    {
        this.ctx = GuardSwiftApplication.getInstance();
        this.guardCache =  GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache();
    }



//    private void performAction(ACTION action, GSTask alarm, boolean automatic, boolean verify) {
//        performAction(action, alarm, verify, null);
//    }

    public Alarm performAction(ACTION action, Alarm alarm, boolean automatic) {

        final Guard guard = guardCache.getLoggedIn();
        final String clientName = alarm.getClient().getName();

        EventLog.Builder event = null;

        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, parseObject);
                break;
            case FORWARD:

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ACCEPT)
                        .event(ctx.getString(R.string.event_forwarded))
                        .remarks(ctx.getString(R.string.alarm_group) + " " + alarm.getForwardedTo())
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_FORWARDED);

                break;
            case ACCEPT:

                alarm.setAccepted(guard);

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ACCEPT)
                        .event(ctx.getString(R.string.event_accepted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ACCEPTED);

                break;
            case IGNORE:
                alarm.setIgnored(guard);
                break;
            case ARRIVE:

                new EventLog().updateDatastore(alarm);

                alarm.setArrived(ctx, guard);

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ARRIVED);

                break;
            case ABORT:

                alarm.setAborted(guard);

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ABORT)
                        .event(ctx.getString(R.string.event_aborted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ABORT);


                ToastHelper.toast(ctx, ctx.getString(R.string.alarm_canceled_at_client, clientName));
                break;

            case FINISH:

                alarm.setFinished(guard);

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_FINISHED);

                ToastHelper.toast(ctx, ctx.getString(R.string.alarm_finished_at_client, clientName));

//                GeofencingModule.Recent.closeTaskResetOthers(alarm);

                break;

            case OPEN_ADD_EVENT:
                GSTaskCreateReportActivity.start(ctx, alarm);
                break;


        }


        alarm.pinThenSaveEventually();

        if (event != null) {
            event.saveAsync();
        }


        return alarm;

    }


//    private boolean verifyAction(final ACTION action, final Alarm alarm, MaterialDialog.ButtonCallback buttonCallback) {
//        if (!(ctx instanceof Activity)) {
//            // unable to show dialog if context is not of type activity
//            return true;
//        }
//
//        if (action == ACTION.FORWARD ) {
//            // distance to client too large
//
//            selectAlarmGroup(alarm, buttonCallback);
//            return false;
//        }
//        if (action == ACTION.ARRIVE && alarm.getClient().hasLargeDistanceToDevice()) {
//            // distance to client too large
//
////            confirmArrival(alarm, buttonCallback);
//            return true;
//        }
//        if (action == ACTION.ABORT) {
//            // always confirm aborts
//
//            confirmAbort(alarm, buttonCallback);
//            return false;
//        }
//
//        return true;
//    }


    private MaterialDialog selectAlarmGroup(final Alarm alarm, final MaterialDialog.ButtonCallback buttonCallback) {
        List<AlarmGroup> alarmGroups;
        try {
            alarmGroups = AlarmGroup.getQueryBuilder(true).sortByName().build().find();
        } catch (ParseException e) {
            Crashlytics.logException(e);
            return null;
        }

        String[] groupNames = new String[alarmGroups.size()];
        for (int i = 0; i<alarmGroups.size(); i++) {
            groupNames[i] = alarmGroups.get(i).getName();
        }
        return new MaterialDialog.Builder(ctx)
                .title(R.string.forward_alarm)
                .items(groupNames)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        if (buttonCallback != null) {
                            buttonCallback.onPositive(materialDialog);
                        }

                        alarm.setForwardedTo(charSequence);

                        performAction(ACTION.FORWARD, alarm, false);

                        return true;
                    }
                })
                .callback(buttonCallback)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();


    }

//    private MaterialDialog confirmArrival(final Alarm alarm, final MaterialDialog.ButtonCallback buttonCallback) {
//        ParseModule.DistanceStrings distanceStrings = ParseModule.getDistanceStrings(alarm.getClient());
//        return new MaterialDialog.Builder(ctx)
//                .title(R.string.mark_arrived)
//                .positiveText(android.R.string.ok)
//                .negativeText(android.R.string.cancel)
//                .content(ctx.getString(R.string.distance_to_client, distanceStrings.distanceValue, distanceStrings.distanceType))
//                .callback(new MaterialDialog.ButtonCallback() {
//                              @Override
//                              public void onPositive(MaterialDialog dialog) {
//                                  Log.d(TAG, "confirmArrival");
//                                  if (buttonCallback != null) {
//                                      buttonCallback.onPositive(dialog);
//                                  }
//                                  performAction(ACTION.ARRIVE, alarm, false);
//                              }
//                          }
//                ).show();
//    }

//    private MaterialDialog confirmAbort(final Alarm alarm, final MaterialDialog.ButtonCallback buttonCallback) {
//
//        return new MaterialDialog.Builder(ctx)
//                .title(R.string.onActionAbort)
//                .positiveText(android.R.string.ok)
//                .negativeText(android.R.string.cancel)
//                .content(ctx.getString(R.string.abort_task_at_client, alarm.getClient().getName()))
//                .callback(new MaterialDialog.ButtonCallback() {
//                              @Override
//                              public void onPositive(MaterialDialog dialog) {
//                                  if (buttonCallback != null) {
//                                      buttonCallback.onPositive(dialog);
//                                  }
//                                  performAction(ACTION.ABORT, alarm, false);
//                              }
//                          }
//                ).show();
//    }





}
