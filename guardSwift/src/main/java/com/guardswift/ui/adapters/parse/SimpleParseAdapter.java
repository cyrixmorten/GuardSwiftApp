//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.guardswift.R;
//import com.parse.ParseObject;
//import com.parse.ParseQueryAdapter;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
//public class SimpleParseAdapter<T extends ParseObject> extends
//		ParseQueryAdapter<T> {
//
//	private static final String TAG = SimpleParseAdapter.class.getSimpleName();
//
//	private final String textCol;
//
//	public SimpleParseAdapter(Context context, String textCol,
//			QueryFactory<T> queryFactory) {
//		super(context, queryFactory);
//		this.textCol = textCol;
//	}
//
//	@BindView(R.id.text) TextView text;
//
//	@Override
//	public View getItemView(T object, View v, ViewGroup parent) {
//
//		if (v == null) {
//			v = View.inflate(getContext(), R.layout.gs_view_adapter_item_selectable_simple,
//					null);
//		}
//
//		super.getItemView(object, v, parent);
//
//		ButterKnife.bind(this, v);
//
//		text.setText(object.getString(textCol));
//
//		return v;
//
//	}
//
//	@Override
//	public T getItem(int index) {
//		return super.getItem(index);
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
