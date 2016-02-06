package com.guardswift.ui.parse.data.client;

import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.common.RecyclerViewClickListener;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import javax.inject.Inject;

public class ClientListFragment extends AbstractParseRecyclerFragment<Client, ClientAdapter.ClientViewHolder> {


    public interface OnClientSelectedListener {
        void clientSelected(Client client);
    }

    public static ClientListFragment newInstance() {

        ClientListFragment fragment = new ClientListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ClientCache clientCache;

    private OnClientSelectedListener onClientSelectedListener;

    public void setOnClientSelectedListener(OnClientSelectedListener onClientSelectedListener) {
        this.onClientSelectedListener = onClientSelectedListener;
    }

    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new Client();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<Client> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Client>() {
            @Override
            public ParseQuery<Client> create() {
                return new Client.QueryBuilder(false).sortByDistance().build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Client, ClientAdapter.ClientViewHolder> createRecycleAdapter() {
        final ClientAdapter adapter = new ClientAdapter(createNetworkQueryFactory(), new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                if (onClientSelectedListener != null) {
                    onClientSelectedListener.clientSelected(getAdapter().getItem(position));
                }
            }
        });

        return adapter;
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
