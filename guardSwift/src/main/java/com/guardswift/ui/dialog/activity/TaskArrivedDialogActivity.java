//package com.guardswift.ui.activity.dialog;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.google.common.collect.Lists;
//import com.guardswift.R;
//import com.guardswift.core.tasks.controller.TaskController;
//import com.guardswift.core.ca.GeofencingModule;
//import com.guardswift.persistence.parse.planning.regular.CircuitUnit;
//import com.guardswift.persistence.parse.planning.GSTask;
//import com.guardswift.util.Analytics;
//import com.guardswift.util.Notifications;
//import com.guardswift.util.Sounds;
//
//import java.util.List;
//import java.util.Set;
//
////import com.guardswift.modules.LocationsModule;
//
//public class TaskArrivedDialogActivity extends AbstractDialogActivity {
//
//	protected static final String TAG = TaskArrivedDialogActivity.class
//			.getSimpleName();
//
//    private static final int DISMISS_TIMEOUT = 10;
//
//
//    public static void show(Context context, GSTask task) {
//
//        if (task instanceof CircuitUnit) {
//            if (CircuitUnit.Recent.getArrived() != null) {
//                Log.e(TAG, "Already arrived at a circuitUnit");
//                return;
//            }
//        }
//
//        Set<GSTask> tasks =  GeofencingModule.Recent.getWithinGeofenceScheduledNow();
//        if (!tasks.isEmpty()) {
//
//            // TODO other than the default sound
//            // Should only ask if multiple tasks are available
//            Sounds.getInstance(context).playNotification(R.raw.arrived);
//
//            context.startActivity(new Intent(context, TaskArrivedDialogActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }
//    }
//
//	@Override
//	protected void onCreate(Bundle arg0) {
//		super.onCreate(arg0);
//
//        Set<GSTask> tasks =  GeofencingModule.Recent.getWithinGeofenceScheduledNow();
//
//        if (tasks.size() == 1) {
//            GSTask task = tasks.iterator().next();
//
//            showSingleOptionDialog(task);
//
//            String clickToOpenDetails = getString(R.string.click_to_open_task);
//            Notifications.displayTaskNotification(this, task,
//                    clickToOpenDetails
//            );
//        }
//        else if (tasks.size() > 1) {
//            showMultipleOptionsDialog(tasks);
//        } else {
//            onActionFinish();
//        }
//
////        if (!isFinishing()) {
////            new Handler().postDelayed(new Runnable() {
////                @Override
////                public void run() {
////                    if (!isFinishing())
////                        onActionFinish();
////                }
////            }, DISMISS_TIMEOUT * 1000);
////        }
//	}
//
//
//
//    private void showSingleOptionDialog(final GSTask task) {
//        String content = getString(R.string.question_arrived_at_task, task.getTaskTitle(this));
//        showDialog(new MaterialDialog.Builder(this)
//                .title(R.string.action_set_arrived)
//                .content(appendAutoDismissMsg(content))
//                .positiveText(R.string.correct)
//                .negativeText(R.string.no)
//                .dismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        onActionFinish();
//                    }
//                })
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        task.getController(TaskArrivedDialogActivity.this).performAction(TaskController.ACTION.ARRIVE, task, false);
//                        Analytics.eventTaskAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Correct);
//                    }
//
//                    @Override
//                    public void onNegative(MaterialDialog dialog) {
//                        Analytics.eventTaskAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Incorrect);
//                    }
//                }).build());
//    }
//
//
//    private void showMultipleOptionsDialog(Set<GSTask> tasks) {
//        final List<GSTask> tasklist = Lists.newArrayList(tasks);
//        String[] items = new String[tasks.size()];
//        int index = 0;
//        for (GSTask task: tasks) {
//            items[index] = task.getTaskTitle(this);
//            index++;
//        }
//        String content = getString(R.string.question_arrived_at_task_multiple);
//        showDialog(new MaterialDialog.Builder(this)
//                .title(R.string.action_set_arrived)
//                .content(appendAutoDismissMsg(content))
//                .items(items)
//                .negativeText(R.string.no)
//                .dismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        onActionFinish();
//                    }
//                })
//                .itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        GSTask task = tasklist.get(which);
//                        task.getController(TaskArrivedDialogActivity.this).performAction(TaskController.ACTION.ARRIVE, task, false);
//
//                        Analytics.eventTaskAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Correct);
//                    }
//                })
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onNegative(MaterialDialog dialog) {
//                        Analytics.eventTaskAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Incorrect);
//                    }
//                })
//                .build());
//    }
//
//    private String appendAutoDismissMsg(String str) {
//        return str;// + "\n\n" + getString(R.string.autodismiss_after_x_seconds, DISMISS_TIMEOUT);
//    }
//
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//    }
//
//    @Override
//	protected void onResume() {
//		super.onResume();
//	}
//
//
//
//
//
//}
