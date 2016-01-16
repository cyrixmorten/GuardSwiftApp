package com.guardswift.ui.parse.data.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PositionedViewHolder;
import com.parse.ParseQueryAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ClientAdapter extends ParseRecyclerQueryAdapter<Client, ClientAdapter.ClientViewHolder> {

	private static final String TAG = ClientAdapter.class.getSimpleName();

	public ClientAdapter(final ParseQueryAdapter.QueryFactory<Client> factory) {
		super(factory);
	}

	public static class ClientViewHolder extends PositionedViewHolder {

		@Bind(R.id.clientName)
		TextView clientName;

		@Bind(R.id.clientNumber)
		TextView clientNumber;

		@Bind(R.id.clientAddress)
		TextView clientAddress;

		public ClientViewHolder(View itemView) {
			super(itemView);

			ButterKnife.bind(this, itemView);
		}
	}

	@Override
	public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.
				from(parent.getContext()).
				inflate(R.layout.gs_card_client, parent, false);
		return new ClientViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ClientViewHolder holder, int position) {
		final Client client = getItem(position);

		holder.clientName.setText(client.getName());
		holder.clientNumber.setText(String.valueOf(client.getNumber()));
		holder.clientAddress.setText(client.getFullAddress());

		holder.clientNumber.setVisibility((client.getNumber() != 0) ? View.VISIBLE : View.GONE);

		new PositionedViewHolder.CalcDistanceAsync(client, holder).execute();
	}


}
