package com.guardswift.persistence.parse;

import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.documentation.gps.LocationTracker;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatch;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
import com.guardswift.persistence.parse.execution.task.regular.Circuit;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.task.statictask.StaticTask;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParseObjectFactory {

	private final Client client;
	private final Circuit circuit;
	private final ParseTask task;
	private final CircuitUnit circuitUnit;
	private final CircuitStarted circuitStarted;
	private final StaticTask staticTask;
	private final DistrictWatch districtWatch;
	private final DistrictWatchStarted districtWatchStarted;
	private final DistrictWatchClient districtWatchClient;
	private final EventLog eventLog;
	private final EventType eventType;
	private final EventRemark eventRemark;
	private final Guard guard;
    private final ClientContact clientContact;
    private final ClientLocation clientLocation;
    private final LocationTracker locationTracker;
	private final Report report;

	private final List<ExtendedParseObject> allParseObjects;


    @Inject
	public ParseObjectFactory() {
		circuit = new Circuit();
        circuitStarted = new CircuitStarted();
		task = new ParseTask();
		circuitUnit = new CircuitUnit();
		staticTask = new StaticTask();
        client = new Client();
        clientContact = new ClientContact();
        clientLocation = new ClientLocation();
        districtWatch = new DistrictWatch();
        districtWatchClient = new DistrictWatchClient();
        districtWatchStarted = new DistrictWatchStarted();
        eventLog = new EventLog();
        eventRemark = new EventRemark();
        eventType = new EventType();
        locationTracker = new LocationTracker();
//		message = new Message();
        guard = new Guard();
		report = new Report();

		allParseObjects = new ArrayList<>();
		allParseObjects.add(client);
		allParseObjects.add(circuit);
		allParseObjects.add(task);
		allParseObjects.add(circuitUnit);
		allParseObjects.add(circuitStarted);
		allParseObjects.add(districtWatch);
		allParseObjects.add(districtWatchStarted);
		allParseObjects.add(districtWatchClient);
		allParseObjects.add(eventLog);
		allParseObjects.add(eventType);
		allParseObjects.add(eventRemark);
		allParseObjects.add(guard);
        allParseObjects.add(clientContact);
        allParseObjects.add(clientLocation);
        allParseObjects.add(locationTracker);
	}

	public List<ExtendedParseObject> getAll() {
		return allParseObjects;
	}


	public Client getClient() {
		return client;
	}


	public EventRemark getEventRemark() {
		return eventRemark;
	}

	public CircuitUnit getCircuitUnit() {
		return circuitUnit;
	}


	public Circuit getCircuit() {
		return circuit;
	}

	public CircuitStarted getCircuitStarted() {
		return circuitStarted;
	}


	public DistrictWatch getDistrictWatch() {
		return districtWatch;
	}

	public DistrictWatchClient getDistrictWatchClient() {
		return districtWatchClient;
	}


	public DistrictWatchStarted getDistrictWatchStarted() {
		return districtWatchStarted;
	}

	public EventLog getEventLog() {
		return eventLog;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Guard getGuard() {
		return guard;
	}

}
