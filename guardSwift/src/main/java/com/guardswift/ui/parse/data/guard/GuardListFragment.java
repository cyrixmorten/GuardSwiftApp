package com.guardswift.ui.parse.data.guard;

import android.location.Location;
import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class GuardListFragment extends AbstractParseRecyclerFragment<Guard, GuardRecycleAdapter.GuardViewHolder> {

    public static GuardListFragment newInstance() {
        GuardListFragment fragment = new GuardListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new Guard();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<Guard> getNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Guard>() {
            @Override
            public ParseQuery<Guard> create() {
                return new Guard.QueryBuilder(false).build();
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Guard, GuardRecycleAdapter.GuardViewHolder> getRecycleAdapter() {
        return new GuardRecycleAdapter(getNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return ev.getObject() instanceof Location;
    }
}
