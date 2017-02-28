package com.guardswift.persistence.parse.data.client;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.util.GSIntents;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import dk.alexandra.positioning.wifi.Fingerprint;
import dk.alexandra.positioning.wifi.io.WiFiIO;

@ParseClassName("Client")
public class Client extends ExtendedParseObject implements Positioned {


    public static final String clientId = "clientId";
    public static final String name = "name";

    // since version 4.0.0
    public static final String street = "street";
    public static final String streetNumber = "streetNumber";
    public static final String postalCode = "postalCode";
    public static final String city = "city";
    public static final String formattedAddress = "formattedAddress";
    // <--

    // dreprecated since version 4.0.0
    public static final String addressName = "addressName";
    public static final String addressNumber = "addressNumber";
    public static final String fullAddress = "fullAddress";
    public static final String cityName = "cityName";
    public static final String zipcode = "zipcode";
    // <--

    public static final String email = "email";
    //    public static final String number = "number";
    public static final String position = "position";

    //    public static final String messages = "messages";
    public static final String roomLocations = "roomLocations";
    public static final String people = "people";
    public static final String contacts = "contacts";
    public static final String automatic = "automatic"; // e.g. alarm client

    public static final String fingerprints = "fingerprints";
    public static final String tasksRadius = "tasksRadius";


    @Override
    public String getParseClassName() {
        return Client.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Client> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public void storeFingerprints(Set<Fingerprint> fingerprints) {
        JSONArray jsonArray = new JSONArray();
        for (Fingerprint fingerprint : fingerprints) {
            try {
                String jsonString = WiFiIO.convertToJSON(fingerprint);
                JSONObject jsonObject = new JSONObject(jsonString);

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "storeFingerprints", e);
            }
        }
        put(Client.fingerprints, jsonArray);
    }

    public ClientLocation findCheckpoint(String name) {
        List<ClientLocation> checkpoints = getCheckpoints();
        for (ClientLocation checkpoint : checkpoints) {
            if (checkpoint.getLocation().equals(name)) {
                return checkpoint;
            }
        }
        return null;
    }

