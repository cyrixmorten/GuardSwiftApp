package com.guardswift.ui.parse.data.tracker;

import android.os.Bundle;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class TrackerListFragment extends AbstractParseRecyclerFragment<Tracker, TrackerRecycleAdapter.TrackerViewHolder> {

    public static TrackerListFragment newInstance() {
        TrackerListFragment fragment = new TrackerListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected ExtendedParseObject getObjectInstance() {
        return new Tracker();
    }

    @Override
    protected ParseQueryAdapter.QueryFactory<Tracker> createNetworkQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Tracker>() {
            @Override
            public ParseQuery<Tracker> create() {
                return new Tracker.QueryBuilder(false).build().addDescendingOrder(Tracker.createdAt);
            }
        };
    }

    @Override
    protected ParseRecyclerQueryAdapter<Tracker, TrackerRecycleAdapter.TrackerViewHolder> createRecycleAdapter() {
        return new TrackerRecycleAdapter(getContext(), createNetworkQueryFactory());
    }

    @Override
    protected boolean isRelevantUIEvent(UpdateUIEvent ev) {
        return false;
    }
}
