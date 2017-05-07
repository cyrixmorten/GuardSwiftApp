//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.guardswift.R;
//import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitEnding;
//import com.parse.ParseQuery;
//import com.parse.ParseQueryAdapter;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class ChecklistCircuitEndAdapter extends
//		ParseQueryAdapter<ChecklistCircuitEnding> {
//
//	private static final String TAG = ChecklistCircuitEndAdapter.class
//			.getSimpleName();
//
//	public ChecklistCircuitEndAdapter(Context context) {
//		super(context,
//				new ParseQueryAdapter.QueryFactory<ChecklistCircuitEnding>() {
//
//					@Override
//					public ParseQuery<ChecklistCircuitEnding> create() {
//						return new ChecklistCircuitEnding.QueryBuilder(true)
//								.build();
//					}
//				});
//
//	}
//
//	@BindView(R.id.item) TextView item;
//
//	@Override
//	public View getItemView(ChecklistCircuitEnding object, View v,
//			ViewGroup parent) {
//
//		if (v == null) {
//			v = View.inflate(getContext(),
//					R.layout.view_adapter_item_checklist, null);
//		}
//
//		super.getItemView(object, v, parent);
//
//		ButterKnife.bind(this, v);
//
//		item.setText(object.getItem());
//
//		return v;
//
//	}
//
//	@Override
//	public View getNextPageView(View v, ViewGroup parent) {
//		if (v == null) {
//			v = View.inflate(getContext(), R.layout.view_adapter_item_loadmore,
//					null);
//		}
//		return v;
//	}
//
//}
