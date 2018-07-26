package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeRights;

public class AttributeRightsSet implements AuditEvent {
	private AttributeRights right;
	private String name = this.getClass().getName();
	private String message;

	public AttributeRightsSet(AttributeRights right) {
		this.right = right;
	}

	public AttributeRightsSet() {
	}

	public AttributeRights getRight() {
		return right;
	}

	public void setRight(AttributeRights right) {
		this.right = right;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("Attribute right set: {}", right);
	}
}
