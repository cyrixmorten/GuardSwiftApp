package com.guardswift.eventbus.events;

import com.guardswift.persistence.parse.ExtendedParseObject;

public class ParseObjectUpdatedEvent {

	private final ExtendedParseObject object;

	public ParseObjectUpdatedEvent(ExtendedParseObject object) {
		super();
		this.object = object;
	}

	public ExtendedParseObject getObject() {
		return object;
	}

}
