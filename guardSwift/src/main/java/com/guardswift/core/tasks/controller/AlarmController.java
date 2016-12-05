package com.guardswift.core.tasks.controller;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.ui.activity.GSTaskCreateReportActivity;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.util.ToastHelper;

public class AlarmController extends BaseTaskController {


    private static final String TAG = AlarmController.class.getSimpleName();

    private final Context ctx;



    public AlarmController()
    {
        this.ctx = GuardSwiftApplication.getInstance();
    }



//    private void performAction(ACTION action, GSTask alarm, boolean automatic, boolean verify) {
//        performAction(action, alarm, verify, null);
//    }

    public GSTask performAction(ACTION action, GSTask alarm, boolean automatic) {

        final String clientName = alarm.getClient().getName();

        EventLog.Builder event = null;

        switch (action) {
            case OPEN:
//                TaskDetailsActivityFactory.start(ctx, parseObject);
                break;
            case FORWARD:

//                event = new EventLog.Builder(ctx)
//                        .taskPointer(alarm, GSTask.EVENT_TYPE.ACCEPT)
//                        .event(ctx.getString(R.string.event_forwarded))
//                        .remarks(ctx.getString(R.string.alarm_group) + " " + alarm.getForwardedTo())
//                        .automatic(automatic)
//                        .eventCode(EventLog.EventCodes.ALARM_FORWARDED);

                break;
            case ACCEPT:

                alarm.setAccepted();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ACCEPT)
                        .event(ctx.getString(R.string.event_accepted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ACCEPTED);

                break;
            case IGNORE:
                break;
            case ARRIVE:


                alarm.setArrived();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ARRIVE)
                        .event(ctx.getString(R.string.event_arrived))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ARRIVED);

                break;
            case ABORT:

                alarm.setAborted();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.ABORT)
                        .event(ctx.getString(R.string.event_aborted))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_ABORT);


                break;

            case FINISH:

                alarm.setFinished();

                event = new EventLog.Builder(ctx)
                        .taskPointer(alarm, GSTask.EVENT_TYPE.FINISH)
                        .event(ctx.getString(R.string.event_finished))
                        .automatic(automatic)
                        .eventCode(EventLog.EventCodes.ALARM_FINISHED);

                ToastHelper.toast(ctx, ctx.getString(R.string.alarm_finished_at_client, clientName));

                break;

            case OPEN_WRITE_REPORT:
                GSTaskCreateReportActivity.start(ctx, alarm);
                break;

            default:
                new HandleException(TAG, "Missing action", new IllegalArgumentException("Missing action: " + action));
                return alarm;


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


//    private MaterialDialog selectAlarmGroup(final Alarm alarm, final MaterialDialog.ButtonCallback buttonCallback) {
//        List<AlarmGroup> alarmGroups;
//        try {
//            alarmGroups = AlarmGroup.getQueryBuilder(true).sortByName().build().find();
//        } catch (ParseException e) {
//            Crashlytics.logException(e);
//            return null;
//        }
//
//        String[] groupNames = new String[alarmGroups.size()];
//        for (int i = 0; i<alarmGroups.size(); i++) {
//            groupNames[i] = alarmGroups.get(i).getName();
//        }
//        return new MaterialDialog.Builder(ctx)
//                .title(R.string.forward_alarm)
//                .items(groupNames)
//                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
//
//                    @Override
//                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
//                        if (buttonCallback != null) {
//                            buttonCallback.onPositive(materialDialog);
//                        }
//
//                        alarm.setForwardedTo(charSequence);
//
//                        performAction(ACTION.FORWARD, alarm, false);
//
//                        return true;
//                    }
//                })
//                .callback(buttonCallback)
//                .positiveText(android.R.string.ok)
//                .negativeText(android.R.string.cancel)
//                .show();
//
//
//    }

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
