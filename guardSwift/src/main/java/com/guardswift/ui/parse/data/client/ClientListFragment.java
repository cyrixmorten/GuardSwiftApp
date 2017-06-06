package com.guardswift.ui.parse.data.client;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.activity.SlidingPanelActivity;
import com.guardswift.ui.helpers.RecyclerViewClickListener;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class ClientListFragment extends AbstractParseRecyclerFragment<Client, ClientAdapter.ClientViewHolder> {


    public interface OnClientSelectedListener {
        void clientSelected(Client client);
    }

    private Client.SORT_BY sortBy;
    private SlidingUpPanelLayout mSlideUpPanel;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof SlidingPanelActivity) {

            SlidingPanelActivity slidingPanelActivity = (SlidingPanelActivity) getActivity();
            slidingPanelActivity.setSlidingStateOnBackpressed(SlidingUpPanelLayout.PanelState.COLLAPSED);

            mSlideUpPanel = slidingPanelActivity.getSlidingPanelLayout();
            mSlideUpPanel.setAnchorPoint(0.25f);
            mSlideUpPanel.setFadeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });

        }
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
                    // Custom click handler
                    onClientSelectedListener.clientSelected(client);
                } else {
                    // Default behaviour (show client details)
                    if (getActivity() instanceof SlidingPanelActivity) {
                        SlidingPanelActivity slidingPanelActivity = (SlidingPanelActivity)getActivity();
                        slidingPanelActivity.setSlidingTitle(client.getName(), client.getFullAddress());
                        slidingPanelActivity.setSlidingContent(ClientDetailsFragment.newInstance(client));
                        mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    } else {
                        GenericToolbarActivity.start(getContext(), client.getName(), client.getFullAddress(), ClientDetailsFragment.newInstance(client));
                    }
                }
            }
        });
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
