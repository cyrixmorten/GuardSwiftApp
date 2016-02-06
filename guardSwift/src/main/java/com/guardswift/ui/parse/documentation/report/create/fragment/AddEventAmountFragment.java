//package com.guardswift.ui.fragments.documentation.create_event;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.beardedhen.androidbootstrap.BootstrapButton;
//import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
//import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingListFragment;
//import com.guardswift.ui.fragments.documentation.create_event.event.AbstractCreateEventActivity;
//import com.guardswift.ui.adapters.SimpleArrayAdapter;
//
//import java.util.Locale;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class AddEventAmountFragment extends InjectingListFragment {
//
//	protected static final String TAG = AddEventAmountFragment.class
//			.getSimpleName();
//
//	public static AddEventAmountFragment newInstance() {
//		AddEventAmountFragment fragment = new AddEventAmountFragment();
//		return fragment;
//	}
//
//	public AddEventAmountFragment() {
//	}
//
//	private String[] values;
//	private ArrayAdapter<String> mAdapter;
//
//	@Bind(R.id.header) TextView header;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		// disableCroutons();
//		super.onCreate(savedInstanceState);
//	}
//
//	@Bind(R.id.btn_footer)
//    BootstrapButton footerButton;
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		View rootView = inflater.inflate(R.layout.listview_selectable_header_button_footer,
//				container, false);
//
//		ButterKnife.bind(this, rootView);
//
//
//		values = new String[] { getString(R.string.enter_amount), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
//				"10" };
//
//		mAdapter = new SimpleArrayAdapter(getActivity(), values);
//		setListAdapter(mAdapter);
//
//
//		header.setText(getString(R.string.title_event_amount).toUpperCase(
//                Locale.getDefault()));
//
//        footerButton.setText(getString(R.string.enter_other_amount));
//
//		return rootView;
//	}
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        int selectedAmount = ((AbstractCreateEventActivity)getActivity()).getAmount();
//        if (selectedAmount >= 0) {
//            preSelectAmount(selectedAmount);
//        }
//    }
//
//    @Override
//	public void onListItemClick(ListView l, View v, int clientPosition, long id) {
//        if (clientPosition == 0) {
//            enterAmount();
//        } else {
//            selectAmount(values[clientPosition]);
//        }
//		super.onListItemClick(l, v, clientPosition, id);
//	}
//
//    private void preSelectAmount(int amount) {
//        if (amount <= 10) {
//            getListView().setItemChecked((amount+1), true);
//        } else {
//            updateOtherAmount(String.valueOf(amount));
//            getListView().setItemChecked(0, true);
//        }
//    }
//    private void selectAmount(String amountString) {
//        Toast.makeText(getActivity(), amountString, Toast.LENGTH_LONG).show();
//        int amount = 0;
//        try {
//            amount = Integer.parseInt(amountString);
//            preSelectAmount(amount);
//        } catch (NumberFormatException e) {
////            ((AbstractCreateEventActivity) getActivity()).setAmount(0, true);
//        }
//        ((AbstractCreateEventActivity) getActivity()).setAmount(amount, true);
//    }
//
//    private void updateOtherAmount(String amountString) {
//        values[0] = amountString;
//        mAdapter.notifyDataSetChanged();
//    }
//
//    @OnClick(R.id.btn_footer)
//    public void enterAmount() {
//        new NumberPickerBuilder()
//
//                .setFragmentManager(getActivity().getSupportFragmentManager())
//                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
//                .addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandler() {
//                    @Override
//                    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
//                        selectAmount(String.valueOf(number));
////                        preSelectAmount(Integer.parseInt(input.toString()));
//                    }
//                })
//                .setLabelText(getString(R.string.amount))
//                .setPlusMinusVisibility(View.INVISIBLE)
//                .setDecimalVisibility(View.INVISIBLE)
//                .show();
////        new MaterialDialog.Builder(getActivity())
////                .title(R.string.enter_other_amount)
////                .inputType(InputType.TYPE_CLASS_NUMBER)
////                .input(R.string.amount, R.string.input_empty, new MaterialDialog.InputCallback() {
////                    @Override
////                    public void onInput(MaterialDialog dialog, CharSequence input) {
////                        if (input.length() > 0) {
////                            selectAmount(input.toString());
////                            preSelectAmount(Integer.parseInt(input.toString()));
////                        }
////                    }
////                }).show();
//    }
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        if (getActivity() instanceof AbstractCreateEventActivity) {
//            // OK
//        } else {
//            throw new IllegalStateException(
//                    "Activity must be an instance of AbstractCreateEventActivity!");
//        }
//    }
//
//	@Override
//	public void onDestroyView() {
//		super.onDestroyView();
//		ButterKnife.unbind(this);
//	}
//
//}