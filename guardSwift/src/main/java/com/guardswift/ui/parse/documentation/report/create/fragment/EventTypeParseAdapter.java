package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.data.EventType;
import com.parse.ui.widget.ParseQueryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventTypeParseAdapter extends ParseQueryAdapter<EventType> {

	public EventTypeParseAdapter(Context context,
			QueryFactory<EventType> queryFactory) {
		super(context, queryFactory);
		setObjectsPerPage(50);
	}

	@BindView(R.id.text) TextView text;

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
	public View getNextPageView(View v, ViewGroup parent) {
		if (v == null) {
			v = View.inflate(getContext(), R.layout.view_adapter_item_loadmore,
					null);
		}
		return v;
	}

}
