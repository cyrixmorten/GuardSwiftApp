package com.guardswift.persistence.parse.data.client;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.MessagesHolder;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.Message;
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
import java.util.Set;

import dk.alexandra.positioning.wifi.Fingerprint;
import dk.alexandra.positioning.wifi.io.WiFiIO;

@ParseClassName("Client")
public class Client extends ExtendedParseObject implements Positioned, MessagesHolder {

//    public static class Recent {
//
//        private static String TAG = "Client.Recent";
//
//        private static Client selected;
//        private static Client arrived;
//
//        public static Client getSelected() {
//            return selected;
//        }
//
//        public static void setSelected(Client selected) {
//            Recent.selected = selected;
//        }
//
//        public static Client getArrived() {
//            return arrived;
//        }
//
//        public static void setArrived(Client arrived) {
//            Recent.arrived = arrived;
//
//            Log.d(TAG, "Set arrived: " + arrived);
//        }
//    }

	public static final String PIN = "Client";

	public static final String name = "name";
	public static final String addressName = "addressName";
	public static final String addressName2 = "addressName2";
	public static final String addressNumber = "addressNumber";
	public static final String fullAddress = "fullAddress";
	public static final String cityName = "cityName";
	public static final String zipcode = "zipcode";
	public static final String email = "email";
	public static final String number = "number";
	public static final String position = "position";

    public static final String messages = "messages";
    public static final String roomLocations = "roomLocations";
    public static final String people = "people";
    public static final String contacts = "contacts";

    public static final String fingerprints = "fingerprints";


	@Override
	public String getPin() {
		return PIN;
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

//        ArrayList<Fingerprint> listFingerprints = Lists.newArrayList(fingerprints);
//        String jsonString = WiFiIO.convertToJSON(listFingerprints);
//        try {
//            put(Client.fingerprints, new JSONObject(jsonString));
//        } catch (JSONException e) {
//            Log.e(TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }

        JSONArray jsonArray = new JSONArray();
        for (Fingerprint fingerprint: fingerprints) {
            try {
                String jsonString = WiFiIO.convertToJSON(fingerprint);
                JSONObject jsonObject = new JSONObject(jsonString);
//                jsonObject.put("id", fingerprint.getId().toString());
//                jsonObject.put("averageSignalStrengths", fingerprint.getAverageSignalStrengths());
//                jsonObject.put("standardDeviations", fingerprint.getStandardDeviations());
//
//                Coordinates coordinates = fingerprint.getCoordinates();
//                JSONObject jsonCoordinates = new JSONObject();
//                jsonCoordinates.put("x", coordinates.getX());
//                jsonCoordinates.put("y", coordinates.getY());
//                jsonCoordinates.put("z", coordinates.getZ());
//                jsonCoordinates.put("symbolic", coordinates.getSymbolic());
//                jsonObject.put("coordinates", jsonCoordinates);

                jsonArray.put(jsonObject);



//                ClientLocation checkpoint = findCheckpoint(fingerprint.getCoordinates().getSymbolic());
//                if (checkpoint != null) {
//                    checkpoint.setFingerprint(jsonObject);
//                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "storeFingerprints", e);
            }
        }
        put(Client.fingerprints, jsonArray);
    }

    public ClientLocation findCheckpoint(String name) {
        List<ClientLocation> checkpoints = getCheckpoints();
        for (ClientLocation checkpoint: checkpoints) {
            if (checkpoint.getLocation().equals(name)) {
                return checkpoint;
            }
        }
        return null;
    }

    public static class QueryBuilder extends ParseQueryBuilder<Client> {

		public QueryBuilder(boolean fromLocalDatastore) {
			super(PIN, fromLocalDatastore, ParseQuery.getQuery(Client.class));
		}

		@Override
		public ParseQuery<Client> build() {
			query.setLimit(1000);
            query.include(messages);
            query.include(contacts);
            query.include(roomLocations);
            query.include(people);
			return super.build();
		}

		public QueryBuilder matching(Client client) {
			query.whereEqualTo(objectId, client.getObjectId());
			return this;
		}

        public QueryBuilder sortByDistance() {
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

	public String getAddressName() {
		return getString(addressName);
	}

	public String getAddressName2() {
		return getString(addressName2);
	}

	public String getAddressNumber() {
		return getString(addressNumber);
	}

	public String getFullAddress() {
		return getString(fullAddress);
	}

	public String getCityName() {
		return getString(cityName);
	}

	public String getZipcode() {
		return getString(zipcode);
	}

	public String getEmail() {
		return getString(email);
	}

    public String getNumberString() {
        return (getNumber() != 0) ? String.valueOf(getNumber()) : "";
    }

	public int getNumber() {
		return getInt(number);
	}

    public boolean hasUnreadMessagesFor(Guard guard) {
        for (Message info: getMessages()) {
            if (!info.isReadBy(guard)) {
                return true;
            }
        }
        return false;
    }

    public List<Message> getMessages() {
        if (has(messages)) return getList(messages);
        return new ArrayList<Message>();
    }

    public void addMessage(Message message) {
        add(Client.messages, message);
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
        for (Person person: people) {
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
        for (ClientLocation location: locations) {
            location.deleteEventually();
        }
    }

	public ParseGeoPoint getPosition() {
		return getParseGeoPoint(position);
	}

    public List<ClientContact> getContactsWithNames() {
        if (has(contacts))  {
            List<ClientContact> allContacts = getList(contacts);
            List<ClientContact> contacts = Lists.newArrayList();
            for (ClientContact contact: allContacts) {
                if (!contact.getName().isEmpty()) {
                    contacts.add(contact);
                }
            }
            return contacts;
        }
        return new ArrayList<>();
    };

    @Override
    public ExtendedParseObject getParseObject() {
        return this;
    }

    public boolean hasCheckPoints() {
        for (ClientLocation location : getLocations()) {
            if (location.isCheckpoint()) {
                return true;
            }
        }
        return false;
    }

    public void clearCheckpoints() {
        List<ClientLocation> checkpoints = getCheckpoints();
        for (ClientLocation checkpoint: checkpoints) {
            checkpoint.reset();
        }
        ParseObject.pinAllInBackground(ClientLocation.PIN, checkpoints);
    }

    public List<ClientLocation> getCheckpoints() {
        List<ClientLocation> allLocations = getLocations();
        List<ClientLocation> checkpoints = new ArrayList<>();
        for (ClientLocation location: allLocations) {
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

//    public boolean hasLargeDistanceToDevice() {
//
//        if (LocationModule.Recent.getLastKnownLocation() == null)
//            return false;
//
//        float distance = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), getPosition());
//        if (distance < TaskController.MAX_DISTANCE_METERS) {
//            return false;
//        }
//        return true;
//    }



}
