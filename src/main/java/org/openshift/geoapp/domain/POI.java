package org.openshift.geoapp.domain;

public class POI {
	private Object info;
	private Object position;
	private Object id;

	public Object getPosition() {
		return position;
	}

	public void setPosition(Object position) {
		this.position = position;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getInfo() {
		return info;
	}

	public void setInfo(Object info) {
		this.info = info;
	}
}
