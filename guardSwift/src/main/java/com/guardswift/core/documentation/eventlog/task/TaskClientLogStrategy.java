package com.guardswift.core.documentation.eventlog.task;

import android.location.Location;

import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Adds client information to the log
 *
 * Created by cyrix on 6/7/15.
 */
public class TaskClientLogStrategy implements LogTaskStrategy {

    public static final String client = "client";
    public static final String clientName = "clientName";
    public static final String clientCity = "clientCity";
    public static final String clientZipcode = "clientZipcode";
    public static final String clientAddress = "clientAddress";
    public static final String clientAddressNumber = "clientAddressNumber";
    public static final String clientFullAddress = "clientFullAddress";
    public static final String clientDistanceMeters = "clientDistanceMeters";

    @Override
    public void log(GSTask task, ParseObject toParseObject) {

        Client client = task.getClient();

        if (client != null) {
            logClientInfo(client, toParseObject);
            logProximity(client, toParseObject);
        }
    }

    private void logClientInfo(Client client, ParseObject toParseObject) {
        toParseObject.put(TaskClientLogStrategy.client, client);
        if (client.has(Client.name))
            toParseObject.put(TaskClientLogStrategy.clientName, client.getName());
        toParseObject.put(TaskClientLogStrategy.clientAddress, client.getAddressName());
        toParseObject.put(TaskClientLogStrategy.clientAddressNumber, client.getAddressNumber());
        toParseObject.put(TaskClientLogStrategy.clientCity, client.getCityName());
        toParseObject.put(TaskClientLogStrategy.clientZipcode, client.getZipcode());
        String clientFullAddress = client.getAddressName() + " "
                + client.getAddressNumber() + " " + client.getZipcode() + " "
                + client.getCityName();
        toParseObject.put(TaskClientLogStrategy.clientFullAddress, clientFullAddress);
    }

    private void logProximity(Client client, ParseObject toParseObject) {
        Location location = LocationModule.Recent.getLastKnownLocation();
        if (client != null && location != null) {
            ParseGeoPoint clientPosition = client.getPosition();
//                double distanceKm = ParseModule.distanceBetweenKilomiters(location, clientPosition);
            float distanceMeters = ParseModule.distanceBetweenMeters(location, clientPosition);
            toParseObject.put(TaskClientLogStrategy.clientDistanceMeters, distanceMeters);
        }
    }

}
