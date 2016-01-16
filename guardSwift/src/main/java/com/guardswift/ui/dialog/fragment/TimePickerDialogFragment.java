//package com.guardswift.ui.fragments.dialog;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.res.Resources;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.text.format.DateFormat;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.TimePicker;
//import android.widget.TimePicker.OnTimeChangedListener;
//
//import com.guardswift.R;
//
//public class TimePickerDialogFragment extends DialogFragment {
//	public static final String HAS_CANCEL_BUTTON = "has_cancel_button";
//	public static final String TITLE_STRING = "title_string";
//	public static final String TITLE_ID = "title_id";
//	public static final String TEXT_ID = "text_id";
//	public static final String LOGGED_IN = "selected";
//
//	public interface TimePickerDialogCallback {
//		public void timePicked(int hourOfDay, int minute);
//	}
//
//	public static TimePickerDialogFragment newInstance(
//			TimePickerDialogCallback callback) {
//		TimePickerDialogFragment fragment = new TimePickerDialogFragment();
//		fragment.setCallback(callback);
//		return fragment;
//	}
//
//	private TimePickerDialogCallback callback;
//	private final CharSequence[] cs = null;
//
//	private int mHourOfDay;
//	private int mMinute;
//
//	private void setCallback(TimePickerDialogCallback callback) {
//		this.callback = callback;
//	}
//
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//		Resources res = getActivity().getResources();
//		Bundle bundle = getArguments();
//
//		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
//
//		View view = getActivity().getLayoutInflater().inflate(
//				R.layout.dialog_timepicker, null);
//		dialog.setView(view);
//
//		String title = "";
//		if (bundle.containsKey(TITLE_STRING)) {
//			title = bundle.getString(TITLE_STRING);
//		}
//		if (bundle.containsKey(TITLE_ID)) {
//			title = getString(bundle.getInt(TITLE_ID));
//		}
//		dialog.setTitle(title);
//
//		TimePicker picker = (TimePicker) view.findViewById(R.id.timePicker);
//		picker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
//		picker.setOnTimeChangedListener(new OnTimeChangedListener() {
//
//			@Override
//			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//				mHourOfDay = hourOfDay;
//				mMinute = minute;
//			}
//		});
//
//		mHourOfDay = picker.getCurrentHour();
//		mMinute = picker.getCurrentMinute();
//
//		TextView text = (TextView) view.findViewById(R.id.text);
//		text.setVisibility(View.GONE);
//		if (bundle.containsKey(TEXT_ID)) {
//			text.setVisibility(View.VISIBLE);
//			text.setText(bundle.getInt(TEXT_ID));
//		}
//
//		dialog.setPositiveButton(android.R.string.ok,
//				new PositiveButtonClickListener());
//
//		return dialog.create();
//	}
//
//	class PositiveButtonClickListener implements
//			DialogInterface.OnClickListener {
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//
//			if (callback != null)
//				callback.timePicked(mHourOfDay, mMinute);
//			dialog.dismiss();
//		}
//	}
//
//	@Override
//	public void onAttach(android.app.Activity activity) {
//		if (activity instanceof TimePickerDialogCallback) {
//			callback = (TimePickerDialogCallback) activity;
//		} else {
//			if (getParentFragment() instanceof TimePickerDialogCallback) {
//				callback = (TimePickerDialogCallback) getParentFragment();
//			} else {
//				// throw new IllegalStateException(
//				// "Parent must implement SingleChoiceDialogCallback");
//			}
//		}
//		super.onAttach(activity);
//	};
//}
