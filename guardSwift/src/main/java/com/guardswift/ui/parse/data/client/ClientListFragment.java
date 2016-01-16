package com.guardswift.ui.parse.data.client;

import android.location.Location;
import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class ClientListFragment extends AbstractParseRecyclerFragment<Client, ClientAdapter.ClientViewHolder> {


    public static ClientListFragment newInstance() {

        ClientListFragment fragment = new ClientListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new Client();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<Client> getNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Client>() {
            @Override
            public ParseQuery<Client> create() {
                return new Client.QueryBuilder(false).sortByDistance().build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Client, ClientAdapter.ClientViewHolder> getRecycleAdapter() {
        return new ClientAdapter(getNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
