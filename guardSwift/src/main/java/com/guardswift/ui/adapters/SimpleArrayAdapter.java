package com.guardswift.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.guardswift.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SimpleArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public SimpleArrayAdapter(Context context, String[] values) {
		super(context, R.layout.gs_view_adapter_item_selectable_simple, values);
		this.context = context;
		this.values = values;
	}

	@Bind(R.id.text) TextView text;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.gs_view_adapter_item_selectable_simple,
				parent, false);

		ButterKnife.bind(this, rowView);

		text.setText(values[position]);

		ButterKnife.bind(this, rowView);

		return rowView;
	}
}