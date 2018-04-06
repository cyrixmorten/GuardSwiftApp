package com.guardswift.persistence.parse.data.client;

import android.content.Context;
import android.databinding.ObservableField;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.guardswift.util.GSIntents;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

@ParseClassName("Client")
public class Client extends ExtendedParseObject implements Positioned {



    public static class ObservableClient {

        public final ObservableField<String> id = new ObservableField<>();
        public final ObservableField<String> name = new ObservableField<>();
        public final ObservableField<String> street = new ObservableField<>();
        public final ObservableField<String> streetNumber = new ObservableField<>();
        public final ObservableField<String> postalCode = new ObservableField<>();
        public final ObservableField<String> city = new ObservableField<>();
        public final ObservableField<ParseGeoPoint> position = new ObservableField<>();

        public ObservableClient(Client client) {
            if (client != null) {
                id.set(client.getId());
                name.set(client.getName());
                street.set(client.getStreet());
                streetNumber.set(client.getStreetNumber());
                postalCode.set(client.getPostalCode());
                city.set(client.getCity());
                position.set(client.getPosition());
            }
        }
    }

    public void updateFromObservable(ObservableClient observableClient) {

    }

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

    //    public static final String number = "number";
    public static final String position = "position";

    //    public static final String messages = "messages";
    public static final String roomLocations = "roomLocations";
    public static final String people = "people";
    public static final String contacts = "contacts";
    public static final String automatic = "automatic"; // e.g. alarm client

    public static final String fingerprints = "fingerprints";
    public static final String tasksRadius = "tasksRadius";


    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Client> getAllNetworkQuery() {
        return new ClientQueryBuilder(false).build();
    }


    public static ClientQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new ClientQueryBuilder(fromLocalDatastore);
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
            contactView.setOnClickListener(view -> GSIntents.dialPhoneNumber(context, phoneNumber));
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

    public int getRadius(ParseTask.TASK_TYPE taskType) {
        Map<String, Integer> radiusMap = getMap(Client.tasksRadius);
        String key = taskType.toString();
        if (radiusMap != null && radiusMap.containsKey(key)) {
            return radiusMap.get(key);
        }

        switch (taskType) {
            case REGULAR: return ParseTask.DEFAULT_RADIUS_REGULAR;
            case RAID: return ParseTask.DEFAULT_RADIUS_RAID;
            case ALARM: return ParseTask.DEFAULT_RADIUS_ALARM;
        }

        return 0;
    }

    public void setRadius(ParseTask.TASK_TYPE taskType, int radius) {
        Map<String, Integer> radiusMap = getMap(Client.tasksRadius);
        String key = taskType.toString();
        if (radiusMap == null) {
            radiusMap = Maps.newHashMap();
        }
        radiusMap.put(key, radius);

        put(key, radiusMap);
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

    public String getStreet() {
        return  getStringSafe(Client.street);
    }

    public String getStreetNumber() {
        return getStringSafe(Client.streetNumber);
    }

    public String getStreetWithNumber() {
        return  getStreet() + " " + getStreetNumber();
    }

    public String getPostalCode() {
        return getStringSafe(Client.postalCode);
    }

    public String getCity() {
        return getStringSafe(Client.postalCode);
    }

    @Deprecated
    public String getAddressName() {
        return has(Client.addressName) ? getString(Client.addressName) : getStringSafe(Client.street);
    }

    @Deprecated
    public String getAddressNumber() {
        return has(Client.addressNumber) ? getString(Client.addressNumber) : getStringSafe(Client.streetNumber);
    }


    @Deprecated
    public String getFullAddress() {
        return getString(fullAddress);
    }

    @Deprecated
    public String getCityName() {
        return has(Client.cityName) ? getString(Client.cityName) : getStringSafe(Client.city);
    }

    @Deprecated
    public String getZipcode() {
        return has(Client.zipcode) ? getString(Client.zipcode) : getStringSafe(Client.postalCode);
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
        return has(position) ? getParseGeoPoint(position) : new ParseGeoPoint(0,0);
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



}
