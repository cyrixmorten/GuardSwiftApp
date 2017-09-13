//package com.guardswift.ui.activity.dialog;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.crashlytics.android.Crashlytics;
//import com.guardswift.R;
//import com.guardswift.core.ca.FingerprintingModule;
//import com.guardswift.persistence.parse.data.client.ClientLocation;
//import com.guardswift.persistence.parse.documentation.eventlog.EventLog;
//import com.guardswift.persistence.parse.planning.regular.CircuitUnit;
//import com.guardswift.persistence.parse.planning.ParseTask;
//import com.guardswift.util.Analytics;
//import com.guardswift.util.Sounds;
//import com.parse.ParseException;
//import com.parse.SaveCallback;
//
//import java.util.Set;
//
//import javax.inject.Inject;
//
//import dk.alexandra.positioning.wifi.AccessPoint;
//
////import com.guardswift.modules.LocationsModule;
//
//public class NearCheckpointDialogActivity extends AbstractDialogActivity {
//
//	protected static final String TAG = NearCheckpointDialogActivity.class
//			.getSimpleName();
//
//
//    private static ParseTask task;
//
//    public static void show(Context context, ClientLocation checkpoint) {
////        ClientLocation checkpoint = ClientLocation.Recent.getNearCheckpoint();
//
//        // TODO locate correct task from checlpoint
//        task = CircuitUnit.Recent.getArrived();
//        if (checkpoint != null && !FingerprintingModule.Recent.isTraining()) {
//            context.startActivity(new Intent(context, NearCheckpointDialogActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }
//    }
//
//    @Inject FingerprintingModule fingerprintingModule;
//
//	@Override
//	protected void onCreate(Bundle arg0) {
//		super.onCreate(arg0);
//
//        final Set<AccessPoint> lastSample = fingerprintingModule.getLastKnownSample();
//
//        Sounds.getInstance(this).playNotification(R.raw.checkpoint);
//
//        final ClientLocation checkpoint = ClientLocation.Recent.getNearCheckpoint();
//
//        String content = getString(R.string.question_near_checkpoint, checkpoint.getLocations());
//        showDialog(new MaterialDialog.Builder(this)
//                .title(R.string.event_checkpoint)
//                .content(appendAutoDismissMsg(content))
//                .positiveText(R.string.correct)
//                .negativeText(R.string.wrong)
//                .dismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        onActionFinish();
//                    }
//                })
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(final MaterialDialog dialog) {
//                        checkpoint.setChecked(true);
//                        checkpoint.pinInBackground(ClientLocation.PIN, new SaveCallback() {
//                            @Override
//                            public void done(ParseException e) {
//                                if (e != null) {
//                                    Toast.makeText(getApplicationContext(), getString(R.string.error_an_error_occured), Toast.LENGTH_LONG).show();
//                                    Crashlytics.logException(e);
//                                    return;
//                                }
//
//
//                                Analytics.eventCheckpointAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Correct);
//                                ClientLocation.Recent.setBlacklisted(null);
//
//                                new EventLog.Builder(getApplicationContext())
//                                        .taskPointer(task, ParseTask.EVENT_TYPE.CHECKPOINT)
//                                        .checkpoint(checkpoint, false)
//                                        .wifiSample(lastSample)
//                                        .correctGuess(true)
//                                        .saveAsync();
//
//                                dialog.dismiss();
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onNegative(MaterialDialog dialog) {
//                        super.onNegative(dialog);
//
//                        Analytics.eventCheckpointAutomation(Analytics.EventAction.Arrival, Analytics.EventLabelGuess.Incorrect);
//
//                        ClientLocation.Recent.setBlacklisted(checkpoint);
//                        ClientLocation.Recent.resetHits();
//
////                        new EventLog.Builder(getApplicationContext())
////                                .taskPointer(task, ParseTask.EVENT_TYPE.CHECKPOINT)
////                                .checkpoint(checkpoint, true)
////                                .wifiSample(lastSample)
////                                .correctGuess(false)
////                                .saveAsync();
//
//                        dialog.dismiss();
//                    }
//                }).autoDismiss(false).build());
//	}
//
//
//
//    private String appendAutoDismissMsg(String str) {
//        return str;// + "\n\n" + getString(R.string.autodismiss_after_x_seconds, DISMISS_TIMEOUT);
//    }
//
//
//
//
//
//
//
//}
