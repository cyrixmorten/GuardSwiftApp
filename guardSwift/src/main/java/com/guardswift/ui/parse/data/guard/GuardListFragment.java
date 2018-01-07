package com.guardswift.ui.parse.data.guard;

import android.location.Location;
import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.query.GuardQueryBuilder;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class GuardListFragment extends AbstractParseRecyclerFragment<Guard, GuardRecycleAdapter.GuardViewHolder> {

    public static GuardListFragment newInstance() {
        GuardListFragment fragment = new GuardListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GuardListFragment() {
    }


    @Override
    protected ParseQueryAdapter.QueryFactory<Guard> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Guard>() {
            @Override
            public ParseQuery<Guard> create() {
                return new GuardQueryBuilder(false).sortByName(true).build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Guard, GuardRecycleAdapter.GuardViewHolder> createRecycleAdapter() {
        return new GuardRecycleAdapter(createNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
