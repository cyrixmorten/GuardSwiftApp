//package com.guardswift.ui.fragments.data.checklist;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.guardswift.R;
//import com.guardswift.ui.activity.GuardLoginActivity;
//import com.guardswift.ui.adapters.parse.ChecklistCircuitEndAdapter;
//import com.guardswift.dagger.InjectingListFragment;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitEnding;
//import com.parse.ParseObject;
//import com.parse.ParseQueryAdapter.OnQueryLoadListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class ChecklistEndFragment extends InjectingListFragment {
//
//	protected static final String TAG = ChecklistEndFragment.class
//			.getSimpleName();
//
//	public ChecklistEndFragment() {
//
//	}
//
//	private ChecklistCircuitEndAdapter mAdapter;
//
//	@Inject
//    ParseModule parse;
//
//	@BindView(R.id.checklist_header) TextView header;
//	@BindView(R.id.checklist_subheader) TextView subheader;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//
//		mAdapter = new ChecklistCircuitEndAdapter(getActivity());
//
//		super.onCreate(savedInstanceState);
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//
//		View mRootView = inflater.inflate(R.layout.fragment_checklist,
//				container, false);
//
//		ButterKnife.bind(this, mRootView);
//
//		header.setText(getActivity().getString(
//				R.string.checklist_header_circuit_end));
//		subheader.setText(getActivity().getString(
//				R.string.checklist_subheader_check_all_to_continue));
//
//		setListAdapter(mAdapter);
//		return mRootView;
//	}
//
//	private List<String> checklistItems;
//
//	private final OnQueryLoadListener<ChecklistCircuitEnding> queryListener = new OnQueryLoadListener<ChecklistCircuitEnding>() {
//
//		@Override
//		public void onLoaded(List<ChecklistCircuitEnding> checklist,
//				Exception arg1) {
//
//			if (checklist.size() == 0)
//				checkListCompleted();
//
//			checklistItems = new ArrayList<String>();
//			for (ParseObject itemObject : checklist) {
//				String item = itemObject.getString("item");
//				checklistItems.addUnique(item);
//			}
//		}
//
//		@Override
//		public void onLoading() {
//		}
//	};
//
//	@Override
//	public void onResume() {
//		mAdapter.addOnQueryLoadListener(queryListener);
//		super.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		mAdapter.removeOnQueryLoadListener(queryListener);
//		super.onPause();
//	}
//
//	@Override
//	public void onListItemClick(ListView l, View v, int clientPosition, long id) {
//		int items = getListView().getCount();
//		int checked = getListView().getCheckedItemCount();
//		if (items == checked) {
//			checkListCompleted();
//		}
//	}
//
//	private void checkListCompleted() {
////        parse.logout(false);
//
//		Intent intent = new Intent(getActivity(), GuardLoginActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//		startActivity(intent);
//	}
//}
