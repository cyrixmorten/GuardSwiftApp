package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

/**
 * Adds client information to the log
 *
 * Created by cyrix on 6/7/15.
 */
public class TaskClientLogStrategy implements LogTaskStrategy {

    public static final String client = "client";
    public static final String contactClient = "contactClient";
    public static final String clientName = "clientName";
    public static final String clientCity = "clientCity";
    public static final String clientZipcode = "clientZipcode";
    public static final String clientAddress = "clientAddress";
    public static final String clientAddressNumber = "clientAddressNumber";
    public static final String clientFullAddress = "clientFullAddress";

    @Override
    public void log(GSTask task, ParseObject toParseObject) {

        Client client = task.getClient();

        if (client != null) {
            toParseObject.put(TaskClientLogStrategy.client, client);
            toParseObject.put(TaskClientLogStrategy.contactClient, client);
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
    }

}
