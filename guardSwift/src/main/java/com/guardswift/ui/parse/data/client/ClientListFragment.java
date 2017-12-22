package com.guardswift.ui.parse.data.client;

import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.helpers.RecyclerViewClickListener;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.data.client.details.ClientDetailsViewpagerFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

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
        super(false);
    }


    private OnClientSelectedListener onClientSelectedListener;

    public void setOnClientSelectedListener(OnClientSelectedListener onClientSelectedListener) {
        this.onClientSelectedListener = onClientSelectedListener;
    }



    @Override
    protected ParseQueryAdapter.QueryFactory<Client> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Client>() {
            @Override
            public ParseQuery<Client> create() {
                return new ClientQueryBuilder(false).notAutomatic().sort(ClientListFragment.this.sortBy).build();
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
                    // Custom click handler
                    onClientSelectedListener.clientSelected(client);
                }
                else {
                    GenericToolbarActivity.start(getContext(), client.getName(), client.getFullAddress(), ClientDetailsViewpagerFragment.newInstance(client));
                }
            }
        });
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
