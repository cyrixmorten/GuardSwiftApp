package com.guardswift.ui.parse.data.guard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        @BindView(R.id.client_name)
        TextView vClientName;
        @BindView(R.id.client_address)
        TextView vClientAddress;

//        @BindView(R.id.no_activity_registered)
//        TextView vNoActivity;

        public GuardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


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


        new PositionedViewHolder.CalcDistanceAsync(guard, holder).execute();
    }
}
