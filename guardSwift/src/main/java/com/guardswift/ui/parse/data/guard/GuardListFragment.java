package com.guardswift.ui.parse.data.guard;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.guardswift.R;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.query.GuardQueryBuilder;
import com.guardswift.ui.activity.GenericToolbarActivity;
import com.guardswift.ui.menu.MenuItemBuilder;
import com.guardswift.ui.menu.MenuItemIcons;
import com.guardswift.ui.parse.AbstractParseRecyclerFragment;
import com.guardswift.ui.parse.ParseRecyclerQueryAdapter;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.parse.ui.widget.ParseQueryAdapter;

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
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }



    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {

        new MenuItemBuilder(getContext())
                .icon(MenuItemIcons.create(getContext(), GoogleMaterial.Icon.gmd_google_maps))
                .showAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                .addToMenu(menu, R.string.map, menuItem -> {

                    GenericToolbarActivity.start(getContext(), R.string.title_drawer_guards, GuardsMapFragment.newInstance());

                    return false;
                });


        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    protected ParseQueryAdapter.QueryFactory<Guard> createNetworkQueryFactory() {
        return () -> new GuardQueryBuilder(false).sortByName(true).build();
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
