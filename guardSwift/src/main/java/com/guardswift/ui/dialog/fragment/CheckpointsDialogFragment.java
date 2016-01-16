//package com.guardswift.ui.fragments.dialog;
//
//import android.app.Dialog;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.DialogFragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//
//import com.guardswift.R;
//import com.guardswift.ui.fragments.task.circuit.CircuitUnitCheckpointsFragment;
//
//public class CheckpointsDialogFragment extends DialogFragment {
//
//    protected static final String TAG = CheckpointsDialogFragment.class
//            .getSimpleName();
//
//
//    public static CheckpointsDialogFragment newInstance() {
//
//        CheckpointsDialogFragment frag = new CheckpointsDialogFragment();
//        Bundle bundle = new Bundle();
//
//        frag.setCancelable(false);
//        frag.setArguments(bundle);
//
//        return frag;
//    }
//
//    public CheckpointsDialogFragment() {
//        // Empty constructor required for DialogFragment
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.dialog_checkpoints, null);
//        final Dialog dialog = getDialog();
//        dialog.setTitle(R.string.checkpoints);
////        dialog.setPositiveButton(android.R.string.ok,
////                        new DialogInterface.OnClickListener() {
////                            public void onClick(DialogInterface dialog, int whichButton) {
////
////                            }
////                        }
////                );
//
//        ((Button)rootView.findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        return rootView;
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//
//        getChildFragmentManager().beginTransaction().replace(R.id.frag_checkpoints, CircuitUnitCheckpointsFragment.newInstance()).commit();
//
//        super.onViewCreated(view, savedInstanceState);
//    }
//
//
//    //    @Override
////    public Dialog onCreateDialog(Bundle savedInstanceState) {
////        LayoutInflater inflater = getActivity().getLayoutInflater();
//////        View dialogView = inflater.inflate(R.layout.dialog_checkpoints, null, false);
////
////
////
////        Dialog diag = new AlertDialog.Builder(getActivity())
////                .setTitle(R.string.checkpoints)
////                .setCancelable(false)
//////                .setView(dialogView)
////                .setPositiveButton(android.R.string.ok,
////                        new DialogInterface.OnClickListener() {
////                            public void onClick(DialogInterface dialog, int whichButton) {
////
////                            }
////                        }
////                )
////                .create();
////
//////        View fragView = dialogView.findViewById(R.id.frag_checkpoints);
//////
//////        Fragment checkpointsFragment = CircuitUnitCheckpointsFragment.newInstance();
//////        getChildFragmentManager().beginTransaction().replace(fragView.getId(), checkpointsFragment).commit();
////
////        return diag;
////    }
//
//
//}
