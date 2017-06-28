package com.guardswift.ui.parse.data.guard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.guardswift.ui.parse.PositionedViewHolder;
import com.parse.ParseQueryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GuardRecycleAdapter extends ParseRecyclerQueryAdapter<Guard, GuardRecycleAdapter.GuardViewHolder> {

    public static class GuardViewHolder extends PositionedViewHolder {


        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.onlinestatus)
        AwesomeTextView onlinestatus; // isOnline

        @BindView(R.id.layout_last_seen)
        LinearLayout vLayoutLastSeen;
//        @BindView(R.id.geocoded_address)
//        TextView vGeoCodedAddress; // lastGeocodedAddress
        @BindView(R.id.time)
        TextView vTime; // lastLocationUpdate

//        @BindView(R.id.layout_recent_event)
//        LinearLayout vLayoutRecentActivity;
        @BindView(R.id.event)
        TextView vEvent; // lastEvent
        @BindView(R.id.client_name)
        TextView vClientName;
        @BindView(R.id.client_address)
        TextView vClientAddress;

//        @BindView(R.id.no_activity_registered)
//        TextView vNoActivity;

        public GuardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int recentActivityVisibility = (vLayoutRecentActivity.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
//                    vLayoutRecentActivity.setVisibility(recentActivityVisibility);
//                }
//            });
        }

    }

    private Context context;

    public GuardRecycleAdapter(ParseQueryAdapter.QueryFactory<Guard> queryFactory) {
        super(queryFactory);
        this.context = GuardSwiftApplication.getInstance();
    }

    @Override
    public GuardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.gs_card_guard, parent, false);
        return new GuardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final GuardViewHolder holder, int position) {
        Guard guard = getItem(position);
        holder.name.setText(guard.getName());
        if (guard.isOnline()) {
            holder.onlinestatus.setText(context.getString(R.string.online));
            holder.onlinestatus.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
        } else {
            holder.onlinestatus.setText(context.getString(R.string.offline));
            holder.onlinestatus.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
        }

        /**
         * LAST SEEN
         */

//        holder.vLayoutLastSeen.setVisibility(View.GONE);
//
//        holder.vGeoCodedAddress.setVisibility(View.GONE);
//        GeocodedAddress geocodedAddress = guard.getLastGeocodedAddress();
//        if (geocodedAddress.hasData()) {
//            holder.vLayoutLastSeen.setVisibility(View.VISIBLE);
//            holder.vGeoCodedAddress.setVisibility(View.VISIBLE);
//            holder.vGeoCodedAddress.setText(geocodedAddress.getFullAddress());
//        }
//
//        holder.vTime.setVisibility(View.GONE);
//        Date lastLocationUpdate = guard.getLastLocationUpdate();
//        if (lastLocationUpdate != null) {
//            holder.vLayoutLastSeen.setVisibility(View.VISIBLE);
//            holder.vTime.setVisibility(View.VISIBLE);
//            String dateString = DateFormat.getDateFormat(GuardSwiftApplication.getInstance()).format(lastLocationUpdate);
//            String timeString = DateFormat.getTimeFormat(GuardSwiftApplication.getInstance()).format(lastLocationUpdate);
//            String dateTimeString = dateString + " " + timeString;
//            holder.vTime.setText(dateTimeString);
//        }
//
//        /**
//         * RECENT ACTIVITY
//         */
//
//        holder.vLayoutRecentActivity.setVisibility(View.GONE);
//
//        holder.vNoActivity.setVisibility(View.GONE);
//        holder.vClientName.setVisibility(View.GONE);
//        holder.vClientAddress.setVisibility(View.GONE);
//
//        EventLog lastEvent = guard.getLastEvent();
//        if (lastEvent != null) {
//            holder.vEvent.setText(lastEvent.getEvent());
//
//            lastEvent.getClientInBackground().onSuccess(new Continuation<Client, Object>() {
//                @Override
//                public Object then(Task<Client> task) throws Exception {
//
//                    Client client = task.getResult();
//                    holder.vClientName.setText(client.getIdAndName());
//                    holder.vClientAddress.setText(client.getFullAddress());
//
//                    holder.vClientName.setVisibility(View.VISIBLE);
//                    holder.vClientAddress.setVisibility(View.VISIBLE);
//
//                    return null;
//                }
//            });
//        } else {
//            holder.vNoActivity.setVisibility(View.VISIBLE);
//        }
        new PositionedViewHolder.CalcDistanceAsync(guard, holder).execute();
    }
}
