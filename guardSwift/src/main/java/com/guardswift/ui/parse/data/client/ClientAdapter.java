package com.guardswift.ui.parse.data.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.common.RecyclerViewClickListener;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PositionedViewHolder;
import com.parse.ParseQueryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClientAdapter extends ParseRecyclerQueryAdapter<Client, ClientAdapter.ClientViewHolder> {

	private static final String TAG = ClientAdapter.class.getSimpleName();

	private static RecyclerViewClickListener clientClicked;

	public ClientAdapter(final ParseQueryAdapter.QueryFactory<Client> factory, RecyclerViewClickListener clientClicked) {
		super(factory);
		ClientAdapter.clientClicked = clientClicked;
	}


	public static class ClientViewHolder extends PositionedViewHolder implements View.OnClickListener {

		@BindView(R.id.clientName)
		TextView clientName;

		@BindView(R.id.clientNumber)
		TextView clientNumber;

		@BindView(R.id.clientAddress)
		TextView clientAddress;

		public ClientViewHolder(View itemView) {
			super(itemView);

			ButterKnife.bind(this, itemView);

			itemView.setOnClickListener(this);
		}


		@Override
		public void onClick(View view) {
			clientClicked.recyclerViewListClicked(view, this.getAdapterPosition());
		}
	}

	@Override
	public void onDetatch() {
		ClientAdapter.clientClicked = null;

		super.onDetatch();
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
		holder.clientNumber.setText(client.getId());
		holder.clientAddress.setText(client.getFullAddress());

		holder.clientNumber.setVisibility(client.getId().isEmpty() ? View.VISIBLE : View.GONE);

		new PositionedViewHolder.CalcDistanceAsync(client, holder).execute();
	}


}
