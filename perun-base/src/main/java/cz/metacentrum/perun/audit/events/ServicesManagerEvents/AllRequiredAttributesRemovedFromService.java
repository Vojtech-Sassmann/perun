package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

public class AllRequiredAttributesRemovedFromService implements AuditEvent {

	private Service service;
	private String name = this.getClass().getName();
	private String message;

	public AllRequiredAttributesRemovedFromService() {
	}

	public AllRequiredAttributesRemovedFromService(Service service) {
		this.service = service;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "All required attributes removed from " + service + ".";
	}
}