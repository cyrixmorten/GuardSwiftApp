//package com.guardswift.ui.fragments.dialog;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.view.ContextThemeWrapper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.EditText;
//
//import com.afollestad.materialdialogs.AlertDialogWrapper;
//import com.guardswift.util.Util;
//
//import static com.guardswift.ui.fragments.dialog.Dialogs.ICON_ID;
//import static com.guardswift.ui.fragments.dialog.Dialogs.MESSAGE_ID;
//import static com.guardswift.ui.fragments.dialog.Dialogs.MESSAGE_STRING;
//import static com.guardswift.ui.fragments.dialog.Dialogs.TITLE_ID;
//
//public class GenericEditTextDialogFragment extends DialogFragment {
//
//    public interface GenericEditTextDialogInterface {
//
//        public void okClicked(String editTextString);
//
//    }
//
//    private static GenericEditTextDialogInterface mDialogInterface;
//
//    public static GenericEditTextDialogFragment newInstance(
//            int title, GenericEditTextDialogInterface dialogInterface) {
//        Bundle bundle = new Bundle();
//        bundle.put(TITLE_ID, title);
//        return GenericEditTextDialogFragment.newInstance(bundle,dialogInterface);
//    }
//
//    public static GenericEditTextDialogFragment newInstance(
//            int title, String text, GenericEditTextDialogInterface dialogInterface) {
//        Bundle bundle = new Bundle();
//        bundle.put(TITLE_ID, title);
//        bundle.put(MESSAGE_STRING, text);
//        return GenericEditTextDialogFragment.newInstance(bundle,dialogInterface);
//    }
//
//    public static GenericEditTextDialogFragment newInstance(
//            int title,
//            int message, GenericEditTextDialogInterface dialogInterface) {
//        Bundle bundle = new Bundle();
//        bundle.put(TITLE_ID, title);
//        bundle.put(MESSAGE_ID, message);
//        return GenericEditTextDialogFragment.newInstance(bundle, dialogInterface);
//    }
//
//    public static GenericEditTextDialogFragment newInstance(
//            Bundle bundle, GenericEditTextDialogInterface dialogInterface) {
//
//        mDialogInterface = dialogInterface;
//
//        GenericEditTextDialogFragment frag = new GenericEditTextDialogFragment();
//
//        frag.setCancelable(false);
//        frag.setArguments(bundle);
//
//        return frag;
//    }
//
//    public GenericEditTextDialogFragment() {
//        // Empty constructor required for DialogFragment
//    }
//
//    // private EditText editText;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // editText = new EditText(getActivity());
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
//
//    private EditText editText;
//
//    public void setText(String text) {
//        // if (editText != null) {
//        // editText.setText(text);
//        // }
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        final EditText editText = new EditText(getActivity());
//        editText.requestFocus();
//
//
//        // ContextThemeWrapper context = new ContextThemeWrapper(getActivity(),
//        // android.R.style.Theme_Holo_Dialog_NoActionBar);
//        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(),
//                android.R.style.Theme_Holo_Light_DarkActionBar);
//
//        AlertDialogWrapper.Builder alertBuilder = new AlertDialogWrapper.Builder(context)
//                .setCancelable(true)
//                .setView(editText)
//                .setPositiveButton(android.R.string.ok, new OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        mDialogInterface.okClicked(editText.getText()
//                                .toString());
//                        editText.setText("");
//                        Util.hideKeyboard(getActivity(),
//                                editText.getWindowToken());
//                    }
//                })
//                .setNegativeButton(android.R.string.cancel,
//                        new OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//                                editText.setText("");
//                                Util.hideKeyboard(getActivity(),
//                                        editText.getWindowToken());
//                            }
//                        });
//
//        Bundle bundle = getArguments();
//
//        if (bundle.containsKey(TITLE_ID))
//            alertBuilder.setTitle(bundle.getInt(TITLE_ID));
//        if (bundle.containsKey(MESSAGE_ID))
//            alertBuilder.setMessage(bundle.getInt(MESSAGE_ID));
//        if (bundle.containsKey(MESSAGE_STRING))
//            editText.setText(bundle.getString(MESSAGE_STRING));
//        if (bundle.containsKey(ICON_ID))
//            alertBuilder.setIcon(bundle.getInt(ICON_ID));
//
//
//        Dialog dialog = alertBuilder.create();
//
//        return dialog;
//    }
//
//}
