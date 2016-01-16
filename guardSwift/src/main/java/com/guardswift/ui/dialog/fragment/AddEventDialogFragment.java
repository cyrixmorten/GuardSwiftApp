//package com.guardswift.ui.fragments.dialog;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.ContextThemeWrapper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingDialogFragment;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.data.EventType;
//import com.guardswift.persistence.parse.planning.regular.CircuitUnit;
//import com.parse.FindCallback;
//import com.parse.GetCallback;
//import com.parse.ParseException;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.ButterKnife;
//import ButterKnife.bindView;
//import de.greenrobot.event.EventBus;
//
//import static com.guardswift.ui.fragments.dialog.Dialogs.MESSAGE_ID;
//import static com.guardswift.ui.fragments.dialog.Dialogs.TITLE_ID;
//
//public class AddEventDialogFragment extends InjectingDialogFragment {
//
//	public interface AddEventCallback {
//		public void addEvent(String type, int amount, String newClientLocation,
//				String location, String remark);
//	}
//
//	protected static final String TAG = AddEventDialogFragment.class
//			.getSimpleName();
//
//	private static AddEventCallback mAddEventCallback;
//
//	public static AddEventDialogFragment newInstance(
//			AddEventCallback dialogInterface) {
//
//		mAddEventCallback = dialogInterface;
//
//		AddEventDialogFragment frag = new AddEventDialogFragment();
//		Bundle bundle = new Bundle();
//		bundle.put(TITLE_ID, R.string.title_event_add);
//		bundle.put(MESSAGE_ID, R.drawable.ic_action_new_event);
//
//		frag.setCancelable(false);
//		frag.setArguments(bundle);
//
//		return frag;
//	}
//
//	public AddEventDialogFragment() {
//		// Empty constructor required for DialogFragment
//	}
//
//	@Bind(R.id.eventType) Spinner eventType;
//	@Bind(R.id.eventAmount) Spinner eventAmount;
//	@Bind(R.id.eventClientLocation) Spinner eventClientLocation;
//	@Bind(R.id.eventNewClientLocation) EditText eventNewClientLocation;
//	@Bind(R.id.eventRemarks) EditText eventRemarks;
//
//	@Bind(R.id.addEventLayout) LinearLayout addEventLayout;
//	@Bind(R.id.loadingContent) ProgressBar loadingContent;
//
//	@Bind(R.id.eventClientLocationLayout) LinearLayout eventClientLocationLayout;
//
//	private final int elementsToLoad = 2;
//	private int loadedCounter;
//
//	// private void contentElementLoaded() {
//	// loadedCounter++;
//	// if (loadedCounter == elementsToLoad) {
//	//
//	// new Handler().postDelayed(new Runnable() {
//	//
//	// @Override
//	// public void run() {
//	// if (loadingContent != null)
//	// loadingContent.setVisibility(View.GONE);
//	// }
//	// }, 200);
//	// new Handler().postDelayed(new Runnable() {
//	//
//	// @Override
//	// public void run() {
//	// if (addEventLayout != null)
//	// addEventLayout.setVisibility(View.VISIBLE);
//	// }
//	// }, 700);
//	// }
//	// }
//
//	private ContextThemeWrapper themedContext;
//
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//		int title = getArguments().getInt(TITLE_ID);
//		int icon = getArguments().getInt(MESSAGE_ID);
//
//		loadedCounter = 0;
//
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//
//		View v = inflater.inflate(R.layout.dialog_add_event, null);
//
//		ButterKnife.bind(this, v);
//
//		themedContext = new ContextThemeWrapper(getActivity(),
//				android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
//
//		populateEventTypeSpinner(true);
//		populateAmountSpinner();
//		populateClientLocationSpinner(true);
//
//		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
//				themedContext)
//				.setView(v)
//				.setTitle(title)
//				.setIcon(icon)
//				.setPositiveButton(R.string.action_save,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								if (mAddEventCallback != null) {
//									String type = (String) eventType
//											.getSelectedItem();
//									String amountString = (String) eventAmount
//											.getSelectedItem();
//									int amount = Integer.parseInt(amountString);
//									String location = (String) eventClientLocation
//											.getSelectedItem();
//									String newClientLocation = eventNewClientLocation
//											.getEditableText().toString();
//									String remark = eventRemarks
//											.getEditableText().toString();
//
//									mAddEventCallback.addEvent(type, amount,
//											newClientLocation, location,
//											remark);
//								}
//							}
//						})
//				.setNeutralButton(android.R.string.cancel,
//						new OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								// do nothing
//							}
//						});
//
//		AlertDialog dialog = alertBuilder.create();
//		setCancelable(true);
//		dialog.setCanceledOnTouchOutside(true);
//
//		return dialog;
//	}
//
//	private void populateClientLocationSpinner(final boolean fromDataStore) {
//		CircuitUnit circuitUnit = EventBus.getDefault().getStickyEvent(
//				CircuitUnit.class);
//		if (circuitUnit != null) {
//			circuitUnit.getClient().fetchFromLocalDatastoreInBackground(
//					new GetCallback<Client>() {
//
//						@Override
//						public void done(Client object, ParseException e) {
//
//							if (e != null) {
//								Log.e(TAG,
//										"populateClientLocationSpinner fromDataStore "
//												+ fromDataStore, e);
//								if (fromDataStore) {
//									populateClientLocationSpinner(false);
//								} else {
//									Toast.makeText(
//											getActivity(),
//											getString(R.string.message_no_internet_connection),
//											Toast.LENGTH_SHORT).show();
//									dismiss();
//								}
//
//								return;
//							}
//
//							// contentElementLoaded();
//
////							List<String> location = object
////									.getLocations();
//                            List<String> location = new ArrayList<String>();
//
//							if (location == null
//									|| location.size() == 0) {
//								return;
//							}
//							ArrayAdapter<String> eventClientLocationAdapter = new ArrayAdapter<String>(
//									themedContext,
//									android.R.layout.simple_spinner_item,
//									location);
//
//							eventClientLocationAdapter
//									.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//							// Apply the adapter to the spinner
//							if (eventClientLocation != null) {
//								eventClientLocation
//										.setAdapter(eventClientLocationAdapter);
//								eventClientLocationLayout
//										.setVisibility(View.VISIBLE);
//							}
//						}
//					});
//		}
//	}
//
//	private void populateEventTypeSpinner(final boolean fromDataStore) {
//		new EventType.QueryBuilder(true).sortByTimesUsed().build()
//				.findInBackground(new FindCallback<EventType>() {
//
//					@Override
//					public void done(List<EventType> objects, ParseException e) {
//
//						if (e != null) {
//							Log.e(TAG,
//									"populateEventTypeSpinner fromDataStore "
//											+ fromDataStore, e);
//							if (fromDataStore) {
//								populateEventTypeSpinner(false);
//							} else {
//								Toast.makeText(
//										getActivity(),
//										getString(R.string.message_no_internet_connection),
//										Toast.LENGTH_SHORT).show();
//								dismiss();
//							}
//
//							return;
//						}
//						// contentElementLoaded();
//
//						List<String> mTypeNames = new ArrayList<String>();
//
//						for (EventType eventType : objects) {
//							mTypeNames.addUnique(eventType.getName());
//						}
//
//						ArrayAdapter<String> eventTypesAdapter = new ArrayAdapter<String>(
//								themedContext,
//								android.R.layout.simple_spinner_item,
//								mTypeNames);
//
//						eventTypesAdapter
//								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//						if (eventType != null) {
//							eventType.setAdapter(eventTypesAdapter);
//						}
//					}
//				});
//	}
//
//	private void populateAmountSpinner() {
//		String[] amountValues = new String[] { "1", "2", "3", "4", "5", "6",
//				"7", "8", "9", "10" };
//		ArrayAdapter<String> amountAdapter = new ArrayAdapter<String>(
//				themedContext, android.R.layout.simple_spinner_item,
//				amountValues);
//
//		amountAdapter
//				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		// Apply the adapter to the spinner
//		eventAmount.setAdapter(amountAdapter);
//	}
//
//	@Override
//	public void onDestroyView() {
//		ButterKnife.unbind(this);
//		super.onDestroyView();
//	}
//
//}