    public LinearLayout createContactsList(final Context context) {
        final List<ClientContact> contacts = getContactsWithNames();

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater li = LayoutInflater.from(context);

        for (final ClientContact contact : contacts) {
            View contactView = li.inflate(R.layout.gs_view_clientcontact, null);
            TextView name = ButterKnife.findById(contactView, R.id.tvName);
            TextView phone = ButterKnife.findById(contactView, R.id.tvPhoneNumber);
            TextView desc = ButterKnife.findById(contactView, R.id.tvDescription);
            TextView email = ButterKnife.findById(contactView, R.id.tvEmail);

            name.setVisibility((contact.getName().isEmpty()) ? View.INVISIBLE : View.VISIBLE);
            name.setText(contact.getName());

            phone.setVisibility((contact.getPhoneNumber().isEmpty()) ? View.INVISIBLE : View.VISIBLE);
            phone.setText(contact.getPhoneNumber());

            desc.setVisibility((contact.getDesc().isEmpty()) ? View.INVISIBLE : View.VISIBLE);
            desc.setText(contact.getDesc());

            email.setVisibility((contact.getEmail().isEmpty()) ? View.INVISIBLE : View.VISIBLE);
            email.setText(contact.getEmail());


            final String phoneNumber = contact.getPhoneNumber();
            contactView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GSIntents.dialPhoneNumber(context, phoneNumber);
                }
            });
            layout.addView(contactView);
        }
        return layout;
    }

    public List<String> getContactsRequestingReportEmails() {
        List<String> emails = Lists.newArrayList();
        for (ClientContact clientContact : getContactsRequestingReport()) {
            emails.add(clientContact.getEmail());
        }
        return emails;
    }

    public int getRadius(GSTask.TASK_TYPE taskType) {
        Map<String, Integer> radiusMap = getMap(Client.tasksRadius);
        String key = taskType.toString();
        if (radiusMap != null && radiusMap.containsKey(key)) {
            return radiusMap.get(key);
        }

        switch (taskType) {
            case REGULAR: return GSTask.DEFAULT_RADIUS_REGULAR;
            case RAID: return GSTask.DEFAULT_RADIUS_RAID;
            case ALARM: return GSTask.DEFAULT_RADIUS_ALARM;
        }

        return 0;
    }

    public void setRadius(GSTask.TASK_TYPE taskType, int radius) {
        Map<String, Integer> radiusMap = getMap(Client.tasksRadius);
        String key = taskType.toString();
        if (radiusMap == null) {
            radiusMap = Maps.newHashMap();
        }
        radiusMap.put(key, radius);

        put(key, radiusMap);
    }

    public enum SORT_BY {NAME, DISTANCE}

    public static class QueryBuilder extends ParseQueryBuilder<Client> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(Client.class));
        }

        @Override
        public ParseQuery<Client> build() {
            query.setLimit(1000);
            query.include(roomLocations);
            query.include(people);
            return super.build();
        }

        public QueryBuilder matching(Client client) {
            query.whereEqualTo(objectId, client.getObjectId());
            return this;
        }

        public QueryBuilder sort(SORT_BY sortBy) {
            if (sortBy == SORT_BY.NAME) {
                return sortByName();
            }
            if (sortBy == SORT_BY.DISTANCE) {
                return sortByDistance();
            }

            return this;
        }

        public QueryBuilder notAutomatic() {
            query.whereNotEqualTo(Client.automatic, true);
            return this;
        }

        QueryBuilder sortByName() {
            query.orderByAscending(Client.name);
            return this;
        }

        QueryBuilder sortByDistance() {
            ParseModule.sortNearest(query,
                    Client.position);
            return this;
        }
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    public String getName() {
        return getString(name);
    }

    public String getIdAndName() {
        if (getId() == null) {
            return getId() + " " + getName();
        }
        return getName();
    }

    public String getAddressName() {
        return has(Client.addressName) ? getString(Client.addressName) : getStringSafe(Client.street);
    }


    public String getAddressNumber() {
        return has(Client.addressNumber) ? getString(Client.addressNumber) : getStringSafe(Client.streetNumber);
    }

    public String getFullAddress() {
        return getString(fullAddress);
    }

    public String getCityName() {
        return has(Client.cityName) ? getString(Client.cityName) : getStringSafe(Client.city);
    }

    public String getZipcode() {
        return has(Client.zipcode) ? getString(Client.zipcode) : getStringSafe(Client.postalCode);
    }

    public String getEmail() {
        return getString(email);
    }


    public String getId() {
        return (has(clientId)) ? getString(clientId) : "";
    }


    public List<Person> getPeople() {
        if (has(people)) return getList(people);
        return Lists.newArrayList();
    }

    public void addPerson(Person person) {
        add(people, person);
    }

    public void removePeople(Person... people) {
        removeAll(Client.people, new ArrayList<Person>(Arrays.asList(people)));
        for (Person person : people) {
            person.deleteEventually();
        }
    }

    public List<ClientLocation> getLocations() {
        if (has(roomLocations)) return getList(roomLocations);
        return Lists.newArrayList();
    }

    public void addLocation(ClientLocation location) {
        add(roomLocations, location);
    }

    public void removeLocations(ClientLocation... locations) {
        removeAll(roomLocations, new ArrayList<ClientLocation>(Arrays.asList(locations)));
        for (ClientLocation location : locations) {
            location.deleteEventually();
        }
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(position);
    }

    public List<ClientContact> getContactsWithNames() {
        if (has(contacts)) {
            List<ClientContact> allContacts = getList(contacts);
            List<ClientContact> contacts = Lists.newArrayList();
            for (ClientContact contact : allContacts) {
                if (!contact.getName().isEmpty()) {
                    contacts.add(contact);
                }
            }
            return contacts;
        }
        return new ArrayList<>();
    }

    public List<ClientContact> getContactsRequestingReport() {
        if (has(contacts)) {
            List<ClientContact> allContacts = getList(contacts);
            List<ClientContact> contacts = Lists.newArrayList();
            for (ClientContact contact : allContacts) {
                if (!contact.getEmail().isEmpty() && contact.isReceivingReports()) {
                    contacts.add(contact);
                }
            }
            return contacts;
        }
        return new ArrayList<>();
    }


    // TODO disabled checkpoints to investigate performance issue
    public boolean hasCheckPoints() {
        return false;
//        for (ClientLocation location : getLocations()) {
//            if (location.isCheckpoint()) {
//                return true;
//            }
//        }
//        return false;
    }

    public void clearCheckpoints() {
        List<ClientLocation> checkpoints = getCheckpoints();
        for (ClientLocation checkpoint : checkpoints) {
            checkpoint.reset();
        }
        ParseObject.pinAllInBackground(checkpoints);
    }

    public List<ClientLocation> getCheckpoints() {
        List<ClientLocation> allLocations = getLocations();
        List<ClientLocation> checkpoints = new ArrayList<>();
        for (ClientLocation location : allLocations) {
            if (location.isCheckpoint()) {
                checkpoints.add(location);
            }
        }
        Collections.sort(checkpoints);
        return checkpoints;
    }

    public List<String> getCheckpointNamesAsList() {
        List<String> checkpointNames = new ArrayList<>();
        List<ClientLocation> checkpoints = getCheckpoints();
        for (ClientLocation location : checkpoints) {
            if (location.isCheckpoint()) {
                checkpointNames.add(location.getLocation());
            }
        }
        return checkpointNames;
    }

    public String[] getCheckpointsNamesAsArray() {
        List<String> checkpoints = getCheckpointNamesAsList();
        return checkpoints.toArray(new String[checkpoints.size()]);
    }


    public boolean[] getCheckpointsCheckedArray() {

        List<ClientLocation> checkpoints = getCheckpoints();
        boolean[] checked = new boolean[checkpoints.size()];


        for (int i = 0; i < checkpoints.size(); i++) {
            boolean isChecked = checkpoints.get(i).isChecked();
            checked[i] = isChecked;
        }

        return checked;
    }


}
