package com.guardswift.persistence.parse;

import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitEnding;
import com.guardswift.persistence.parse.data.checklist.ChecklistCircuitStarting;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchUnit;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.Message;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.gps.LocationTracker;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.parse.execution.regular.CircuitUnit;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchClient;
import com.guardswift.persistence.parse.execution.regular.Circuit;
import com.guardswift.persistence.parse.execution.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatch;
import com.guardswift.persistence.parse.execution.districtwatch.DistrictWatchStarted;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParseObjectFactory {

	private final Alarm alarm;
	private final ChecklistCircuitEnding checklistCircuitEnding;
	private final ChecklistCircuitStarting checklistCircuitStarting;
	private final Client client;
	private final Circuit circuit;
	private final CircuitUnit circuitUnit;
	private final CircuitStarted circuitStarted;
	private final Message message;
	private final DistrictWatch districtWatch;
	private final DistrictWatchStarted districtWatchStarted;
	private final DistrictWatchClient districtWatchClient;
	private final DistrictWatchUnit districtWatchUnit;
	private final EventLog eventLog;
	private final EventType eventType;
	private final EventRemark eventRemark;
	private final Guard guard;
    private final ClientContact clientContact;
    private final ClientLocation clientLocation;
    private final LocationTracker locationTracker;

	private final List<ExtendedParseObject> allParseObjects;


    @Inject
	public ParseObjectFactory() {
		alarm = new Alarm();
		checklistCircuitEnding = new ChecklistCircuitEnding();
		checklistCircuitStarting = new ChecklistCircuitStarting();
		circuit = new Circuit();
        circuitStarted = new CircuitStarted();
		circuitUnit = new CircuitUnit();
        client = new Client();
        clientContact = new ClientContact();
        clientLocation = new ClientLocation();
        districtWatch = new DistrictWatch();
        districtWatchClient = new DistrictWatchClient();
        districtWatchStarted = new DistrictWatchStarted();
        districtWatchUnit = new DistrictWatchUnit();
        eventLog = new EventLog();
        eventRemark = new EventRemark();
        eventType = new EventType();
        locationTracker = new LocationTracker();
		message = new Message();
        guard = new Guard();


		allParseObjects = new ArrayList<ExtendedParseObject>();
		allParseObjects.add(alarm);
		allParseObjects.add(checklistCircuitEnding);
		allParseObjects.add(checklistCircuitStarting);
		allParseObjects.add(client);
		allParseObjects.add(circuit);
		allParseObjects.add(circuitUnit);
		allParseObjects.add(circuitStarted);
		allParseObjects.add(message);
		allParseObjects.add(districtWatch);
		allParseObjects.add(districtWatchStarted);
		allParseObjects.add(districtWatchClient);
		allParseObjects.add(districtWatchUnit);
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

	/**
	 * Returns all ParseObjects that should fill local database prior to usage
	 * 
	 * @return List<ExtendedParseObject>
	 */
	// public List<ExtendedParseObject> getAllPreloadable() {
	// List<ExtendedParseObject> preloadable = new
	// ArrayList<ExtendedParseObject>();
	// preloadable.addUnique(checklistCircuitEnding);
	// preloadable.addUnique(checklistCircuitStarting);
	// preloadable.addUnique(circuit);
	// preloadable.addUnique(circuitUnit);
	// preloadable.addUnique(circuitStarted);
	// preloadable.addUnique(districtWatch);
	// preloadable.addUnique(districtWatchClient);
	// preloadable.addUnique(districtWatchUnit);
	// preloadable.addUnique(eventType);
	// preloadable.addUnique(guard);
	// return preloadable;
	// }

	public Client getClient() {
		return client;
	}

	public Alarm getAlarm() {
		return alarm;
	}

	public EventRemark getEventRemark() {
		return eventRemark;
	}

	public CircuitUnit getCircuitUnit() {
		return circuitUnit;
	}


	public ChecklistCircuitEnding getChecklistCircuitEnding() {
		return checklistCircuitEnding;
	}

	public ChecklistCircuitStarting getChecklistCircuitStarting() {
		return checklistCircuitStarting;
	}

	public Circuit getCircuit() {
		return circuit;
	}

	public CircuitStarted getCircuitStarted() {
		return circuitStarted;
	}

	public Message getMessage() {
		return message;
	}

	public DistrictWatch getDistrictWatch() {
		return districtWatch;
	}

	public DistrictWatchClient getDistrictWatchClient() {
		return districtWatchClient;
	}

	public DistrictWatchUnit getDistrictWatchUnit() {
		return districtWatchUnit;
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
