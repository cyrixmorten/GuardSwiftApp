//package com.guardswift.ui.dialog.fragment;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.os.Bundle;
//import android.text.format.DateFormat;
//import android.view.ContextThemeWrapper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.TextView;
//
//import com.afollestad.materialdialogs.AlertDialogWrapper;
//import com.guardswift.R;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.dagger.InjectingDialogFragment;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.execution.task.alarm.Alarm;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class AlarmDialogFragment extends InjectingDialogFragment {
//
//	protected static final String TAG = AlarmDialogFragment.class
//			.getSimpleName();
//
//
//    public interface AlarmDialogCallback {
//
//        public void ok();
//		public void accept();
//		public void open();
//		public void ignore();
//
//	}
//
//	private AlarmDialogCallback responseCallback;
//	private Alarm mAlarm;
//    private ParseModule.DistanceStrings distanceStrings;
//
//    private int quedAlarms;
//
//    private static final String ARG_GUARD_LOGGED_IN = "guardLoggedIn";
//
//	public static AlarmDialogFragment newInstance(boolean guardLoggedIn,
//                                                  Alarm alarm, int qued,
//                                                  ParseModule.DistanceStrings distanceStrings, AlarmDialogCallback responseCallback) {
//
//
//        AlarmDialogFragment frag = new AlarmDialogFragment();
//		Bundle bundle = new Bundle();
//
//        bundle.putBoolean(ARG_GUARD_LOGGED_IN, guardLoggedIn);
//		frag.setCancelable(false);
//		frag.setArguments(bundle);
//
//		frag.mAlarm = alarm;
//        frag.quedAlarms = qued;
//        frag.distanceStrings = distanceStrings;
//		frag.setResponseCallback(responseCallback);
//
//		return frag;
//	}
//
//	public void setResponseCallback(AlarmDialogCallback responseCallback) {
//		this.responseCallback = responseCallback;
//	}
//
//	public AlarmDialogFragment() {
//		// Empty constructor required for DialogFragment
//	}
//
//    @Bind(R.id.tv_queMsg) TextView queMsg;
//	@Bind(R.id.alarmType) TextView alarmType;
//	@Bind(R.id.securityLevel) TextView securityLevel;
//    @Bind(R.id.zone) TextView zone;
//	@Bind(R.id.clientAddress) TextView clientAddress;
//	@Bind(R.id.clientAddress2) TextView clientAddress2;
//	@Bind(R.id.clientName) TextView clientName;
//	@Bind(R.id.alarmDate) TextView alarmDate;
//	@Bind(R.id.alarmTime) TextView alarmTime;
//	@Bind(R.id.distanceValue) TextView distanceValue;
//	@Bind(R.id.distanceType) TextView distanceType;
//
//    @Bind(R.id.tv_not_logged_in) TextView notLoggedInMsg;
//
////	@Bind(R.id.sharedText) BootstrapButton sharedText;
////	@Bind(R.id.button_mark_accepted) BootstrapButton btnAccept;
////	@Bind(R.id.button_mark_arrived) BootstrapButton btnArrived;
////	@Bind(R.id.button_mark_finished) BootstrapButton btnFinished;
////	@Bind(R.id.button_cancel_arrived) BootstrapButton btnAbort;
////	@Bind(R.id.button_add_event) BootstrapButton btnAddEvent;
//
//	private ContextThemeWrapper themedContext;
//
//	// private Alarm alarm;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		// alarm = EventBus.getDefault().getStickyEvent(Alarm.class);
//		// if (alarm == null) {
//		// throw new IllegalArgumentException("Missing Alarm on EventBus");
//		// }
//		super.onCreate(savedInstanceState);
//	}
//
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//
//		View v = inflater.inflate(R.layout.view_adapter_item_alarm, null);
//
//		ButterKnife.bind(this, v);
//
//        Bundle bundle = getArguments();
//
//        queMsg.setVisibility(View.GONE);
//        if (quedAlarms > 0) {
//            queMsg.setVisibility(View.VISIBLE);
//            queMsg.setText(getString(R.string.alarm_que_msg, quedAlarms));
//        }
//
//
//		fillData();
//		hideButtons();
//
//		themedContext = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
//
//		AlertDialogWrapper.Builder alertBuilder = new AlertDialogWrapper.Builder(
//				themedContext)
//				.setView(v)
//				.setTitle(R.string.new_alarm);
//
//
//        if (bundle.getBoolean(ARG_GUARD_LOGGED_IN)) {
//            // guard logged in
//
//            alertBuilder.setPositiveButton(R.string.accept,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog,
//                                            int which) {
//                            responseCallback.accept();
//                        }
//                    });
//
//            alertBuilder.setNeutralButton(R.string.open, new OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface arg0, int arg1) {
//                    responseCallback.open();
//                }
//            });
//            alertBuilder.setNegativeButton(R.string.ignore, new OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    responseCallback.ignore();
//                }
//            });
//        } else {
//            // no guard logged in
//
//            notLoggedInMsg.setVisibility(View.VISIBLE);
//
//            alertBuilder.setPositiveButton(android.R.string.ok,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog,
//                                            int which) {
//                            responseCallback.ok();
//                        }
//                    });
//        }
//
//		Dialog dialog = alertBuilder.create();
//		setCancelable(false);
//		dialog.setCanceledOnTouchOutside(false);
//
//		return dialog;
//	}
//
//	private void hideButtons() {
////		hideView(sharedText);
////		hideView(btnAccept);
////		hideView(btnArrived);
////		hideView(btnFinished);
////		hideView(btnAbort);
////		hideView(btnAddEvent);
//	}
//
//	private void hideView(View v) {
//		v.setVisibility(View.GONE);
//	}
//
//	private void fillData() {
//
//        Client client = mAlarm.getClient();
//
//        String clientAddressString = client.getCityName() + " " + client.getAddressName()
//                + " " + client.getAddressNumber();
//
//		securityLevel.setText(getString(R.string.security_level_val, mAlarm.getSecurityLevelString()));
//        zone.setText(mAlarm.getZone());
//
//		clientAddress.setText(clientAddressString);
//		clientName.setText(client.getName());
//
//		String clientAddress2String = client.getAddressName2();
//		if (!clientAddress2String.isEmpty()) {
//			clientAddress2.setVisibility(View.VISIBLE);
//			clientAddress2.setText(clientAddress2String);
//		} else {
//			clientAddress2.setVisibility(View.GONE);
//		}
//
//		alarmType.setText(mAlarm.getType());
//
//		alarmDate.setText(DateFormat.getDateFormat(getActivity()).format(mAlarm.getAlarmTime()));
//		alarmTime.setText(DateFormat.getTimeFormat(getActivity()).format(mAlarm.getAlarmTime()));
//
//		distanceType.setText(distanceStrings.distanceType);
//		distanceValue.setText(distanceStrings.distanceValue);
//	}
//
//
//	@Override
//	public void onDestroyView() {
//		ButterKnife.unbind(this);
//		super.onDestroyView();
//	}
//
//}
