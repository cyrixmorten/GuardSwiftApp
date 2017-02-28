package com.guardswift.ui.parse.data.client;

import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.common.RecyclerViewClickListener;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class ClientListFragment extends AbstractParseRecyclerFragment<Client, ClientAdapter.ClientViewHolder> {


    public interface OnClientSelectedListener {
        void clientSelected(Client client);
    }

    private Client.SORT_BY sortBy;

    public static ClientListFragment newInstance(Client.SORT_BY sortBy) {

        ClientListFragment fragment = new ClientListFragment();

        fragment.sortBy = sortBy;

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


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
                return new Client.QueryBuilder(false).notAutomatic().sort(ClientListFragment.this.sortBy).build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Client, ClientAdapter.ClientViewHolder> createRecycleAdapter() {
        return new ClientAdapter(createNetworkQueryFactory(), new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                Client client = getAdapter().getItem(position);

                if (onClientSelectedListener != null) {
                    // creating static report
                    onClientSelectedListener.clientSelected(client);
                }
            }
        });
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
