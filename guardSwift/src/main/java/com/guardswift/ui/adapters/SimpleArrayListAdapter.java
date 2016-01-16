package com.guardswift.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SimpleArrayListAdapter extends ArrayAdapter<String> {

    private final int item_layout;
	private final Context context;
	private final List<String> values;

    private final View.OnLongClickListener onLongClickListener;

	public SimpleArrayListAdapter(Context context, int item_layout, List<String> values) {
		super(context, item_layout, values);
        // R.layout.gs_view_adapter_item_selectable_simple
		this.context = context;
		this.values = values;
        this.item_layout = item_layout;
        this.onLongClickListener = null;
	}

    public SimpleArrayListAdapter(Context context, int item_layout, List<String> values, View.OnLongClickListener onLongClickListener) {
        super(context, item_layout, values);
        // R.layout.gs_view_adapter_item_selectable_simple
        this.context = context;
        this.values = values;
        this.item_layout = item_layout;
        this.onLongClickListener = onLongClickListener;
    }

	@Bind(android.R.id.text1) TextView text;
//	@Bind(R.id.star) FontAwesomeText star;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // R.layout.gs_view_adapter_item_selectable_simple
		View rowView = inflater.inflate(item_layout,
				parent, false);

		ButterKnife.bind(this, rowView);

		text.setText(values.get(position));
//		star.setVisibility(View.GONE);

        if (onLongClickListener != null) {
            text.setOnLongClickListener(onLongClickListener);
        }

		return rowView;
	}
}