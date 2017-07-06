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
        TextView tvName;
        @BindView(R.id.online_status)
        AwesomeTextView tvOnlineStatus; // isOnline


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
        holder.tvName.setText(guard.getName());
        if (guard.isOnline()) {
            holder.tvOnlineStatus.setText(context.getString(R.string.online));
            holder.tvOnlineStatus.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
        } else {
            holder.tvOnlineStatus.setText(context.getString(R.string.offline));
            holder.tvOnlineStatus.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
        }


        new PositionedViewHolder.CalcDistanceAsync(guard, holder).execute();
    }
}
