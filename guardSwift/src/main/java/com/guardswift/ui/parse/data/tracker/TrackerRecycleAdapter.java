package com.guardswift.ui.parse.data.tracker;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.ViewGroup;

import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.execution.TaskRecycleAdapter;
import com.guardswift.ui.view.card.TrackerCard;
import com.parse.ui.widget.ParseQueryAdapter;


public class TrackerRecycleAdapter extends ParseRecyclerQueryAdapter<Tracker, TrackerRecycleAdapter.TrackerViewHolder> {

    private static final String TAG = TaskRecycleAdapter.class.getSimpleName();

    static class TrackerViewHolder extends RecyclerView.ViewHolder {

        TrackerCard trackerCard;

        TrackerViewHolder(TrackerCard trackerCard) {
            super(trackerCard);

            this.trackerCard = trackerCard;
        }

    }

    TrackerRecycleAdapter(Context context, ParseQueryAdapter.QueryFactory<Tracker> queryFactory) {
        super(queryFactory);
        this.context = context;
    }

    @Override
    public TrackerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TrackerCard trackerCard = new TrackerCard(context);
        return new TrackerViewHolder(trackerCard);
    }


    @Override
    public void onBindViewHolder(TrackerViewHolder holder, int position) {
        final Tracker tracker = getItem(position);
        holder.trackerCard.setTracker(tracker);

        holder.trackerCard.setOnClick(view -> GenericToolbarActivity.start(context, DateFormat.getLongDateFormat(context).format(tracker.getDateStart()), tracker.getGuardName(), TrackerMapFragment.newInstance(tracker)));

    }
}
