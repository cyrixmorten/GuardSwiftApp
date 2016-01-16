package com.guardswift.ui.parse.documentation.event;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by cyrix on 11/22/15.
 */
public class EventViewHolder extends RecyclerView.ViewHolder {


        public EventViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

}
