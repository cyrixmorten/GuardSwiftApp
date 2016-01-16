package com.guardswift.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.guardswift.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SimpleMultichoiceArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> items;
    private final boolean[] checkedItems;

	public SimpleMultichoiceArrayAdapter(Context context, List<String> items, boolean[] checkedItems) {
		super(context, R.layout.view_adapter_item_checklist, items);
		this.context = context;
		this.items = items;
        this.checkedItems = checkedItems;
	}

	@Bind(R.id.item)
    CheckedTextView item;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflater.inflate(R.layout.view_adapter_item_checklist,
					parent, false);
		}

		ButterKnife.bind(this, rowView);

        String value = items.get(position);
        boolean checked = (checkedItems.length > position) && checkedItems[position];

		item.setText(value);
        item.setChecked(checked);

		ButterKnife.bind(this, rowView);

		return rowView;
	}
}