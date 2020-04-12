package com.guardswift.ui.parse.data.client;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.client.ClientContact;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClientContactsAdapter extends RecyclerView.Adapter<ClientContactsAdapter.ClientContactViewHolder> {

	private static final String TAG = ClientContactsAdapter.class.getSimpleName();

	List<ClientContact> contacts;

	public ClientContactsAdapter(List<ClientContact> contacts) {
		this.contacts = Lists.newArrayList();
		for (ClientContact contact: contacts) {
			if (!contact.getName().isEmpty()) {
				contacts.add(contact);
			}
		}
	}

	public static class ClientContactViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.tvName)
		TextView contactName;

		@BindView(R.id.tvPhoneNumber)
		TextView contactPhoneNumber;

		@BindView(R.id.tvDescription)
		TextView contactDescription;

		public ClientContactViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}

	@Override
	public ClientContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.
				from(parent.getContext()).
				inflate(R.layout.gs_view_clientcontact, parent, false);
		return new ClientContactViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ClientContactViewHolder holder, int position) {
		final ClientContact contact = contacts.get(position);
		holder.contactName.setText(contact.getName());
		holder.contactPhoneNumber.setText(contact.getPhoneNumber());
		holder.contactDescription.setText(contact.getDesc());
	}

	@Override
	public int getItemCount() {
		return contacts.size();
	}



}
