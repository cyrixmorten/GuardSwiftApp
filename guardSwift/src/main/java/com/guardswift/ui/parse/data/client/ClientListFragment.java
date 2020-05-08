package com.guardswift.ui.parse.data.client;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ui.widget.ParseQueryAdapter;

public class ClientListFragment extends AbstractParseRecyclerFragment<Client, ClientAdapter.ClientViewHolder> {


    public interface OnClientSelectedListener {
        void clientSelected(Client client);
    }

    private ClientQueryBuilder.SORT_BY sortBy;

    public static ClientListFragment newInstance(ClientQueryBuilder.SORT_BY sortBy) {

        ClientListFragment fragment = new ClientListFragment();

        fragment.sortBy = sortBy;

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ClientListFragment() {
    }


    private OnClientSelectedListener onClientSelectedListener;

    public void setOnClientSelectedListener(OnClientSelectedListener onClientSelectedListener) {
        this.onClientSelectedListener = onClientSelectedListener;
    }



    @Override
    protected ParseQueryAdapter.QueryFactory<Client> createNetworkQueryFactory() {
        return () -> new ClientQueryBuilder(false)
                    .notAutomatic().sort(ClientListFragment.this.sortBy).build();
    }

    @Override
    protected ParseRecyclerQueryAdapter<Client, ClientAdapter.ClientViewHolder> createRecycleAdapter() {
        return new ClientAdapter(createNetworkQueryFactory(), (v, position) -> {
            Client client = getAdapter().getItem(position);

            if (onClientSelectedListener != null) {
                // Custom click handler
                onClientSelectedListener.clientSelected(client);
            }

            // TODO make optional from arguments
            Activity activity = this.getActivity();
            if (activity instanceof GenericToolbarActivity) {
                activity.finish();
            }

        });
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
