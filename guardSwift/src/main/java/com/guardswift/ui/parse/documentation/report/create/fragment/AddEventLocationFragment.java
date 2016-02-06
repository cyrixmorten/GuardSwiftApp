package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.guardswift.R;
import com.guardswift.dagger.InjectingListFragment;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.adapters.SimpleMultichoiceArrayAdapter;
import com.guardswift.ui.common.UpdateFloatingActionButton;
import com.guardswift.ui.parse.documentation.report.create.activity.AddEventHandler;
import com.guardswift.util.Device;
import com.guardswift.util.StringUtil;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddEventLocationFragment extends InjectingListFragment implements EventEntryFragment, UpdateFloatingActionButton {

    protected static final String TAG = AddEventLocationFragment.class
            .getSimpleName();

    public static AddEventLocationFragment newInstance(Client client) {

        GuardSwiftApplication.getInstance().getCacheFactory().getClientCache().setSelected(client);

        AddEventLocationFragment fragment = new AddEventLocationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddEventLocationFragment() {
    }

    @Inject
    ClientCache clientCache;
    @Inject
    Device device;

    private Client mClient;
    private SimpleMultichoiceArrayAdapter mAdapter;

    private List<ClientLocation> locations;
    private List<String> all_options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mClient = clientCache.getSelected();


        all_options = new ArrayList<String>();

        mAdapter = new SimpleMultichoiceArrayAdapter(getActivity(), all_options, new boolean[]{});

        super.onCreate(savedInstanceState);
    }

    private void updateLocations() {
        if (getActivity() == null || !isAdded() || mAdapter == null) {
            return;
        }

        locations = mClient.getLocations();

        Collections.sort(locations);

        all_options.clear();
        for (ClientLocation clientLocationObject : locations) {
            all_options.add(clientLocationObject.getLocation());
        }

        resetSelections();
        mAdapter.notifyDataSetInvalidated();
    }

    @Bind(R.id.btn_footer)
    FloatingActionButton footerButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.gs_listview_selectable_fab, container,
                false);

        ButterKnife.bind(this, rootView);

        setListAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        updateLocations();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int locations_index, long arg3) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.action)
                        .items(R.array.edit_delete)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                if (locations.get(locations_index).isCheckpoint()) {
                                    Toast.makeText(getActivity(), getString(R.string.error_cannot_edit_or_delete_checkpoint), Toast.LENGTH_SHORT).show();
                                    return true;
                                }

                                switch (which) {
                                    case 0:
                                        performEdit(locations_index);
                                        break;
                                    case 1:
                                        performDelete(locations_index);
                                        break;
                                }
                                return true;
                            }
                        })
                        .alwaysCallSingleChoiceCallback()
                        .show();
                return true;
            }
        });
    }

    private void performEdit(final int locations_index) {
        final ClientLocation clientLocation = locations.get(locations_index);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.edit)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(null, clientLocation.getLocation(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editTextString = input.toString();
                        if (!editTextString.isEmpty()) {
                            clientLocation.setName(editTextString);
                            saveClient();
                        }
                    }
                }).negativeText(android.R.string.cancel).show();
    }

    private void performDelete(final int locations_index) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete)
                .content(getString(R.string.confirm_delete, locations.get(locations_index).getLocation()))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mClient.removeLocations(locations.get(locations_index));
                        saveClient();
                    }
                })
                .show();
    }

    @OnClick(R.id.btn_footer)
    public void addLocation() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.add_client_location)
                .content(R.string.new_client_location_example)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.new_client_location, R.string.input_empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editTextString = input.toString();
                        if (!editTextString.isEmpty()) {
                            ClientLocation clientLocation = ClientLocation.create(editTextString.trim(), false);
                            mClient.addLocation(clientLocation);
                            saveClient();
                        }
                    }
                }).negativeText(android.R.string.cancel).show();
    }

    private void saveClient() {
        if (!device.isOnline()) {
            Toast.makeText(getActivity(), getString(R.string.message_no_internet_connection), Toast.LENGTH_LONG).show();
            return;
        }


        mClient.pinThenSaveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), getString(R.string.error_an_error_occured), Toast.LENGTH_LONG).show();
                    return;
                }

                updateLocations();
            }
        });

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        updateSelections();

        super.onListItemClick(l, v, position, id);
    }

    private void updateSelections() {
        String locations = "";

        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (checked.get(i)) {
                locations += all_options.get(i) + ", ";
            }
        }
        locations = StringUtil.removeLast(locations, ",").trim();

        ((AddEventHandler) getActivity()).setLocations(locations);
    }

    private void resetSelections() {
        Log.e(TAG, "resetSelections");

        if (getListView() == null)
            return;

        for (int i = 0; i < getListView().getCount(); i++) {
            getListView().setItemChecked(i, false);
        }

        loadPreselected();
    }

    private void loadPreselected() {
        Log.d(TAG, "loadPreselected");

        String selectedLocations = ((AddEventHandler) getActivity()).getLocations();

        if (selectedLocations == null)
            return;

        String[] splitLocations = selectedLocations.split(",");
        for (int i = 0; i < splitLocations.length; i++) {
            splitLocations[i] = splitLocations[i].trim();
        }


        for (int i = 0; i < all_options.size(); i++) {

            String currentOption = all_options.get(i).trim();

            for (int j = 0; j < splitLocations.length; j++) {
                Log.d(TAG, "Comparing " + splitLocations[j] + " == " + currentOption + " : " + splitLocations[j].equals(currentOption));
                if (splitLocations[j].equals(currentOption)) {
                    getListView().setItemChecked(i, true);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void fragmentBecameVisible() {

    }

    @Override
    public void fragmentBecameInvisible() {

    }

    @Override
    public int getTitle() {
        return R.string.title_event_location;
    }

    @Override
    public void updateFloatingActionButton(Context context, android.support.design.widget.FloatingActionButton
            floatingActionButton) {
        floatingActionButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_add_location_white_18dp));
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLocation();
            }
        });
        floatingActionButton.show();
    }
}