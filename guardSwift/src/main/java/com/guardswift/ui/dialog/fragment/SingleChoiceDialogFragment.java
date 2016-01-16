//package com.guardswift.ui.fragments.dialog;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.res.Resources;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.widget.Toast;
//
//import com.afollestad.materialdialogs.AlertDialogWrapper;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SingleChoiceDialogFragment extends DialogFragment {
//	public static final String HAS_CANCEL_BUTTON = "has_cancel_button";
//	public static final String TITLE_STRING = "title_string";
//	public static final String TITLE_ID = "title_id";
//	public static final String DATA_LIST = "items_list";
//	public static final String DATA_ARRAY_ID = "items_array_id";
//	public static final String LOGGED_IN = "selected";
//
//	public interface SingleChoiceDialogCallback {
//		public void singleChoiceDialogItemSelected(int index, String value);
//	}
//
//	public static SingleChoiceDialogFragment newInstance(
//			SingleChoiceDialogCallback callback) {
//		SingleChoiceDialogFragment fragment = new SingleChoiceDialogFragment();
//		fragment.setCallback(callback);
//		return fragment;
//	}
//
//    public static SingleChoiceDialogFragment newInstance(
//            boolean has_cancel_button, int title_id, int array_id, SingleChoiceDialogCallback callback) {
//        SingleChoiceDialogFragment fragment = new SingleChoiceDialogFragment();
//        fragment.setCallback(callback);
//        Bundle bundle = new Bundle();
//        bundle.putBoolean(HAS_CANCEL_BUTTON, has_cancel_button);
//        bundle.put(TITLE_ID, title_id);
//        bundle.put(DATA_ARRAY_ID, array_id);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//    public static SingleChoiceDialogFragment newInstance(
//            boolean has_cancel_button, int title_id, ArrayList<String> items, SingleChoiceDialogCallback callback) {
//        SingleChoiceDialogFragment fragment = new SingleChoiceDialogFragment();
//        fragment.setCallback(callback);
//        Bundle bundle = new Bundle();
//        bundle.putBoolean(HAS_CANCEL_BUTTON, has_cancel_button);
//        bundle.put(TITLE_ID, title_id);
//        bundle.putStringArrayList(DATA_LIST, items);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//	private SingleChoiceDialogCallback callback;
//	private String[] cs = null;
//
//	private int index;
//	private String value;
//
//	private void setCallback(SingleChoiceDialogCallback callback) {
//		this.callback = callback;
//	}
//
//    private boolean returnOnSelection;
//
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//		Resources res = getActivity().getResources();
//		Bundle bundle = getArguments();
//
//        AlertDialogWrapper.Builder dialog = new AlertDialogWrapper.Builder(getActivity());
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
//
//		if ((bundle.containsKey(DATA_LIST) || bundle.containsKey(DATA_ARRAY_ID))) {
//			if (bundle.containsKey(DATA_LIST)) {
//				@SuppressWarnings("unchecked") List<String> list = (List<String>) bundle
//						.get(DATA_LIST);
//				cs = list.toArray(new String[list.size()]);
//			}
//			if (bundle.containsKey(DATA_ARRAY_ID)) {
//				cs = res.getStringArray(bundle.getInt(DATA_ARRAY_ID));
//			}
//
//			int position = bundle.getInt(LOGGED_IN, 0);
//
//			value = cs[position].toString();
//
//            if (bundle.containsKey(LOGGED_IN)) {
//                dialog.setSingleChoiceItems(cs, position, selectItemListener);
//                dialog.setPositiveButton(android.R.string.ok,
//                        new PositiveButtonClickListener());
//            } else {
//                dialog.setItems(cs, selectItemListener);
//                returnOnSelection = true;
//            }
//
//		} else {
//			Toast.makeText(getActivity(), "Missing DATA or LOGGED_IN",
//					Toast.LENGTH_SHORT).show();
//		}
//
//		if (bundle.getBoolean(HAS_CANCEL_BUTTON, false)) {
//			dialog.setNegativeButton(android.R.string.cancel, null);
//		}
//
//		return dialog.create();
//	}
//
//	class PositiveButtonClickListener implements
//			DialogInterface.OnClickListener {
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//
//			if (callback != null) {
//                callback.singleChoiceDialogItemSelected(index, value);
//            }
//
//			dialog.dismiss();
//		}
//	}
//
//	OnClickListener selectItemListener = new OnClickListener() {
//
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			index = which;
//			value = cs[which].toString();
//
//            if (returnOnSelection && callback != null) {
//                callback.singleChoiceDialogItemSelected(index, value);
//                dialog.dismiss();
//            }
//
//		}
//	};
//
//	@Override
//	public void onAttach(android.app.Activity activity) {
//		if (activity instanceof SingleChoiceDialogCallback) {
//			callback = (SingleChoiceDialogCallback) activity;
//		} else {
//			if (getParentFragment() instanceof SingleChoiceDialogCallback) {
//				callback = (SingleChoiceDialogCallback) getParentFragment();
//			} else {
//				// throw new IllegalStateException(
//				// "Parent must implement SingleChoiceDialogCallback");
//			}
//		}
//		super.onAttach(activity);
//	};
//}
