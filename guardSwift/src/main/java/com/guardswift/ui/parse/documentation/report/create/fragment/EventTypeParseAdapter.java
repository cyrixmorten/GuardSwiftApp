package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.data.EventType;
import com.parse.ParseQueryAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EventTypeParseAdapter extends ParseQueryAdapter<EventType> {

	private static final String TAG = EventTypeParseAdapter.class
			.getSimpleName();

	public EventTypeParseAdapter(Context context,
			QueryFactory<EventType> queryFactory) {
		super(context, queryFactory);
	}

	@Bind(R.id.text) TextView text;

	@Override
	public View getItemView(EventType object, View v, ViewGroup parent) {

		if (v == null) {
			v = View.inflate(getContext(), R.layout.gs_view_adapter_item_selectable_simple,
					null);
		}

		super.getItemView(object, v, parent);

		ButterKnife.bind(this, v);

		text.setText(object.getName());


		return v;

	}

	@Override
	public EventType getItem(int index) {
		return super.getItem(index);
	}

	@Override
	public View getNextPageView(View v, ViewGroup parent) {
		if (v == null) {
			v = View.inflate(getContext(), R.layout.view_adapter_item_loadmore,
					null);
		}
		return v;
	}

}
