//package com.guardswift.ui.fragments.dialog;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.res.Resources;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//
//import com.afollestad.materialdialogs.AlertDialogWrapper;
//
//public class MultiChoiceDialogFragment extends DialogFragment {
//    public static final String HAS_CANCEL_BUTTON = "has_cancel_button";
//    public static final String TITLE_ID = "title_id";
//    public static final String ITEMS_LIST = "items_list";
//    public static final String CHECKED_LIST = "checked_list";
//
//    public interface MultiChoiceDialogCallback {
//        public void multiChoiceDialogItemSelected(String checkpoint, boolean isChecked);
//    }
//
//    public static MultiChoiceDialogFragment newInstance(int title, String[] items, boolean[] checkedItems,
//                                                        MultiChoiceDialogCallback callback) {
//        MultiChoiceDialogFragment fragment = new MultiChoiceDialogFragment();
//        fragment.setCallback(callback);
//        Bundle b = new Bundle();
//        b.put(TITLE_ID, title);
//        b.putStringArray(ITEMS_LIST, items);
//        b.putBooleanArray(CHECKED_LIST, checkedItems);
//        fragment.setArguments(b);
//        return fragment;
//    }
//
//    private MultiChoiceDialogCallback callback;
//    private String[] cs = {};
//    private boolean[] checkedItems = {};
//
//
//    private void setCallback(MultiChoiceDialogCallback callback) {
//        this.callback = callback;
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Resources res = getActivity().getResources();
//        Bundle bundle = getArguments();
//
//        AlertDialogWrapper.Builder dialog = new AlertDialogWrapper.Builder(getActivity());
//
//        String title = "";
//        if (bundle.containsKey(TITLE_ID)) {
//            title = getString(bundle.getInt(TITLE_ID));
//        }
//        dialog.setTitle(title);
//
//        dialog.setPositiveButton(android.R.string.ok,
//                new PositiveButtonClickListener());
//
//
//        if ((bundle.containsKey(ITEMS_LIST))) {
//            cs = bundle
//                    .getStringArray(ITEMS_LIST);
//        }
//
//        if (bundle.containsKey(CHECKED_LIST)) {
//            checkedItems = bundle.getBooleanArray(CHECKED_LIST);
//        }
//
//        if (bundle.getBoolean(HAS_CANCEL_BUTTON, false)) {
//            dialog.setNegativeButton(android.R.string.cancel, null);
//        }
//
//        dialog.setMultiChoiceItems(cs, checkedItems, multiChoiceClickListener);
//
//        return dialog.create();
//    }
//
//    class PositiveButtonClickListener implements
//            OnClickListener {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//
////            if (callback != null)
////                callback.singleChoiceDialogItemSelected(index, value);
//
//
//            dialog.dismiss();
//        }
//    }
//
//    private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener = new DialogInterface.OnMultiChoiceClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//            String checkpoint = cs[which].toString();
//
//            if (callback != null) {
//                callback.multiChoiceDialogItemSelected(checkpoint, isChecked);
//            }
//        }
//    };
//
////    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
////        @Override
////        public void onItemSelected(AdapterView<?> parent, View view, int clientPosition, long id) {
////
////        }
////
////        @Override
////        public void onNothingSelected(AdapterView<?> parent) {
////
////        }
////    }
////    OnClickListener selectItemListener = new OnClickListener() {
////
////        @Override
////        public void onClick(DialogInterface dialog, int which) {
////            index = which;
////            value = cs[which].toString();
////        }
////    };
//
////    @Override
////    public void onAttach(android.app.Activity activity) {
////        if (activity instanceof SingleChoiceDialogCallback) {
////            callback = (SingleChoiceDialogCallback) activity;
////        } else {
////            if (getParentFragment() instanceof SingleChoiceDialogCallback) {
////                callback = (SingleChoiceDialogCallback) getParentFragment();
////            } else {
////                // throw new IllegalStateException(
////                // "Parent must implement SingleChoiceDialogCallback");
////            }
////        }
////        super.onAttach(activity);
////    }
//
//    ;
//}
