//package com.guardswift.persistence.parse;
//
//import com.guardswift.persistence.parse.data.EventType;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.data.client.ClientContact;
//import com.guardswift.persistence.parse.data.client.ClientLocation;
//import com.guardswift.persistence.parse.documentation.event.EventLog;
//import com.guardswift.persistence.parse.documentation.event.EventRemark;
//import com.guardswift.persistence.parse.documentation.gps.Tracker;
//import com.guardswift.persistence.parse.documentation.report.Report;
//import com.guardswift.persistence.parse.execution.task.ParseTask;
//import com.guardswift.persistence.parse.misc.Message;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//@Singleton
//public class ParseObjectFactory {
//
//	private final Client client;
//	private final ParseTask task;
//	private final EventLog eventLog;
//	private final EventType eventType;
//	private final EventRemark eventRemark;
//	private final Guard guard;
//    private final ClientContact clientContact;
//    private final ClientLocation clientLocation;
//    private final Tracker tracker;
//	private final Report report;
//	private final Message message;
//
//	private final List<ExtendedParseObject> allParseObjects;
//
//
//    @Inject
//	public ParseObjectFactory() {
//		task = new ParseTask();
//        client = new Client();
//        clientContact = new ClientContact();
//        clientLocation = new ClientLocation();
//        eventLog = new EventLog();
//        eventRemark = new EventRemark();
//        eventType = new EventType();
//        tracker = new Tracker();
//        guard = new Guard();
//		report = new Report();
//		message = new Message();
//
//		allParseObjects = new ArrayList<>();
//		allParseObjects.add(client);
//		allParseObjects.add(task);
//		allParseObjects.add(eventLog);
//		allParseObjects.add(eventType);
//		allParseObjects.add(eventRemark);
//		allParseObjects.add(guard);
//        allParseObjects.add(clientContact);
//        allParseObjects.add(clientLocation);
//        allParseObjects.add(tracker);
//		allParseObjects.add(message);
//	}
//
//	public List<ExtendedParseObject> getAll() {
//		return allParseObjects;
//	}
//
//
//	public Client getClient() {
//		return client;
//	}
//
//
//	public EventRemark getEventRemark() {
//		return eventRemark;
//	}
//
//	public ParseTask getTask() {
//		return task;
//	}
//
//
//	public EventLog getEventLog() {
//		return eventLog;
//	}
//
//	public EventType getEventType() {
//		return eventType;
//	}
//
//	public Guard getGuard() {
//		return guard;
//	}
//
//	public Message getMessage() { return message; }
//
//}
