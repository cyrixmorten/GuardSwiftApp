//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CheckedTextView;
//
//import com.guardswift.R;
//import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitStarting;
//import com.parse.ParseQuery;
//import com.parse.ParseQueryAdapter;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class ChecklistCircuitStartAdapter extends
//		ParseQueryAdapter<ChecklistCircuitStarting> {
//
//	private static final String TAG = ChecklistCircuitStartAdapter.class
//			.getSimpleName();
//
//	public ChecklistCircuitStartAdapter(Context context) {
//		super(context,
//				new ParseQueryAdapter.QueryFactory<ChecklistCircuitStarting>() {
//
//					@Override
//					public ParseQuery<ChecklistCircuitStarting> create() {
//						return new ChecklistCircuitStarting.QueryBuilder(true)
//								.build();
//					}
//				});
//
//	}
//
//	@BindView(R.id.item)
//    CheckedTextView item;
//
//	@Override
//	public View getItemView(ChecklistCircuitStarting object, View v,
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
