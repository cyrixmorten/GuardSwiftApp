package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.dagger.InjectingListFragment;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.Person;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.adapters.SimpleMultichoiceArrayAdapter;
import com.guardswift.ui.common.UpdateFloatingActionButton;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.documentation.report.create.activity.AddEventHandler;
import com.guardswift.util.Device;
import com.guardswift.util.StringUtil;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class AddEventPeopleFragment extends InjectingListFragment implements EventEntryFragment, UpdateFloatingActionButton {

    protected static final String TAG = AddEventPeopleFragment.class
            .getSimpleName();

    public static AddEventPeopleFragment newInstance(Client client) {

        GuardSwiftApplication.getInstance().getCacheFactory().getClientCache().setSelected(client);

        AddEventPeopleFragment fragment = new AddEventPeopleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddEventPeopleFragment() {
    }

    @Inject
    ClientCache clientCache;
    @Inject
    Device device;

    private Client mClient;
    private SimpleMultichoiceArrayAdapter mAdapter;

    private List<Person> people;
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

        people = mClient.getPeople();

        Collections.sort(people);

        all_options.clear();
        for (Person clientLocationObject : people) {
            all_options.add(clientLocationObject.getName());
        }

        resetSelections();
        mAdapter.notifyDataSetInvalidated();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.gs_listview_selectable_fab, container,
                false);

        ButterKnife.bind(this, rootView);

        setListAdapter(mAdapter);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        new AddEntryMenuItem().create(getContext(), menu, new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                addPerson();
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
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
                                           final int people_index, long arg3) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.action)
                        .items(R.array.edit_delete)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        performEdit(people_index);
                                        break;
                                    case 1:
                                        performDelete(people_index);
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

    private void performEdit(final int people_index) {
        final Person person = people.get(people_index);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.edit)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(null, person.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editTextString = input.toString();
                        if (!editTextString.isEmpty()) {
                            person.setName(editTextString);
                            saveClient();
                        }
                    }
                }).negativeText(android.R.string.cancel).show();
    }

    private void performDelete(final int people_index) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete)
                .content(getString(R.string.confirm_delete, people.get(people_index).getName()))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mClient.removePeople(people.get(people_index));
                        saveClient();
                    }
                })
                .show();
    }

    public void addPerson() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.add_person)
                .content(R.string.new_person_example)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.new_person, R.string.input_empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editTextString = input.toString();
                        if (!editTextString.isEmpty()) {
                            Person person = Person.create(editTextString.trim());
                            mClient.addPerson(person);
                            saveClient();
                        }
                    }
                }).negativeText(android.R.string.cancel).show();
    }

    private void saveClient() {
        if (!device.isOnline()) {
            new CommonDialogsBuilder.MaterialDialogs(getActivity()).missingInternetContent().show();
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
        String people = "";

        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (checked.get(i)) {
                people += all_options.get(i) + ", ";
            }
        }
        people = StringUtil.removeLast(people, ",").trim();

        ((AddEventHandler) getActivity()).setPeople(people);
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

        String selectedLocations = ((AddEventHandler) getActivity()).getPeople();

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
        return R.string.title_people;
    }

    @Override
    public void updateFloatingActionButton(Context context, FloatingActionButton floatingActionButton) {
        floatingActionButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_person_add_white_18dp));
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPerson();
            }
        });
        floatingActionButton.show();
    }
}