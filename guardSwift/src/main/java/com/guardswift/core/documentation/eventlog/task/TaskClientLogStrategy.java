package com.guardswift.core.documentation.eventlog.task;

import android.location.Location;

import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Adds client information to the log
 *
 * Created by cyrix on 6/7/15.
 */
public class TaskClientLogStrategy implements LogTaskStrategy {



    @Override
    public void log(ParseTask task, ParseObject toParseObject) {

        Client client = task.getClient();

        if (client != null) {
            logClientInfo(client, toParseObject);
            logProximity(client, toParseObject);
        }
    }

    private void logClientInfo(Client client, ParseObject toParseObject) {
        toParseObject.put(EventLog.client, ParseObject.createWithoutData(Client.class, client.getObjectId()));
        if (client.has(Client.name))
            toParseObject.put(EventLog.clientName, client.getName());
        toParseObject.put(EventLog.clientAddress, client.getAddressName());
        toParseObject.put(EventLog.clientAddressNumber, client.getAddressNumber());
        toParseObject.put(EventLog.clientCity, client.getCityName());
        toParseObject.put(EventLog.clientZipcode, client.getZipcode());
        String clientFullAddress = client.getAddressName() + " "
                + client.getAddressNumber() + " " + client.getZipcode() + " "
                + client.getCityName();
        toParseObject.put(EventLog.clientFullAddress, clientFullAddress);
    }

    private void logProximity(Client client, ParseObject toParseObject) {
        Location location = LocationModule.Recent.getLastKnownLocation();
        if (client != null && location != null) {
            ParseGeoPoint clientPosition = client.getPosition();
//                double distanceKm = ParseModule.distanceBetweenKilometers(location, position);
            float distanceMeters = ParseModule.distanceBetweenMeters(location, clientPosition);
            toParseObject.put(EventLog.clientDistanceMeters, distanceMeters);
        }
    }

}
