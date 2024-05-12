package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.guardswift.R;
import com.guardswift.dagger.InjectingListFragment;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.query.EventTypeQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.helpers.UpdateFloatingActionButton;
import com.guardswift.ui.menu.MenuItemBuilder;
import com.guardswift.ui.menu.MenuItemIcons;
import com.guardswift.ui.parse.documentation.report.create.activity.AddEventHandler;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.parse.ParseUser;
import com.parse.ui.widget.ParseQueryAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddEventTypeFragment extends InjectingListFragment implements EventEntryFragment, UpdateFloatingActionButton {

    protected static final String TAG = AddEventTypeFragment.class
            .getSimpleName();

    public static AddEventTypeFragment newInstance(Client client) {

        GuardSwiftApplication.getInstance().getCacheFactory().getClientCache().setSelected(client);

        AddEventTypeFragment fragment = new AddEventTypeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddEventTypeFragment() {
    }


    @Inject
    ClientCache clientCache;
    @Inject
    EventTypeCache eventTypeCache;

    private EventTypeParseAdapter mAdapter;

    //    @BindView(R.id.header) TextView tv_header;
    @BindView(R.id.btn_footer)
    FloatingActionButton footerButton;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // disableCroutons();
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gs_listview_selectable_fab,
                container, false);

        unbinder = ButterKnife.bind(this, rootView);

        // mAdapter = new AddEventTypeAdapter(getActivity());
        mAdapter = new EventTypeParseAdapter(getActivity(),
                () -> new EventTypeQueryBuilder(false)
                        .matchingIncludes(clientCache.getSelected())
                        .sortByTimesUsed()
                        .build());

        mAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<EventType>() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List<EventType> eventTypes, Exception e) {

                if (getActivity() == null || !isAdded())
                    return;

                loadPreselected();

                mAdapter.removeOnQueryLoadListener(this);
            }
        });

        setListAdapter(mAdapter);

//		tv_header.setText(getString(R.string.event_observed).toUpperCase(
//				Locale.getDefault()));

//        footerButton.setText(getString(R.string.add_event_type));

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        new MenuItemBuilder(getContext())
                .icon(MenuItemIcons.create(getContext(), FontAwesome.Icon.faw_plus_circle))
                .showAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                .addToMenu(menu, R.string.add_new, menuItem -> {
                    addEventType();
                    return false;
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private AddEventHandler getEventHandler() {
        if (getActivity() != null && getActivity() instanceof AddEventHandler) {
            return ((AddEventHandler) getActivity());
        }

        return null;
    }

    private void loadPreselected() {

        if (getEventHandler() != null) {
            String selectedEvent = getEventHandler().getEventType();

            if (selectedEvent == null)
                return;

            for (int i = 0; i < getListView().getCount(); i++) {
                EventType eventType = (EventType) getListView().getItemAtPosition(i);

                if (eventType != null && eventType.getName().equals(selectedEvent)) {
                    getListView().setItemChecked(i, true);
                    eventTypeCache.setSelected(eventType);
                    return;
                }
            }
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        EventType event = mAdapter.getItem(position);

        selectEvent(event);

        super.onListItemClick(l, v, position, id);
    }

    private void selectEvent(EventType eventType) {
        if (getActivity() == null) {
            return;
        }

        eventType.increment(EventType.timesUsed);
        eventType.saveEventually();

        eventTypeCache.setSelected(eventType);

        // jump to next if amount is not needed
        if (getEventHandler() != null) {
            getEventHandler().setEventType(eventType);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_footer)
    public void addEventType() {
        if (getActivity() != null) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.add_event_type)
                    .content(R.string.add_event_type_desc)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(R.string.event, R.string.input_empty, (dialog, input) -> {
                        if (input.length() > 0) {
                            final EventType new_type = new EventType();
                            new_type.setClient(clientCache.getSelected());
                            new_type.setOwner(ParseUser.getCurrentUser());
                            new_type.setName(input.toString());
                            new_type.pinInBackground(e -> {
                                selectEvent(new_type);
                                if (mAdapter != null) {
                                    mAdapter.loadObjects();
                                }
                            });


                        }
                    }).negativeText(android.R.string.cancel).show();
        }
    }

    @Override
    public void fragmentBecameVisible() {

    }

    @Override
    public void fragmentBecameInvisible() {

    }

    @Override
    public int getTitle() {
        return R.string.title_event_type;
    }

    @Override
    public void updateFloatingActionButton(Context context, com.google.android.material.floatingactionbutton.FloatingActionButton floatingActionButton) {
//        floatingActionButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_add_white_18dp));
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                addEventType();
//            }
//        });
//        floatingActionButton.show();
    }


}